from keras.preprocessing import sequence
import numpy as np


#
# test its accuracy and workings
#

training_sample_file = 'combined-ts-samples.txt'

vocab_file = 'combined-nnet.bin.v'
model_file = 'combined-nnet.bin'
label_file = 'combined-ts.labels.txt'


# load a set of test samples with their labels
test_samples = []
print("loading test samples")
with open(training_sample_file) as reader:
    for line in reader:
        parts = line.strip().split(':')
        word = parts[0]
        label = int(parts[1])
        words = [item.strip() for item in parts[2].split(',') if len(item) > 0]
        test_samples.append((word,label,words))


# load the label lookup file to get the correct "class" for a word
label_lookup = dict()  # label id -> (word, [4x disambig words])
word2labels = dict()  # lookup the labels for a given word
print("loading labels")
with open(label_file) as reader:
    for line in reader:
        parts = line.strip().split(':')
        label = int(parts[0])
        label_word = parts[1]
        disambiguation_words = [item.strip() for item in parts[3].split(',') if len(item) > 0]
        if len(disambiguation_words) > 4:
            disambiguation_words = disambiguation_words[0:4]
        label_lookup[label] = (label_word, disambiguation_words)
        if not label_word in word2labels:
            word2labels[label_word] = []
        if not label in word2labels[label_word]:
            word2labels[label_word].append(label)


# load vocab for word -> int
word2int = dict()
print("loading vocab")
with open(vocab_file) as reader:
    for line in reader:
        line = line.strip()
        parts = line.split(',')
        word2int[parts[0]] = int(parts[1])


# convert text to nnet input
def text_to_int(text_set, word2int, max_sentence_len):
    np_list = []
    for text_list in text_set:
        int_list = []
        for word in text_list:
            lwr = word.lower()
            if lwr in word2int:
                int_list.append(word2int[lwr])
        np_list.append(np.array(int_list, dtype='int32'))
    return sequence.pad_sequences(np_list, maxlen=max_sentence_len)

# setup nnet
from keras.models import load_model
print("loading model")
model = load_model(model_file)

# create inputs (parallel)
X = []
Y = []
label_word = []
for test in test_samples:
    X.append(test[2])  # word window
    Y.append(test[1])  # collect labels for verification
    label_word.append(test[0])  # the actual word

success = 0
fails = 0
text_input = text_to_int(X, word2int, 50)
problem_words = dict()
result = model.predict_proba(text_input, batch_size=100, verbose=2)
if len(result) == len(Y):  # right number of results got returned?
    for i in range(len(result)):
        # get the labels we're expecting to see
        word = label_word[i]
        label_list = word2labels[word]

        # collect the winning label from the list
        best = -1
        best_score = 0.0
        for id in label_list:
            if result[i][id] > best_score:
                best_score = result[i][id]
                best = id

        print(word + ": expected class=" + str(Y[i]) + ", predicted class=" + str(best))
        if Y[i] != best:
            fails += 1
            if word in problem_words:
                problem_words[word] += 1
            else:
                problem_words[word] = 1
        else:
            success += 1

else:
    raise ValueError("invalid count / return results")

print("finished; successes : " + str(success))
print("              fails : " + str(fails))
print("       success rate : " + str(success / (success+fails)))

print("problem words: " + str(problem_words))
