
word2int = dict()
with open('combined-ts.txt') as reader:
    X = []
    Y = []
    counter = 0
    num_classes = 0
    for line in reader:
        line = line.strip()
        parts = line.split(':')
        label = int(parts[0])
        words = [item for item in parts[1].split(',') if len(item) > 0]
        for word in words:
            if not word in word2int:
                word2int[word] = len(word2int) + 1

# write vocab
with open('combined-ts.vocab.txt', 'w') as writer:
    for key in word2int:
        writer.write(key + "," + str(word2int[key]) + "\n")
