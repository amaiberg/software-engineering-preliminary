package edu.cmu.deiis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;

public class TestElementAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process( JCas jcas ) throws AnalysisEngineProcessException {
		String testElement = jcas.getDocumentText();
		int begin = 0 ; 
		int end = 0;
		Pattern eol = Pattern.compile( "\n" );
		Matcher matcher = eol.matcher( testElement );
		while ( matcher.find() ) {
			end = matcher.start();
			String line = testElement.substring( begin , end );
			if ( line.startsWith( "Q" ) ) 
				createQuestion( jcas , line , begin , end );
			if ( line.startsWith( "A" ) )
				createAnswer( jcas , line , begin , end );
			begin = matcher.end();
		}
	}

	private void createAnswer( JCas jcas, String string , int begin , int end ) {
		if ( string == null ) return;
		Answer a = new Answer( jcas , begin + 4 , end );
		String[] fields = string.split( " " , 3 );
		Integer isCorrect = Integer.parseInt( fields[ 1 ] );
		if ( isCorrect == 1 ) a.setIsCorrect( true );
		a.setCasProcessorId( this.getClass().getName() );
		a.setConfidence( 1.0d );
		a.addToIndexes();		
	}

	private void createQuestion( JCas jcas, String string , int begin , int end ) {
		if ( string == null ) return;
		Question q = new Question( jcas , begin + 2 , end );
		q.setCasProcessorId( this.getClass().getName() );
		q.setConfidence( 1.0d );
		q.addToIndexes();
	}

}
