package edu.cmu.deiis;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.Question;

public class Evaluator extends JCasAnnotator_ImplBase {
	
	private static DecimalFormat twoPlace = new DecimalFormat( "0.00" );
	private static AnswerScoreComparator scoreComparator = new AnswerScoreComparator();
	
	private double questionsProcessed;
	private double precisionAccumulator;
	
	@Override
	public void initialize( UimaContext context ){
		try {
			questionsProcessed = 0.0d;
			precisionAccumulator = 0.0d;
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}
	@Override
	public void process( JCas jcas ) throws AnalysisEngineProcessException {
		AnnotationIndex<?> questionIndex = jcas.getAnnotationIndex( Question.type );
		FSIterator<?> questionIterator = questionIndex.iterator();
		Question question = (Question)questionIterator.next();
		System.out.print( "\nQuestion: " + question.getCoveredText() );
		AnnotationIndex<?> answerScoreIndex = jcas.getAnnotationIndex( AnswerScore.type );
		FSIterator<?> answerScoreIterator = answerScoreIndex.iterator();
		List<AnswerScore> scores = new ArrayList<AnswerScore>();
		while ( answerScoreIterator.hasNext() ) 
			scores.add(  (AnswerScore)answerScoreIterator.next() );
		Collections.sort( scores , scoreComparator );
		double totalCorrect = 0.0d;
		for ( AnswerScore answerScore : scores ){
			Answer answer = answerScore.getAnswer();
			if ( answer.getIsCorrect() ) totalCorrect++;
			String label = answer.getIsCorrect() ? "+" : "-" ;
			System.out.print( "\n" +  label + " " + twoPlace.format( answerScore.getScore() ) + " " + answer.getCoveredText() );
		}
		double foundCorrect = 0.0d;
		// precision @ N where N = total # correct answers
		for ( int i = 0 ; i < totalCorrect ; i++ ){
			AnswerScore s = scores.get( i );
			if ( s.getAnswer().getIsCorrect() )
				foundCorrect++;
		}
		double precision = foundCorrect / totalCorrect;
		System.out.println( "\nPrecision at " + totalCorrect + ": " + twoPlace.format( precision ) );
		precisionAccumulator += precision;
		questionsProcessed++;
	}
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		System.out.println( "\nAverage Precision: " + twoPlace.format( precisionAccumulator / questionsProcessed ) );
	}

}
