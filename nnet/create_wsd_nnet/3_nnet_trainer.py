from functools import reduce

import numpy as np
from keras.layers import Dense, Activation, Embedding
from keras.layers import LSTM
from keras.models import Sequential
from keras.optimizers import RMSprop
from keras.preprocessing import sequence
from keras.callbacks import TensorBoard


# see combined-ts.labels.txt for num_classes
def nnetDataGenerator(filename, word2int, num_classes, batch_size = 4096, max_sentence_len=50):

    # provide data for the nnet
    while True:
        with open(filename) as reader:
            X = []
            Y = []
            counter = 0
            for line in reader:
                line = line.strip()
                parts = line.split(':')
                label = int(parts[0])
                words = [item for item in parts[1].split(',') if len(item) > 0]
                word_ints = []
                for word in words:
                    if not word in word2int:
                        word2int[word] = len(word2int) + 1
                    word_ints.append(word2int[word])
                X.append(word_ints)
                Y.append(label)

                counter += 1
                if counter % batch_size == 0:
                    # make the Y's one hot
                    Y_oh = []
                    for y in Y:
                        data = np.zeros(num_classes)
                        data[y] = 1.0
                        Y_oh.append(data)

                    # yield both X and Y's for training
                    x = sequence.pad_sequences(np.array(X), maxlen=max_sentence_len)
                    yield x, np.array(Y_oh)
                    X = []  # reset for next run
                    Y = []

class Trainer:
    """train a neural network"""

    # do the training itself, y is a set of one-hot vectors
    # num_outputs = number of classes
    def train(self, ts_filename, vocab, num_outputs, num_samples, batch_size=4096,
              max_sentence_len=50, learning_rate=0.01, epochs=1, hidden_layer_size=100):

        self.vocab = vocab
        max_features = reduce(max, vocab.values()) + 1  # largest value in the vocab + 1
        print(len(vocab), 'vocab entries, max feature + 1 = ' + str(max_features))

        print('building model')
        self.model = Sequential()
        self.model.add(Embedding(input_dim=max_features, output_dim=hidden_layer_size,
                                 input_length=max_sentence_len, dropout=0.2))
        self.model.add(LSTM(output_dim=hidden_layer_size, dropout_W=0.2, dropout_U=0.2))
        self.model.add(Dense(input_dim=hidden_layer_size, output_dim=num_outputs))
        self.model.add(Activation(input_shape=(1,num_outputs), activation='softmax'))

        # setup tensorboard for keras
        tb = TensorBoard(log_dir='./logs', histogram_freq=1, write_graph=True, write_images=True)

        # sgd = stochastic gradient decent
        rms = RMSprop(lr=learning_rate, rho=0.9, epsilon=1e-08, decay=0.0)  # best for LSTM
        # sgd = SGD(lr=learning_rate, decay=1e-5, momentum=0.9, nesterov=True)
        self.model.compile(loss='binary_crossentropy',
                           optimizer=rms,
                           metrics=['accuracy'])

        print('training model')
        self.model.fit_generator(nnetDataGenerator(ts_filename, vocab, num_outputs, batch_size=batch_size),
                                 samples_per_epoch=num_samples, nb_epoch=epochs, callbacks=[tb])

    # write a model to file (two files) after training
    def save_model(self, model_filename):
        print('saving model as', model_filename)
        self.model.save(model_filename)
        self._save_vocab(model_filename + ".v", self.vocab)

    # little helper to save the lexicon file
    def _save_vocab(self, model_vocab_filename, vocab):
        f = open(model_vocab_filename, 'w')
        for key, value in vocab.items():
            if len(key) > 0:
                f.write(key + ',' + str(value) + '\n')
        f.close()


#
# test the nnet training
#

# the training file is just a random arrangement of "combined-ts.txt"
# e.g   sort --random-sort combined-ts.tx > combined-ts-rnd.txt
training_file = 'combined-ts-rnd.txt'

label_file = 'combined-ts.labels.txt'
vocab_file = 'combined-ts.vocab.txt'
model_save_filename = 'combined-nnet.bin'
batch_size = 4096
epochs = 5

# read vocab
word2int = dict()
print("reading vocab")
with open(vocab_file) as reader:
    for line in reader:
        line = line.strip()
        parts = line.split(',')
        word2int[parts[0]] = int(parts[1])

# count number of training samples
print("counting samples")
num_samples = 0
with open(training_file) as reader:
    for line in reader:
        num_samples += 1
print("got " + str(num_samples) + " samples in " + training_file)

# count the number of labels
print("counting labels")
num_labels = 0
with open(label_file) as reader:
    for line in reader:
        num_labels += 1
print("got " + str(num_labels) + " labels in " + training_file)

t = Trainer()
samples_per_epoch = int(num_samples / batch_size) * batch_size  # round
t.train(training_file, word2int, num_labels, samples_per_epoch, batch_size=batch_size, epochs=epochs)

print("saving model and vocab as " + model_save_filename)
t.save_model(model_save_filename)
