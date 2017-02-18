from keras.preprocessing import sequence
import numpy as np


# the input to the network, the word to "disambiguate" and the word_window list of words around it
class NNetWSDInput:
    def __init__(self, word, word_window):
        self.word = word
        self.word_window = word_window


# word sense disambiguation nnet
class NNetWSD:
    # load the model and the labels (the vocab is derived from the model name)
    def __init__(self, model_file, label_file, max_sentence_len=50):
        from keras.models import load_model
        print("loading WSD NNET model from " + model_file)
        self.model = load_model(model_file)
        self.load_nnet_vocab(model_file)
        self.load_labels(label_file)
        self.max_sentence_len = max_sentence_len
        print("loading WSD NNET done!")


    # perform word sence disambiguation on a list of tokens
    def wsd(self, token_list, window_size):
        judged = {}
        collection = {}
        wsd_input_list = []
        for i in range(len(token_list)):
            token = token_list[i]
            lwr = token.text.lower()
            if self.is_amiguous_noun(lwr, token.tag):
                if lwr not in collection:
                    collection[lwr] = []
                collection[lwr].append(token)  # collect all ambiguous families
                if not lwr in judged:  # judge them only once
                    judged[lwr] = 1  # now "in use" ala. Jarowski
                    word_list = [item.text.lower() for item in
                                 token_list[max(i - window_size, 0):min(i + window_size, len(token_list))]]
                    wsd_input_list.append(NNetWSDInput(lwr, word_list))

        # set all the ids of the affected tokens
        if len(wsd_input_list) > 0:
            result_list = self.predict(wsd_input_list)
            if len(result_list) == len(wsd_input_list):
                for item in result_list:
                    if item[0] in collection:
                        token_list = collection[item[0]]
                        for token in token_list:
                            token.synid = item[1]


    # input list is a list of NNetWSDInput items
    # returns a list of predicted labels as a list of the same size as the input list
    def predict(self, wsd_input_list, batch_size=100, verbose=2):
        # collect words and text
        text_list = []
        word_list = []
        for item in wsd_input_list:
            text_list.append(item.word)
            word_list.append(item.word_window)

        result_list = []
        text_input = self.text2int(text_list)  # turn words into ints for the nnet
        result = self.model.predict_proba(text_input, batch_size=batch_size, verbose=verbose)
        if len(result) == len(text_input):  # right number of results got returned?
            for i in range(len(result)):
                # get the labels we're expecting to see
                word = text_list[i]
                if word in self.word2labels:
                    label_list = self.word2labels[word]
                    smallest_value = min(label_list)  # get the base value of the labels for this word

                    # collect the winning label from the list
                    best = -1
                    best_score = -5.0
                    for id in label_list:
                        if result[i][id] > best_score:
                            best_score = result[i][id]
                            best = id

                    word_synset = best - smallest_value
                    result_list.append((word,word_synset))
                else:
                    result_list.append((word,-1))

        else:
            raise ValueError("invalid return results from nnet")

        return result_list


    # load the labels file - explaining what words go with what labels for WSD
    def load_labels(self, label_file):
        # load the label lookup file to get the correct "class" for a word
        self.label_lookup = dict()  # label id -> (word, [4x disambig words])
        self.word2labels = dict()  # lookup the labels for a given word
        with open(label_file) as reader:
            for line in reader:
                parts = line.strip().split(':')
                label = int(parts[0])
                label_word = parts[1]
                disambiguation_words = [item.strip() for item in parts[3].split(',') if len(item) > 0]
                if len(disambiguation_words) > 4:
                    disambiguation_words = disambiguation_words[0:4]
                self.label_lookup[label] = (label_word, disambiguation_words)
                if not label_word in self.word2labels:
                    self.word2labels[label_word] = []
                if not label in self.word2labels[label_word]:
                    self.word2labels[label_word].append(label)


    # return true if this parser token's text and tag are a noun of the ambigous nouns of the nnet
    def is_amiguous_noun(self, text, tag):
        return tag.startswith("NN") and text in self.word2labels


    # load nnet vocab that translates word -> int for nnet inputs
    def load_nnet_vocab(self, model_file):
        # load vocab for word -> int
        self.word2int = dict()
        with open(model_file + ".v") as reader:
            for line in reader:
                line = line.strip()
                parts = line.split(',')
                self.word2int[parts[0]] = int(parts[1])

    # convert text to nnet input
    def text2int(self, text_set):
        np_list = []
        for text_list in text_set:
            int_list = []
            for word in text_list:
                lwr = word.lower()
                if lwr in self.word2int:
                    int_list.append(self.word2int[lwr])
            np_list.append(np.array(int_list, dtype='int32'))
        return sequence.pad_sequences(np_list, maxlen=self.max_sentence_len)

