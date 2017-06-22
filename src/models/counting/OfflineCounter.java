package models.counting;

import models.AbstractModelThread;
import experiment.AbstractExperiment;
import experiment.common.ContextWindow;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import lingunit.language.Document;
import experiment.common.Description;
import experiment.common.Label;
import integerset.AbstractSetOfIntegers;
import integerset.interval.EmptyInterval;
import integerset.interval.IntegerInterval;
import integerset.interval.LimitedInterval;
import lingunit.flattext.Word;
import lingunit.language.ParsedSentence;
import meta.ExportableAndComparable;
import pattern.language.LanguagePattern;
import pipeline.signals.FinishSignal;
import pipeline.signals.NotifySignal;
import pipeline.signals.PipelineSignal;
import pipeline.signals.StartSignal;
import vector.AbstractVector;
import vector.IntegerVector;
import vector.SparseIntegerVector;
import vector.complex.BagOfMeaningRepresentations;
import vector.complex.MeaningRepresentation;

//first gathers all count vectors, then signals their sum to the pipeline
public class OfflineCounter extends Counter {
    
    private BagOfMeaningRepresentations vectors;
    //private Description contentDesciption;
    
    public OfflineCounter() throws IOException{
        vectors = null; //entries.keys=label: context window, target word; entries.values=AbstractVector
    }
    
    @Override
    public Description getDescription() {
        Description d = super.getDescription();
        d.setTypeAttribute("offline counter");
        
        return d;
    }

	@Override
	public synchronized void signalSuperior(AbstractModelThread thread, Label label, PipelineSignal signal) throws IOException{
		
        if(signal instanceof BagOfMeaningRepresentations){ //label: corpus file
			BagOfMeaningRepresentations vectorsFromThread = (BagOfMeaningRepresentations) signal;
			if(vectors == null){
				vectors = new BagOfMeaningRepresentations(Label.MASTER_LABEL, vectorsFromThread.getContentDescription());
			}
            for(Entry<ExportableAndComparable, MeaningRepresentation> entry : vectorsFromThread.entrySet()){
				Label contextWindowAndTargetWordLabel = (Label) entry.getKey();
				AbstractVector vectorToBeAdded = (AbstractVector) entry.getValue();
				//contentDesciption = vectorToBeAdded.getDescription();
                AbstractVector existingVector = (AbstractVector) vectors.get(contextWindowAndTargetWordLabel);
                if(existingVector == null){
                    vectors.put(contextWindowAndTargetWordLabel, vectorToBeAdded);
                }else{
                    existingVector.add(vectorToBeAdded);
                }
            }
        }
		
		super.signalSuperior(thread, label, signal);
        
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
        OfflineCounterThread thread = new OfflineCounterThread(this, threadLabel, signalQueue, dimensionality, areCountVectorsSparse);
        
        return thread;
    }
    
    @Override
    protected void startModel() {
        report("Ready...");
    }

    @Override
    protected void finishModel() throws IOException{
		report("...Finished counter");
		
        //signal one start signal per context window to pipeline
        for(ContextWindow contextWindow : contextWindows){
            //Label contextWindowPrototypeDescriptionLabel = new Label(new Comparable[]{ contextWindow, prototype.getDescription() });
            //signalPipeline(contextWindowPrototypeDescriptionLabel, new StartSignal());
			Label labelWithoutTargetWord = new Label(contextWindow);
            signalPipeline(labelWithoutTargetWord, new StartSignal());
			//signalPipeline(labelWithoutTargetWord, contentDesciption);
			BagOfMeaningRepresentations contextWindowDependentBag = new BagOfMeaningRepresentations(labelWithoutTargetWord, vectors.getContentDescription());
            signalPipeline(labelWithoutTargetWord, contextWindowDependentBag);
        }
        //signal all vectors to pipeline
        for(Entry<ExportableAndComparable, MeaningRepresentation> entry : vectors.entrySet()){ //entry.key=label: context window, target word
			Label contextWindowLabel = (Label) ((Label) entry.getKey()).getCopy();
			Word targetWord = (Word) contextWindowLabel.removeFinalObject();
			AbstractVector countVector = (AbstractVector) entry.getValue();
            signalPipeline(contextWindowLabel, targetWord);
			signalPipeline(contextWindowLabel, countVector);
        }
        //signal finish signal for all context windows to pipeline
        for(ContextWindow contextWindow : contextWindows){
            Label labelWithoutTargetWord = new Label(contextWindow);
            signalPipeline(labelWithoutTargetWord, new FinishSignal());
        }
        
        allowSuccessorToFinish();
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
            "    label: context window, signal: target word\n" +
			"    label: context window, signal: count vector\n" +
			"  label: context window, signal: finish signal";
    }

}



class OfflineCounterThread extends AbstractCounterThread{

	private BagOfMeaningRepresentations vectors; //label: context window, target word
	private int amountOfContextWindows;
	
	public OfflineCounterThread(OfflineCounter superior, Label threadLabel, BlockingQueue<PipelineSignal> signalQueue, int dimensionality, boolean areCountVectorsSparse){
		super(superior, threadLabel, signalQueue, dimensionality, areCountVectorsSparse);
		
		vectors = new BagOfMeaningRepresentations(threadLabel);
		amountOfContextWindows = contextWindows.size();
	}
	
