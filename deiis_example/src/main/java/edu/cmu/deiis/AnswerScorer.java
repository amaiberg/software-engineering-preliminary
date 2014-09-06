package edu.cmu.deiis;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.Question;

public abstract class AnswerScorer extends JCasAnnotator_ImplBase {

	@Override
	public void initialize( UimaContext context ){
		try {
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}
	@Override
	public void process( JCas jcas ) throws AnalysisEngineProcessException {
		AnnotationIndex<?> questionIndex = jcas.getAnnotationIndex( Question.type );
		FSIterator<?> questionIterator = questionIndex.iterator();
		Question question = (Question)questionIterator.next();
		AnnotationIndex<?> answerIndex = jcas.getAnnotationIndex( Answer.type );
		FSIterator<?> answerIterator = answerIndex.iterator();
		while ( answerIterator.hasNext() ){
			Answer answer = (Answer)answerIterator.next();
			double scoreValue = getAnswerScore( jcas , question , answer );
			AnswerScore score = new AnswerScore( jcas , answer.getBegin() , answer.getEnd() );
			score.setAnswer( answer );
			score.setScore( scoreValue );
			score.setCasProcessorId( this.getClass().getName() );
			score.setConfidence( 1.0d ); // scores are exact computations, so c = 1
			score.addToIndexes();			
		}
	}
	public abstract double getAnswerScore( JCas jcas , Question question , Answer answer);
}
