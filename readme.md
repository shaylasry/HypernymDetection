Hypernym Detection:

This application is a re-designing algorithm for the following experiment:
http://ai.stanford.edu/~rion/papers/hypernym_nips05.pdf

We used map-reduce (AWS EMR) pattern and experimentend the algorithm quality on a large-scale input.

Changes:
Parsing the text : Instead of parsing the corpus with MINIPAR,
we used Google Syntactic N-Grams as an input:
http://storage.googleapis.com/books/syntactic-ngrams/index.html

FOR dependency paths we used the following definition instead the one in the paper:
Given two nouns in a sentence, their dependency path is composed of the
nodes and the edges (i.e., the words and the dependency labels) in the
shortest path between them.

Producing train and test sets (Section 3): 
Instead of acquiring an annotated set from WordNet or manually annotate randomly-selected pairs. 
We used provided hypernym.txt file - composed of word pairs and their annotations
(True - the second word is a hypernym of the first word, False - the
second word is not a hypernym of the first word).

*The application work with every file with the same structure as hypernym.txt.


There is no implementation for evaluation of the extracted features, as described at section 4.
There is no implementation for the simple classifiers, described at the second part of the second paragraph of section 5.
There is no implementation for the usage of coordinate terms (Section 6).


Implementation related information:

The words in the Google Syntactic N-Grams dataset are not stemmed. 
For example, the words table and tables are considered different words even though they share the same
root/lexeme - 'table'.
In order to improve the model we used Snowball stemmer to deal with this issue.

To isolate nouns we used the following list of the part of speeches that appear in the
syntactic ngrams:
http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html

You can read more about The Syntactic Ngrams Corpus dataset in the following link:
https://docs.google.com/document/d/14PWeoTkrnKk9H8_7CfVbdvuoFZ7jYivNTkBX2Hj7qLw/edit#

DPMin - The minimal number of unique noun pairs for each dependency
path. Dependency path with less distinct noun-pairs should not be
considered as a feature (as described at the first paragraph of section 4).


Run instructions:
	vectorbuilder:
	create a bucket and upload to it the hypernym.txt and biarcs files
	In vectorbuilder main.java file change all "s3://ofiwjoiwf/map2output/" to "s3://%YOUR BUCKET NAME%/map2output/"
	package the code and upload to all parts jar files to bucket
	run main and use the output in weka

	weka: download the parts file form the last step and put it in the same directory as the pom in the experiment folder and run the main and it will take care of the rest




Application:


Map-Reduce: 

First map-reduce: for each line in the input file find all the nouns and create for each pair depenecy paths

Mapper:
Key: Map1to2Key <dp, index> , we write new lines twice (with indexes 0 and 1)
	we use sort the key by dp and then by index.
	we iterate the index 0 keys to get the unique counter for the same dp.
 	we iterate the index 1 to use context.write for each dp with dpMin >= unique counter.

Value : Map1Value <nouns , count>, nouns is a string that describe the nouns we found dependency path for and count is the number of time that the dependency path appeared in the corpus which means the amount of times the nouns appeared.

number of key-values pairs: according to the number of 2*(number of valid paths between two Nouns in each of the lines)


Reducer:

Key: Map1to2Key
Value : Map1Value

output:Text, NounsData

number of key-values pairs: for each key that unique counter >= dpmin we get the amount of the different pairs that related to it.



Second map-reduce: Create the vector for each pair of nouns

Mapper:
Key: Text
Value : NounsData <nouns, count, index> , extend of Map1Value - dp index added.

number of key-values pairs: same as Reducer 1.

Reducer:

Key: Nouns <word1, word2> , make the nouns as a key.
Value : DpData <dp, count, index> , after making nouns as key we make the simialer object to NounsData but now we keep dp instead.

output:Nouns, NounsVector
number of key-values pairs: number of different nouns pairs


Third map-reduce: Takes an exmpale file (current file : hypernym.txt) and keep only the nouns that are in it.

Mapper:
Key: Nouns.
Value : NounsVector <ArrayList<LongWritable> counts, isHypernym>. Nouns vectoer represent the characaristics vector and the boolan value from the example file (current file : hypernym.txt)

number of key-values pairs: number of different nouns pairs

Reducer:
Key: Nouns
Value : NounsVector

output:Nouns, NounsVectora

number of key-values pairs: number of different nouns pairs

memory usage:
We only used memory to save the input biarcs file and the hypernym.txt
also for each vector we used a list of longs to create the charecaristics vector but it is bound to the size of the number of features we use
We save only 1 vector at a time in the same scope.



weka:
	Takes the output file from the last step, which contains all the pairs and the match feature vector
	,and run the experiment.
	


=== Evaluation Results ===
Results
Correctly Classified Instances        8725               75.929  %
Incorrectly Classified Instances      2766               24.071  %
Kappa statistic                          0     
Mean absolute error                      0.3643
Root mean squared error                  0.4271
Relative absolute error                 99.6507 %
Root relative squared error             99.8992 %
Total Number of Instances            11491     

Precision: 0.7592898790357672
Recall: 1.0
F1 Score: 0.863177681044717
TRUE POSITIVE:
TRUE NEGATIVE:
"translat"/"edit"
"part"/"discours"
"messag"/"book"
"stori"/"hatr"
"part"/"voic"
FALSE NEGATIVE:
"writ"/"summon"
"posit"/"order"
"polici"/"contain"
"territori"/"arkansa"
"habit"/"resid"
FALSE POSITIVE:
