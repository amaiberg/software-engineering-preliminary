package edu.cmu.deiis;

import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Annotation;
import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;

public class RegexAnnotator extends JCasAnnotator_ImplBase {
	
	// These parameters must be set in the analysis engine descriptor.
	private static String REGEX_PARAMETER_NAME = "regex";
	private static String TYPE_PARAMETER_NAME = "typeName";
	private static String COMPONENT_PARAMETER_NAME = "componentId";

	private Pattern regex;
	private String componentId;
	private Constructor<?> typeInstanceConstructor;
	private static Boolean DEBUG = false;
	

	@Override
	public void initialize( UimaContext context ){
		regex = Pattern.compile( (String)context.getConfigParameterValue( REGEX_PARAMETER_NAME ) );
		componentId = (String)context.getConfigParameterValue( COMPONENT_PARAMETER_NAME );
		try {
			typeInstanceConstructor = Class.forName( (String)context.getConfigParameterValue( TYPE_PARAMETER_NAME ) )
					.getConstructor( JCas.class , int.class , int.class );
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}
	@Override
	public void process( JCas jcas ) throws AnalysisEngineProcessException {
		processInstances( jcas , Question.type );
		processInstances( jcas, Answer.type );
	}
	private void processInstances( JCas jcas , int type ) throws AnalysisEngineProcessException {
		AnnotationIndex<?> index = jcas.getAnnotationIndex( type );
		FSIterator<?> iterator = index.iterator();
		while ( iterator.hasNext() ){
			Annotation instance = (Annotation)iterator.next();
			int offset = instance.getBegin();
			Matcher match = regex.matcher( instance.getCoveredText() );
			while ( match.find() ) {
				try {
					Annotation a = (Annotation)typeInstanceConstructor.newInstance( jcas , offset + match.start() , offset + match.end() );
					a.setCasProcessorId( componentId );
					a.setConfidence( 1.0d );
					a.addToIndexes();
					if ( DEBUG ) System.out.println( a );
				} catch ( Exception e ) {
					throw new AnalysisEngineProcessException( e );
				}
			}
		}
	}
}


