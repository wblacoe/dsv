package models.counting;

import experiment.common.ContextWindow;
import experiment.common.BagOfContextWindows;
import experiment.AbstractExperiment;
import java.io.IOException;
import models.AbstractModel;
import experiment.common.Parameters;
import integerset.interval.DoubleUnlimitedInterval;
import java.util.concurrent.BlockingQueue;
import experiment.common.Description;
import experiment.common.Label;
import integerset.AbstractSetOfIntegers;
import integerset.interval.LimitedInterval;
import lingunit.flattext.Word;
import lingunit.language.Document;
import lingunit.language.ParsedSentence;
import models.AbstractModelThread;
import pattern.language.LanguagePattern;
import pipeline.signals.PipelineSignal;
import vector.SparseIntegerVector;
import vocabulary.Vocabulary;

public abstract class Counter extends AbstractModel {
	
	public static final int SIMPLE_COUNTER = 0;
	public static final int CLUSTERING_COUNTER = 1;
	public static String[] COUNTER_MODELS = new String[]{ "simple counter", "clustering counter" };
    
	protected boolean areCountVectorsSparse;
	protected int dimensionality;
	protected BagOfContextWindows contextWindows;
	
    public Counter() throws IOException{
        areCountVectorsSparse = Parameters.getBooleanParameter("are count vectors sparse");
		dimensionality = AbstractExperiment.getDimensionality();
		contextWindows = AbstractExperiment.getContextWindows();
    }
	
    @Override
    public Description getDescription(){
        Description d = super.getDescription();
		d.addParameter("amount of documents", Parameters.getIntParamter("amount of documents"));
        d.addParameter("are count vectors sparse", areCountVectorsSparse);
        d.addParameter("dimensionality", dimensionality);
        
        return d;
    }
    
	public static Counter create(int counterModel) throws IOException{
        //System.out.println("creating counter model \"" + counterModel + "\"..."); //DEBUG
		Counter counter;
		
		switch(counterModel){
			case SIMPLE_COUNTER:
				counter = new OfflineCounter();
				break;
			case CLUSTERING_COUNTER:
				counter = new OnlineCounter();
				break;
			default:
				counter = null;
		}
		
		return counter;
	}
    
    public static Counter create() throws IOException{
        return create(Parameters.getConstantParameter("counter model", COUNTER_MODELS));
    }
    
    public static Counter create(String modelName) throws IOException{
        return create(getIndex(COUNTER_MODELS, modelName));
    }

}



abstract class AbstractCounterThread extends AbstractModelThread{

	int dimensionality;
	BagOfContextWindows contextWindows, fullTargetSentenceWindowAsBag;
	boolean areCountVectorsSparse;
    final LimitedInterval targetSentenceWindow;
	
	public AbstractCounterThread(AbstractModel superior, Label threadLabel, BlockingQueue<PipelineSignal> signalQueue, int dimensionality, boolean areCountVectorsSparse){
        super(superior, threadLabel, signalQueue);
        
		this.dimensionality = dimensionality;
		contextWindows = AbstractExperiment.getContextWindows();
		ContextWindow fullTargetSentenceWindow = new ContextWindow(new DoubleUnlimitedInterval(), ContextWindow.TARGET_SENTENCE_INTERNAL_WINDOW);
		fullTargetSentenceWindowAsBag = new BagOfContextWindows();
		fullTargetSentenceWindowAsBag.add(fullTargetSentenceWindow);
		
		this.areCountVectorsSparse = areCountVectorsSparse;
        targetSentenceWindow = new LimitedInterval(0, 0);
	}
	
    
    protected SparseIntegerVector[] getAllSentenceCountVectors(Document document, BagOfContextWindows contextWindows){
		SparseIntegerVector[] sentenceCountVectors = new SparseIntegerVector[document.getSize()];
		
        for(ParsedSentence parsedSentence : document.getParsedSentences()){
			
			//save marginal counts
			Vocabulary.increaseTotalWordCountBy(parsedSentence.getSize());
			for(Word word : parsedSentence.getAllWords()) word.increaseTargetElementCountByOne();
			
			//collect context counts
			SparseIntegerVector sentenceCountVector = new SparseIntegerVector(dimensionality);
			for(LanguagePattern contextElement : AbstractExperiment.getContextElements()){
				//if this sentence contains this context element
				AbstractSetOfIntegers[] targetWordIndicesArray = contextElement.getTargetWordIndices(parsedSentence, fullTargetSentenceWindowAsBag);
				if(!targetWordIndicesArray[0].isEmpty()){
					//then save a count for this context element in this sentence's count vector
					int dimension = contextElement.languagePatternIndex;
					sentenceCountVector.add(dimension, 1);
				}
			}
			sentenceCountVectors[parsedSentence.getDocumentInternalSentenceId()] = sentenceCountVector;
        }
		
		return sentenceCountVectors;
	}
    
	public abstract void count(Document document) throws IOException;
    
}
