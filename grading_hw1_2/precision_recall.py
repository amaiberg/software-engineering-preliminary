#!/usr/bin/python
'''
Measure P and R given ground truth and other data
'''

# Written by Shriphani Palakodety 

import argparse

def compare(truth_file, test_file):
  with open(truth_file, 'r') as truth_file_handle, open(test_file, 'r') as test_file_handle:
		truth_lines = set(truth_file_handle.readlines())
		test_lines = set(test_file_handle.readlines())

  if 0 == len(test_lines):
    precision = 0.0
    recall = 0.0
    f1_score = 0.0
  else:
    truths_retrieved = set.intersection(truth_lines, test_lines)

    precision = len(truths_retrieved) / float(len(test_lines))
    recall = len(truths_retrieved) / float(len(truth_lines))
    suma = (precision + recall)
    if suma > 0:
      f1_score = 2.0 * (precision * recall) / suma
    else:
      print "NO RELEVANT RECORDS!"
      f1_score = 0


  return precision, recall, f1_score

if __name__ == '__main__':

	parser = argparse.ArgumentParser()

	parser.add_argument(
		'--truth',
		dest = 'truth', 
	)
	parser.add_argument(
		'--test',
		dest = 'test', 
	)
	
	args = parser.parse_args()

	precision, recall, f1_score = compare(args.truth, args.test)

	print 'Precision:', precision
	print 'Recall:', recall
	print 'F1 Score:', f1_score
