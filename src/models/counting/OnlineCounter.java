package models.counting;

import experiment.common.ContextWindow;
import experiment.AbstractExperiment;
import java.io.IOException;
import java.util.TreeMap;
import lingunit.flattext.Word;
import lingunit.language.Document;
import experiment.common.Description;
import experiment.common.Label;
import integerset.AbstractSetOfIntegers;
import integerset.interval.IntegerInterval;
import integerset.interval.LimitedInterval;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import lingunit.language.ParsedSentence;
import meta.Helper;
import models.AbstractModelThread;
import pattern.language.LanguagePattern;
import pipeline.signals.FinishSignal;
import pipeline.signals.NotifySignal;
import pipeline.signals.PipelineSignal;
import pipeline.signals.StartSignal;
import vector.AbstractVector;
import vector.IntegerVector;
import vector.SparseIntegerVector;
import vector.complex.BagOfMeaningRepresentations;

//each count vector (per context window and target word) is signalled online to the pipeline
public class OnlineCounter extends Counter {
    
    public OnlineCounter() throws IOException{

    }
    
    @Override
    public Description getDescription() {
        Description d = super.getDescription();
        d.setTypeAttribute("online counter");
	    
        return d;
    }


    @Override
    public synchronized void signalSuperior(AbstractModelThread thread, Label threadLabel, PipelineSignal signal) throws IOException{
        if(signal instanceof BagOfMeaningRepresentations){ //empty bag-of-MRs with header
            signalPipeline(threadLabel, signal);
            
        }else if(signal instanceof AbstractVector){ //label: context window, target word
			//remove target word from thread label and signal it separately to pipeline 
			Label contextWindowLabel = new Label(threadLabel.getObject(0));
			Word targetWord = (Word) threadLabel.getObject(1);
			signalPipeline(contextWindowLabel, targetWord);
            signalPipeline(contextWindowLabel, (AbstractVector) signal);
		}
		
		super.signalSuperior(thread, threadLabel, signal);
    }
	
	@Override
	public void signalModel(Label label, PipelineSignal signal) throws IOException{
        if(signal instanceof Document){ //label: corpus file
            signalThread(label, signal);
		}
		
		super.signalModel(label, signal);
	}

    @Override
    protected AbstractModelThread createThread(Label threadLabel) {
        BlockingQueue<PipelineSignal> signalQueue = new LinkedBlockingDeque<>();
        OnlineCounterThread thread = new OnlineCounterThread(this, threadLabel, signalQueue, dimensionality, areCountVectorsSparse);
        
        return thread;
    }
    
    @Override
    protected void startModel() throws IOException{
        StartSignal startSignal = new StartSignal();
        for(ContextWindow contextWindow : contextWindows){
			Label newLabel = new Label(contextWindow);
            signalPipeline(newLabel, startSignal);
        }
        allowSuccessorToFinish();
    }

    @Override
    protected void finishModel() throws IOException{
        FinishSignal finishSignal = new FinishSignal();
        for(ContextWindow contextWindow : contextWindows){
			Label newLabel = new Label(contextWindow);
            signalPipeline(newLabel, finishSignal);
        }
    }

    @Override
    public String protocol() {
        return "INPUT:\n" +
            "label: master label, signal: start signal\n" +
			"for each corpus file in corpus folder:\n" +
			"  label: corpus file, signal: start signal\n" +
			"  for each document in corpus file:\n" +
			"    label: corpus file, signal: document\n\n" +
            
            "OUTPUT:\n" +
            "label: master label, signal: start signal\n" +
			"for each context window:\n" +
			"  label: context window, signal: start signal\n" +
            "  label: context window, signal: empty bag-of-MRs with header\n" +
			"  for each target word:\n" +
			"    for each occurence of target word within context window in document:" +
			"      label: context window, signal: target word\n" +
            "      label: context window, signal: count vector\n" +
			"    label: context window, target word, signal: finish signal";
    }

}



class OnlineCounterThread extends AbstractCounterThread{
    
    private BagOfMeaningRepresentations emptyBag;

	public OnlineCounterThread(OnlineCounter superior, Label threadLabel, BlockingQueue<PipelineSignal> signalQueue, int dimensionality, boolean areCountVectorsSparse){
		super(superior, threadLabel, signalQueue, dimensionality, areCountVectorsSparse);
        
        emptyBag = new BagOfMeaningRepresentations(threadLabel);
	}
    
