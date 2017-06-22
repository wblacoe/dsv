package experiment;

import experiment.common.BagOfContextWindows;
import experiment.common.BagOfTargetElements;
import experiment.common.ContextWindow;
import experiment.common.Description;
import experiment.common.Parameters;
import experiment.common.ThreadsManager;
import java.io.File;
import meta.Helper;
import java.io.IOException;
import lingunit.flattext.Word;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import models.AbstractModel;
import pattern.language.BagOfLanguagePatterns;
import pattern.language.LanguagePattern;
import pipeline.Pipeline;
import vector.complex.DistributionOfVectors;
import vocabulary.Vocabulary;

public abstract class AbstractExperiment {

	private static BagOfTargetElements targetWords;
	private static BagOfLanguagePatterns contextElements;
    private static BagOfContextWindows contextWindows;
    private static Pipeline pipeline;
    
    private static ThreadsManager threadsManager;
    private static BlockingQueue<Runnable> startThreadQueue, finishThreadQueue;
    
    public static int vectorDistributionSimilarityFunction = 0;
    
    
    public static synchronized BagOfTargetElements getTargetWords(){
        return targetWords;
    }
	
	public static boolean isTargetWord(Word w){
		return targetWords.contains(w);
	}
    
    public static synchronized BagOfLanguagePatterns getContextElements(){
        return contextElements;
    }
    
    public static synchronized LanguagePattern getContextElement(int languagePatternIndex){
        return contextElements.get(languagePatternIndex);
    }
    
    public static synchronized BagOfContextWindows getContextWindows(){
        return contextWindows;
    }
	
	public static synchronized ContextWindow getContextWindow1(int index){
        return contextWindows.get(index);
    }
    
    public static synchronized int getDimensionality(){
        return contextElements.size();
    }
	
	public static void prepareExperiment() throws IOException{
		Helper.developer = true;
		
		//prepare vocabulary
		File vocabularyFile;
        if(Parameters.hasParameter("vocabulary file") && (vocabularyFile = Parameters.getFileParameter("vocabulary file")).exists()){
            //import an existing vocabulary
            Vocabulary.importFrom(vocabularyFile);
        }else{
            //use a fresh and empty vocabulary
			System.out.println("[Vocabulary] Using a new empty vocabulary");
            int wordForm = Word.LEMMA; //default word form
			if(Parameters.hasParameter("word form")) wordForm = Parameters.getConstantParameter("word form", Word.WORD_FORMS);
            Vocabulary.setWordForm(wordForm);
        }
        
		//prepare target words and their meaning representations
		targetWords = BagOfTargetElements.importFrom(Parameters.getFileParameter("target elements input file"));
		
		//prepare context elements
		File contextElementsFile;
		File contextElementsOutputFile = Parameters.getFileParameter("context elements output file"); //reuse context elements file with counts (previously generated)?
		if(contextElementsOutputFile.exists()){
			contextElementsFile = contextElementsOutputFile;
		}else{
			contextElementsFile = Parameters.getFileParameter("context elements input file"); //use context elements file without counts
		}
		contextElements = BagOfLanguagePatterns.importFrom(contextElementsFile);
        
        //prepare context windows
        contextWindows = BagOfContextWindows.importFrom(Parameters.getFileParameter("context windows input file"));
        
        //prepare threading manager
        startThreadQueue = new LinkedBlockingDeque<>();
        finishThreadQueue = new LinkedBlockingDeque<>();
        int maxAmountOfThreads = Parameters.getIntParamter("amount of threads");
        threadsManager = new ThreadsManager(startThreadQueue, finishThreadQueue, maxAmountOfThreads);
        
        vectorDistributionSimilarityFunction = Parameters.getConstantParameter("vector distribution similarity function", DistributionOfVectors.SIMILARITY_FUNCTIONS);
	}
	
	public static void startPipeline(Pipeline pl) throws IOException{
		//prepare pipeline
        pipeline = pl;
		
        (new Thread(threadsManager)).start();
        pipeline.run();
	}
    
    public static void startExperiment(AbstractModel[] models) throws IOException{
		System.out.println("[Experiment] Starting the experiment...");
        startPipeline(new Pipeline(models));
    }
	
	public static void finishExperiment() throws IOException{
        contextElements.exportTo(Helper.getFileWriter(Parameters.getFileParameter("context elements output file")));
        Vocabulary.exportTo(Parameters.getFileParameter("vocabulary file"), false);
        //Helper.exportToFile((ExportToFileJob) pipeline.exportTo(Parameters.getFileParameter("pipeline output file")));
        pipeline.exportTo(Helper.getFileWriter(Parameters.getFileParameter("pipeline output file")));
		
		System.out.println("[Experiment] ...Finished the experiment");
	}
    
    public static void startThread(Runnable thread){
        startThreadQueue.add(thread);
		notifyThreadsManager();
		//Helper.report("[Experiment] Adding thread " + thread + " to start-thread-queue...");
    }
    
    public static void finishThread(Runnable thread){
        finishThreadQueue.add(thread);
		notifyThreadsManager();
    }
    
    public static void closeThreadsManager(){
        startThread(threadsManager); //this is a workaround for ending the threads manager
        threadsManager.notifyThreadsManager();
    }
	
    public static Description getDescription(){
        Description d = new Description();
        d.setType("experiment");
        d.addChild(Vocabulary.getDescription());
        d.addChild(targetWords.getDescription());
        d.addChild(contextElements.getDescription());
        d.addChild(contextWindows.getDescription());
        d.addChild(pipeline.getDescription());

        return d;
    }
    
	public static synchronized void signalThreadsManagerFinished() throws IOException{
        finishExperiment();
    }
    
    public static void notifyThreadsManager(){
        threadsManager.notifyThreadsManager();
    }
    
    public abstract void prepareParameters(File parametersFile) throws IOException;
    public abstract AbstractModel[] prepareModels() throws IOException;
        
}