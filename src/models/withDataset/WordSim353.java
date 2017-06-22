package models.withDataset;

import experiment.common.AbstractInstance;
import experiment.common.Dataset;
import experiment.common.ExperimentWithDataset;
import experiment.common.Label;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import lingunit.flattext.Word;
import meta.ExportableAndComparable;
import meta.Helper;
import models.AbstractModel;
import models.AbstractModelThread;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import pipeline.signals.FinishSignal;
import pipeline.signals.PipelineSignal;
import vector.complex.BagOfMeaningRepresentations;
import vector.complex.MeaningRepresentation;
import vocabulary.Vocabulary;

public class WordSim353 extends AbstractModel implements ExperimentWithDataset {
    
    private File datasetFile;
    private Dataset dataset;
    private SimilarityFunction similarityFunction;
    
    public WordSim353(File datasetFile, SimilarityFunction similarityFunction) throws IOException{
        this.datasetFile = datasetFile;
        dataset = new Dataset();
        this.similarityFunction = similarityFunction;
        
        importDataset();
    }
    
    public static WordSim353 create(File datasetFile, SimilarityFunction similarityFunction) throws IOException{
        return new WordSim353(datasetFile, similarityFunction);
    }
    
    @Override
    public void importDataset() throws IOException{
        BufferedReader in = Helper.getFileReader(datasetFile);
        
        String line = in.readLine(); //skip first row
        int i=0;
        while((line = in.readLine()) != null){
            String[] entries = line.split("\t");
            if(!line.isEmpty() && entries.length == 3){
                String word1 = entries[0].toLowerCase();
                String word2 = entries[1].toLowerCase();
                float score = Float.parseFloat(entries[2]);

                AbstractInstance instance = new WordSim353Instance(i, word1, word2, score);
                dataset.addInstance(instance);
                i++;
            }
        }
        
        in.close();
    }
    
    @Override
    protected AbstractModelThread createThread(Label threadLabel){ //label: context window

        //Label contextWindowLabel = (Label) threadLabel.getCopy();
        //Description contentDescription = (Description) contextWindowLabel.removeFinalObject();
        //BagOfMeaningRepresentations bag = new BagOfMeaningRepresentations(contextWindowLabel, contentDescription);
        
        BlockingQueue<PipelineSignal> signalQueue = new LinkedBlockingDeque<>();
        //signalQueue.add(bag);
        
        return new WordSim353Thread(
            this,
            threadLabel,
            signalQueue,
            dataset,
            (SimilarityFunction) similarityFunction.getCopy()
        );
    }
    
    @Override
    public synchronized void signalSuperior(AbstractModelThread thread, Label threadLabel, PipelineSignal signal) throws IOException{ //signal from subordinate thread
        if(!(signal instanceof FinishSignal)){
            signalPipeline(threadLabel, signal);
        }
        
        super.signalSuperior(thread, threadLabel, signal);
    }
    
    @Override
    protected void startModel(){
        report("Ready...");
    }
    
    @Override
    protected void finishModel() throws IOException{
        allowSuccessorToFinish();
        report("...Finished evaluating data under all " + amountOfFinishedThreads + " labels"); //DEBUG
    }
    
    @Override
    public void signalModel(Label label, PipelineSignal signal) throws IOException{
        //flush all signals through to pipeline
        if(!(signal instanceof FinishSignal)){
            signalThread(label, signal);
            signalPipeline(label, signal);
        }
        
        super.signalModel(label, signal);
    }

    @Override
    public String protocol() {
        return "INPUT:\n" +
            "label: master label, signal: start signale\n" +
			"for each context window in experiment:\n" +
			"  label: context window, signal: start signal\n" +
			"  for each target word:\n" +
			"    label: context window, signal: target word\n" +
            "    label: context window, signal: meaning representation\n" +
			"  label: context window, target word, signal: finish signal\n" +
            
            "OUTPUT:\n" +
            "label: master label, signal: start signal";
    }

}


class WordSim353Thread extends AbstractModelThread{

    private Dataset dataset;
	private BagOfMeaningRepresentations targetWordVectors;
	private SpearmansCorrelation spearman;
    //private SimilarityCache similarityCache;
    private SimilarityFunction similarityFunction;
	