    private void count(ParsedSentence parsedSentence, TreeMap<Label, IntegerVector> labelTargetWordCountVectorMap) {
		
        //go through all context elements
		for(LanguagePattern contextElement : AbstractExperiment.getContextElements()){
			contextElement.increaseContextElementCountByOne();
			int dimension = contextElement.languagePatternIndex;
			//compute target word indices for all context windows simultaneously
			AbstractSetOfIntegers[] targetWordIndicesArray = contextElement.getTargetWordIndices(parsedSentence, contextWindows);
			//go through all findings per context window
			for(int cw=0; cw<contextWindows.size(); cw++){
				ContextWindow contextWindow = contextWindows.get(cw);
				AbstractSetOfIntegers targetWordIndices = targetWordIndicesArray[cw];
				//update each target word's vector for this context window
				for(Word targetWord : parsedSentence.getWords(targetWordIndices)){
                    if(AbstractExperiment.isTargetWord(targetWord)){
						Label label = new Label(new Comparable[]{ contextWindow, targetWord });
						IntegerVector countVector = labelTargetWordCountVectorMap.get(label);
						if(countVector == null){
                            countVector = IntegerVector.create(areCountVectorsSparse, dimensionality);
                            labelTargetWordCountVectorMap.put(label, countVector);
                        }
						countVector.add(dimension, 1);
					}
				}
			}
		}

    }
    
    @Override
	public void count(Document document) throws IOException{
		//get one count vector per sentence (where sentence internal window is a double unlimited interval)
		SparseIntegerVector[] sentenceCountVectors = getAllSentenceCountVectors(document, contextWindows);
        
        //set the limits for document internal sentence intervals (important for unbounded document internal sentence intervals)
        IntegerInterval documentInternalInterval = new LimitedInterval(0, sentenceCountVectors.length - 1);
        
        //prepare cache maps
        TreeMap<Label, IntegerVector> labelTargetWordCountVectorMap = new TreeMap<>(); //label: context window, target word
        TreeMap<IntegerInterval, IntegerVector> targetSentenceContextCountVectorMap = new TreeMap<>();
        
        //go through all sentences in given document
        for(ParsedSentence targetSentence : document.getParsedSentences()){
            int targetSentenceId = targetSentence.getDocumentInternalSentenceId();
            
            //get target sentence counts and and cache them in label target word count vector map
            count(targetSentence, labelTargetWordCountVectorMap);
            
            //get all sentence counts and cache them per distinct document internal sentence window relative to target sentence (saves a lot of redundant computation)
            for(ContextWindow contextWindow : contextWindows){
                IntegerInterval documentInternalSentenceWindowRelativeToTargetSentence = contextWindow.documentInternalSentenceWindow.shiftBoundariesBy(targetSentenceId).intersect(documentInternalInterval);
                if(!targetSentenceContextCountVectorMap.containsKey(documentInternalSentenceWindowRelativeToTargetSentence)){
                    SparseIntegerVector targetSentenceContextCountVector = new SparseIntegerVector(dimensionality);
                    for(int i=documentInternalSentenceWindowRelativeToTargetSentence.lowerBoundary; i<=documentInternalSentenceWindowRelativeToTargetSentence.upperBoundary; i++){
                        if(i != targetSentenceId){
                            targetSentenceContextCountVector.add(sentenceCountVectors[i]);
                        }
                    }
                    targetSentenceContextCountVectorMap.put(documentInternalSentenceWindowRelativeToTargetSentence, targetSentenceContextCountVector);
                }
            }
            
            //add target word counts and sentence counts in document internal sentence window
            for(Label label : labelTargetWordCountVectorMap.keySet()){ //label: context window, target word
                IntegerVector targetWordCountVector = labelTargetWordCountVectorMap.get(label);
                //add context sentences count vector to target word count vector
                IntegerInterval documentInternalSentenceWindowRelativeToTargetSentence = ((ContextWindow) label.getObject(0)).documentInternalSentenceWindow;
                IntegerVector targetSentenceContextCountVector = targetSentenceContextCountVectorMap.get(documentInternalSentenceWindowRelativeToTargetSentence);
                //signal counts to dependent pipeline nodes
                AbstractVector signalVector = (targetSentenceContextCountVector == null ?
						targetWordCountVector :
						targetWordCountVector.plus(targetSentenceContextCountVector));
                if(emptyBag.getContentDescription() == null){
                    emptyBag.setContentDescription(signalVector.getDescription());
                    superior.signalSuperior(this, threadLabel, emptyBag); //send empty bag-of-MRs with content description just learned from first vector
                }
                superior.signalSuperior(this, label, signalVector); //label: context window, target word
            }
            
            //clear cache because these vectors are only for current target sentence
            labelTargetWordCountVectorMap.clear();
            targetSentenceContextCountVectorMap.clear();
        }
        
    }

    @Override
    public void run() {
        int amountOfDocumentsCounted = 0;
        try{
			while(true){
				PipelineSignal signal = signalQueue.take();
				if(signal instanceof Document){
					//if(amountOfDocumentsCounted > 0 && amountOfDocumentsCounted % 100 == 0) report("has counted " + amountOfDocumentsCounted + " documents..."); //DEBUG
					count((Document) signal);
                    superior.signalSuperior(this, threadLabel, new NotifySignal());
                    amountOfDocumentsCounted++;
				}else if(signal instanceof FinishSignal){
					break;
				}
			}
            
            finishThread();
		}catch(InterruptedException | IOException e){
			e.printStackTrace();
		}
    }
    
}
