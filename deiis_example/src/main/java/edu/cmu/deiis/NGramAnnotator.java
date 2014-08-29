package edu.cmu.deiis;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import edu.cmu.deiis.types.Annotation;
import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.NGram;
import edu.cmu.deiis.types.Question;

public class NGramAnnotator extends JCasAnnotator_ImplBase {
	
	// These parameters must be set in the analysis engine descriptor.
	private static String N_PARAMETER_NAME = "n";
	private static String TYPE_PARAMETER_NAME = "typeName";
	private static String COMPONENT_PARAMETER_NAME = "componentId";
	
	private Integer n;
	private String typeName, componentId;
	private static Boolean DEBUG = false;

	@Override
	public void initialize( UimaContext context ){
		n = (Integer)context.getConfigParameterValue( N_PARAMETER_NAME );
		typeName = (String)context.getConfigParameterValue( TYPE_PARAMETER_NAME );
		componentId = (String)context.getConfigParameterValue( COMPONENT_PARAMETER_NAME );
	}
	@Override
	public void process( JCas jcas ) throws AnalysisEngineProcessException {
		processInstances( jcas , Question.type );
		processInstances( jcas , Answer.type );
	}
	public void processInstances( JCas jcas , int type ){
		AnnotationIndex<?> instanceIndex = jcas.getAnnotationIndex( type );
		FSIterator<?> instanceIterator = instanceIndex.iterator();
		while ( instanceIterator.hasNext() ){
			Annotation instance = (Annotation)instanceIterator.next();
			AnnotationIndex<?> typeIndex = jcas.getAnnotationIndex( jcas.getTypeSystem().getType( typeName ) );
			FSIterator<?> typeIterator = typeIndex.subiterator( instance );
			List<Annotation> typeInstances = new ArrayList<Annotation>();
			while ( typeIterator.hasNext() )
				typeInstances.add( (Annotation)typeIterator.next() );
			Annotation[] array = new Annotation[ typeInstances.size() ];
			for ( int i = 0 ; i < typeInstances.size(); i++ )
				array[ i ] = typeInstances.get( i );
			int lastNGramStart = typeInstances.size() - n + 1; 
			for ( int offset = 0 ; offset < lastNGramStart ; offset++ ) {
				FSArray elementsArray = new FSArray( jcas , n );
				for ( int i = 0 ; i < n ; i++ )
					elementsArray.set( i , array[ offset + i ] );
				NGram a = new NGram( jcas , array[ offset ].getBegin() , array[ offset + n - 1 ].getEnd() );
				a.setElementType( typeName );
				a.setElements( elementsArray );
				a.setCasProcessorId( componentId );
				a.setConfidence( 1.0d );
				a.addToIndexes();
				if ( DEBUG ) System.out.println( a );
			}
		}
	}

}
