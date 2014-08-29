package edu.cmu.deiis;

import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;

public class GoldAnswerScorer extends AnswerScorer {

	@Override
	public double getAnswerScore( JCas jcas , Question question , Answer answer ) {
		return answer.getIsCorrect() ? 1.0d : 0.0d;
	}

}