	private void cacheVector(Label label, AbstractVector vector){
		if(vectors.getContentDescription() == null){
			vectors.setContentDescription(vector.getDescription());
		}
		vectors.put(label, vector);
	}
	
	private void prepareCountsForTargetSentence(ParsedSentence targetSentence){
        //go through all context elements
		for(LanguagePattern contextElement : AbstractExperiment.getContextElements()){
			contextElement.increaseContextElementCountByOne();
			int dimension = contextElement.languagePatternIndex;
            //compute target word indices for all context windows simultaneously
			AbstractSetOfIntegers[] targetWordIndicesArray = contextElement.getTargetWordIndices(targetSentence, contextWindows);
			//go through all findings per context window
			for(int cw=0; cw<amountOfContextWindows; cw++){
                ContextWindow contextWindow = contextWindows.get(cw);
				AbstractSetOfIntegers targetWordIndices = targetWordIndicesArray[cw];
				//update each target word's vector for this context window
            	for(Word targetWord : targetSentence.getWords(targetWordIndices)){
                    if(AbstractExperiment.isTargetWord(targetWord)){
                        Label label = new Label(new Comparable[]{ contextWindow, targetWord });
                        //IntegerVector targetWordCountVector = (IntegerVector) targetWord.bagOfMeaningRepresentations.get(label);
                        IntegerVector targetWordCountVector = (IntegerVector) vectors.get(label);
                        if(targetWordCountVector == null){
							targetWordCountVector = IntegerVector.create(areCountVectorsSparse, dimensionality);
							//targetWord.bagOfMeaningRepresentations.put(label, targetWordCountVector);
                            cacheVector(label, targetWordCountVector);
						}
						targetWordCountVector.add(dimension, 1);
					}
				}
			}
		}
    }
	
	@Override
	public void count(Document document){
		//get one count vector per sentence (where sentence internal window is a double unlimited interval)
		SparseIntegerVector[] sentenceCountVectors = getAllSentenceCountVectors(document, fullTargetSentenceWindowAsBag);
		
		//gather counts for all target words and all context elements (pretending like document internal sentence window is [0, 0], i.e. count only target sentence)
		for(ParsedSentence targetSentence : document.getParsedSentences()){
			prepareCountsForTargetSentence(targetSentence);
		}
        
        //set the limits for document internal sentence intervals (important for unbounded document internal sentence intervals)
        IntegerInterval documentInternalInterval = new LimitedInterval(0, sentenceCountVectors.length - 1);
		
		//add sentence count vectors to gathered counts for all non-target sentences in document internal sentence window
		for(ParsedSentence targetSentence : document.getParsedSentences()){
            int targetSentenceId = targetSentence.getDocumentInternalSentenceId();
			for(Word targetWord : targetSentence.getAllWords()){
				if(AbstractExperiment.isTargetWord(targetWord)){
					for(ContextWindow contextWindow : contextWindows){
						if(!(contextWindow.documentInternalSentenceWindow instanceof EmptyInterval) && !contextWindow.documentInternalSentenceWindow.equals(targetSentenceWindow)){
							//prepare count vector for target word + context window
							Label label = new Label(new Comparable[]{ contextWindow, targetWord });
							//IntegerVector targetWordCountVector = (IntegerVector) targetWord.bagOfMeaningRepresentations.get(label);
                            IntegerVector targetWordCountVector = (IntegerVector) vectors.get(label);
							if(targetWordCountVector == null){
								targetWordCountVector = IntegerVector.create(areCountVectorsSparse, dimensionality);
								//targetWord.bagOfMeaningRepresentations.put(label, targetWordCountVector);
                                cacheVector(label, targetWordCountVector);
							}
							//add all non-target sentences' count vectors to this target word count vector
							IntegerInterval documentInternalSentenceWindowRelativeToTargetSentence = contextWindow.documentInternalSentenceWindow.shiftBoundariesBy(targetSentenceId).intersect(documentInternalInterval);
							for(int i = documentInternalSentenceWindowRelativeToTargetSentence.lowerBoundary; i <= documentInternalSentenceWindowRelativeToTargetSentence.upperBoundary; i++){
								if(i != targetSentenceId){ //target sentence has already been counted
                                    targetWordCountVector.add(sentenceCountVectors[i]);
                                }
							}
						}
					}
				}
			}
		}
		
    }

	
	@Override
	public void run() {
        int amountOfDocumentsCounted = 0;
		try{
			while(true){
				PipelineSignal signal = signalQueue.take();
				//report("Received signal: " + signal + "...");
				if(signal instanceof Document){
                    //if(amountOfDocumentsCounted > 0 && amountOfDocumentsCounted % 100 == 0) report( "has counted " + amountOfDocumentsCounted + " documents..."); //DEBUG
					count((Document) signal);
                    superior.signalSuperior(this, threadLabel, new NotifySignal());
                    amountOfDocumentsCounted++;
				}else if(signal instanceof FinishSignal){
					break;
				}
			}
            
            //report("...Finished after counting " + amountOfDocumentsCounted + " documents");
            signalSuperior(threadLabel, vectors);
            finishThread();
        }catch(InterruptedException | IOException e){
			e.printStackTrace();
		}

	}

}