	public WordSim353Thread(WordSim353 superior, Label threadLabel, BlockingQueue<PipelineSignal> signalQueue, Dataset dataset, SimilarityFunction similarityFunction){
		super(superior, threadLabel, signalQueue);
        
        this.dataset = dataset;
        targetWordVectors = new BagOfMeaningRepresentations(threadLabel); //label: context window
		spearman = new SpearmansCorrelation();
        this.similarityFunction = similarityFunction;
	}
	
	//assumes that all target word vectors are in data structure
	private double evaluate() throws IOException{
        //report("Evaluating data...");
        
        ArrayList<Float> expectedList = new ArrayList<>();
        ArrayList<Float> predictedList = new ArrayList<>();
        
        for(Entry<Integer, AbstractInstance> entry : dataset.entrySet()){
            WordSim353Instance instance = (WordSim353Instance) entry.getValue();
            //String wordPair = (instance.word1.compareTo(instance.word2) < 0 ? instance.word1 + "\t" + instance.word2 : instance.word2 + "\t" + instance.word1);
            
            Word word1 = Vocabulary.getWord(instance.word1);
            Word word2 = Vocabulary.getWord(instance.word2);
            MeaningRepresentation mr1 = targetWordVectors.get(word1);
            MeaningRepresentation mr2 = targetWordVectors.get(word2);
            
            Float wordSimilarity;
            if(mr1 == null){
                //report("skipping datapoint #" + instance.index + ": no representation for \"" + instance.word1 + "\"...");
            }else if(mr2 == null){
                //report("skipping datapoint #" + instance.index + ": no representation for \"" + instance.word2 + "\"...");
            }else{
                wordSimilarity = similarityFunction.apply(mr1, mr2);
                if(wordSimilarity != null && Double.isFinite(wordSimilarity) && !Double.isNaN(wordSimilarity)){
                    //report("sim(" + word1.asString(null) + ", " + word2.asString(null) + ") = " + wordSimilarity); //DEBUG
                    instance.prediction = wordSimilarity;
                    expectedList.add(instance.score);
                    predictedList.add(wordSimilarity);
                }
            }
        }
        
        double[] expectedArray = new double[expectedList.size()];
        for(int i=0; i<expectedList.size(); i++) expectedArray[i] = 1.0 * expectedList.get(i);
        double[] predictedArray = new double[predictedList.size()];
        for(int i=0; i<predictedList.size(); i++) predictedArray[i] = 1.0 * predictedList.get(i);
        
        double correlation = spearman.correlation(expectedArray, predictedArray);
        
        //report("Spearman correlation is " + (float) correlation);
        return correlation;
	}
    
    @Override
	public void run() {
		
		Word targetWord = null;
		try{
			while(true){
				PipelineSignal signal = signalQueue.take();
				
				/*if(signal instanceof BagOfMeaningRepresentations){
					targetWordVectors = (BagOfMeaningRepresentations) signal;
					
                }else if(signal instanceof Description){
                    Description contentDescription = (Description) signal;
                    if(targetWordVectors == null){
                        targetWordVectors = new BagOfMeaningRepresentations
                    }
                    targetWordVectors.setContentDescription(contentDescription);
                    
				}else*/ if(signal instanceof Word){
					targetWord = (Word) signal;
					
				}else if(signal instanceof MeaningRepresentation){
					MeaningRepresentation mr = (MeaningRepresentation) signal;
					targetWordVectors.put(targetWord, mr);
					
				}else if(signal instanceof FinishSignal){
					double correlation = evaluate();
                    report(targetWordVectors.getLabel().toString(), "Spearman correlation: " + (float) correlation);
					break;
				}
				
			}
            
            for(ExportableAndComparable key : targetWordVectors.keySet()){
                signalSuperior(threadLabel, (Word) key);
                signalSuperior(threadLabel, targetWordVectors.get(key));
            }
            
            finishThread();
		}catch(InterruptedException | IOException e){
			e.printStackTrace();
		}
	}
    
}



class WordSim353Instance extends AbstractInstance{

    public String word1, word2;
    public float score, prediction;
    
    public WordSim353Instance(int index, String word1, String word2, float score){
        super(index);
        
        this.word1 = word1;
        this.word2 = word2;
        this.score = score;
        prediction = -1;
    }
    
}