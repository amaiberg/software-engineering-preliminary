package edu.cmu.deiis;

import java.util.Comparator;

import edu.cmu.deiis.types.AnswerScore;

public class AnswerScoreComparator implements Comparator<AnswerScore> {

	@Override
	public int compare( AnswerScore o1 , AnswerScore o2 ) {
		double s1 = o1.getScore();
		double s2 = o2.getScore();
		if ( s1 < s2 ) return 1;
		if ( s1 > s2 ) return -1;
		return 0;
	}
	
}
