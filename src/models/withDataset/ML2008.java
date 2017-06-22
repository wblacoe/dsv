package models.withDataset;

import experiment.common.AbstractInstance;
import experiment.common.Dataset;
import experiment.common.ExperimentWithDataset;
import experiment.common.Label;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;
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

//Mitchell and Lapata 2008
public class ML2008 extends AbstractModel implements ExperimentWithDataset{
    
    private File datasetFile;
    private Dataset dataset;
    private SimilarityFunction similarityFunction;
    private CompositionFunction compositionFunction;
    
    public ML2008(File datasetFile, SimilarityFunction similarityFunction, CompositionFunction compositionFunction) throws IOException{
        this.datasetFile = datasetFile;
        dataset = new Dataset();
        this.similarityFunction = similarityFunction;
        this.compositionFunction = compositionFunction;
        
        importDataset();
    }
    
    public static ML2008 create(File datasetFile, SimilarityFunction similarityFunction, CompositionFunction compositionFunction) throws IOException{
        return new ML2008(datasetFile, similarityFunction, compositionFunction);
    }
    
    @Override
    public void importDataset() throws IOException{
        BufferedReader in = Helper.getFileReader(datasetFile);
        
        String line = in.readLine(); //skip first row
        int i=0;
        while((line = in.readLine()) != null){
            String[] entries = line.split("\t");
            String participant = entries[0].toLowerCase();
            String verb = entries[1].toLowerCase();
            String noun = entries[2].toLowerCase();
            String landmark = entries[3].toLowerCase();
            int input = Integer.parseInt(entries[4]);
            String hilo = entries[5].toLowerCase();
            
            ML2008Instance instance = new ML2008Instance(i, participant, verb, noun, landmark, input, hilo);
            dataset.addInstance(instance);
            i++;
        }
        
        in.close();
    }
    
    @Override
    protected AbstractModelThread createThread(Label threadLabel){ //label: context window
        BlockingQueue<PipelineSignal> signalQueue = new LinkedBlockingDeque<>();
        
        return new ML2008Thread(
            this,
            threadLabel,
            signalQueue,
            dataset,
            (SimilarityFunction) similarityFunction.getCopy(),
            (CompositionFunction) compositionFunction.getCopy()
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


class ML2008Thread extends AbstractModelThread{

    private Dataset dataset;
	private BagOfMeaningRepresentations targetWordVectors;
	private SpearmansCorrelation spearman;
    //private SimilarityCache similarityCache;
    private SimilarityFunction similarityFunction;
    private CompositionFunction compositionFunction;
	
	public ML2008Thread(ML2008 superior, Label threadLabel, BlockingQueue<PipelineSignal> signalQueue, Dataset dataset, SimilarityFunction similarityFunction, CompositionFunction compositionFunction){
		super(superior, threadLabel, signalQueue);
        
        this.dataset = dataset;
        targetWordVectors = new BagOfMeaningRepresentations(threadLabel); //label: context window
		spearman = new SpearmansCorrelation();
        //similarityCache = new SimilarityCache(new SimilarityFunction(SimilarityFunction.COSINE));
        this.similarityFunction = similarityFunction;
        this.compositionFunction = compositionFunction;
        
	}
    
    private MeaningRepresentation compose(MeaningRepresentation mr1, MeaningRepresentation mr2){
        MeaningRepresentation r = null;
        
        if(mr1 != null && mr2 != null){
            r = compositionFunction.apply(mr1, mr2);
        }
        
        return r;
    }
    
    private MeaningRepresentation[] compose(String[][] words){
        int amountOfPhrases = words.length;
        MeaningRepresentation[] mrs = new MeaningRepresentation[amountOfPhrases];
        
        for(int phrase=0; phrase<amountOfPhrases; phrase++){
            MeaningRepresentation phraseMr = null;
            
            Word word1 = Vocabulary.getWord(words[phrase][0]);
            Word word2 = Vocabulary.getWord(words[phrase][1]);
            MeaningRepresentation mr1 = targetWordVectors.get(word1);
            MeaningRepresentation mr2 = targetWordVectors.get(word2);
            
            if(mr1 != null && mr2 != null) phraseMr = compose(mr1, mr2);
            mrs[phrase] = phraseMr;
        }
        
        return mrs;
    }
	
	//assumes that all target word vectors are in data structure
	private double evaluate(){
		//System.out.println(targetWordVectors.toString());
        //report("Evaluating data...");
        
        double[] values1 = new double[dataset.size()];
        double[] values2 = new double[dataset.size()];
        
        int i=0;
        for(Map.Entry<Integer, AbstractInstance> entry : dataset.entrySet()){
            ML2008Instance instance = (ML2008Instance) entry.getValue();
            
            String[][] words = new String[][]{
                new String[]{ instance.noun, instance.verb },
                new String[]{ instance.landmark, instance.verb }
            };
            MeaningRepresentation[] phraseMrs = compose(words);
            
            Float phraseSimilarity = 0f;
            if(phraseMrs[0] != null && phraseMrs[1] != null){
                phraseSimilarity = similarityFunction.apply(phraseMrs[0], phraseMrs[1]);
            }
            
            instance.prediction = phraseSimilarity;
            values1[i] = instance.input;
            values2[i] = phraseSimilarity;
            i++;
        }
        
        double correlation = spearman.correlation(values1, values2);
        
        //report("Spearman correlation is " + (float) correlation + " for label " + threadLabel);
        
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



class ML2008Instance extends AbstractInstance{

    public String participant, verb, noun, landmark, hilo;
    public int input;
    public float prediction;
    
    public ML2008Instance(int index, String participant, String verb, String noun, String landmark, int input, String hilo){
        super(index);
        
        this.participant = participant;
        this.verb = verb;
        this.noun = noun;
        this.landmark = landmark;
        this.input = input;
        prediction = -1;
    }
    
}