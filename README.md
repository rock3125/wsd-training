# wsd-training

Create training data for Word Sense Disambiguation (WSD) deep learning

A Java based project using my own Semantic Lexicon for creating word sense disambiguation training data for deep learning.
WordNet does not provide a clear enough set of semantic nouns, so I've created my own clear and practical training set.

## parser

This uses the Apache OpenNLP parser for sentence boundary detection, and Part of Speech (POS) tagging using Penn tags.

## gradle build
Creates a Java set in `./dist/` by running
```
gradle clean build setup
```

## supported training files
Added support for reading `.txt`, `.gz` and Peter's `.parsed` file formats for setting up
Unlabelled training data

## python Keras DNN

The processed data is then used to train an LSTM using Keras/Tensorflow that can be loaded to get a neural network that will label the correct Synset ID (according to the lexicon) and assing a Synset ID to an ambiguous noun.

With the right lexicon / training data this seems to get around a 75% accuracy level.
