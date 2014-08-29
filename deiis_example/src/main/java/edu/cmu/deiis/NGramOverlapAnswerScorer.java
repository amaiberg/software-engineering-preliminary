package edu.cmu.deiis;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.NGram;
import edu.cmu.deiis.types.Question;

public class NGramOverlapAnswerScorer extends AnswerScorer {

	@Override
	public double getAnswerScore( JCas jcas , Question question , Answer answer ) {
		AnnotationIndex<?> NGramIndex = jcas.getAnnotationIndex( NGram.type );
		
		Map<String,NGram> answerNGramMap = new HashMap<String,NGram>();
		FSIterator<?> answerNGrams = NGramIndex.subiterator( answer );
		while ( answerNGrams.hasNext() ){
			NGram answerNGram = (NGram)answerNGrams.next();
			answerNGramMap.put( answerNGram.getCoveredText(), answerNGram );
		}
		
		FSIterator<?> questionNGrams = NGramIndex.subiterator( question );
		double questionNGramsFound = 0.0d;
		while ( questionNGrams.hasNext() ){
			NGram questionNGram = (NGram)questionNGrams.next();
			NGram matchingNGram = answerNGramMap.get( questionNGram.getCoveredText() );
			if ( matchingNGram != null )
				questionNGramsFound++;
		}
		double totalAnswerNGrams = (double)answerNGramMap.size();		
		return questionNGramsFound / totalAnswerNGrams;
	}

}
