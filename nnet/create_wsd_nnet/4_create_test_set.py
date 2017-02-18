

#
# create a training set from the random file
# its not separate as it should be - but its just for quick sanity testing for now
#

training_file = 'combined-ts-rnd.txt'
training_sample_file = 'combined-ts-samples.txt'
max_samples_per_label = 5
label_file = 'combined-ts.labels.txt'


# load the label lookup file to get the correct "class" for a word
label_lookup = dict()
label2word = dict()
with open(label_file) as reader:
    for line in reader:
        parts = line.strip().split(':')
        label = int(parts[0])
        label_word = parts[1]
        label_lookup[label] = label_word


sample_count = dict()
sample_set = []
with open(training_file) as reader:
    for line in reader:
        parts = line.strip().split(":")
        if len(parts) == 2:
            label = int(parts[0])
            word = label_lookup[label]
            if label not in sample_count:
                sample_count[label] = 1
                sample_set.append(word + ":" + line.strip())
            elif sample_count[label] < max_samples_per_label:
                sample_count[label] += 1
                sample_set.append(word + ":" + line.strip())

# and write the collected samples to a test file
with open(training_sample_file, 'w') as writer:
    for sample in sample_set:
        writer.write(sample + "\n")

