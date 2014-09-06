package edu.cmu.deiis;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;
import edu.cmu.deiis.types.Token;

public class TokenOverlapAnswerScorer extends AnswerScorer {

	@Override
	public double getAnswerScore( JCas jcas , Question question , Answer answer ) {
		AnnotationIndex<?> tokenIndex = jcas.getAnnotationIndex( Token.type );
		
		Map<String,Token> answerTokenMap = new HashMap<String,Token>();
		FSIterator<?> answerTokens = tokenIndex.subiterator( answer );
		while ( answerTokens.hasNext() ){
			Token answerToken = (Token)answerTokens.next();
			answerTokenMap.put( answerToken.getCoveredText(), answerToken );
		}
		
		FSIterator<?> questionTokens = tokenIndex.subiterator( question );
		double questionTokensFound = 0.0d;
		while ( questionTokens.hasNext() ){
			Token questionToken = (Token)questionTokens.next();
			Token matchingToken = answerTokenMap.get( questionToken.getCoveredText() );
			if ( matchingToken != null )
				questionTokensFound++;
		}
		double totalAnswerTokens = (double)answerTokenMap.size();		
		return questionTokensFound / totalAnswerTokens;
	}

}
