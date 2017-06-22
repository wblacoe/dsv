package models.io;

import experiment.common.ContextWindow;
import experiment.common.Label;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import lingunit.flattext.Word;
import meta.ExportableAndComparable;
import meta.Helper;
import models.AbstractModel;
import models.AbstractModelThread;
import pipeline.signals.FinishSignal;
import pipeline.signals.PipelineSignal;
import pipeline.signals.StartSignal;
import vector.complex.BagOfMeaningRepresentations;
import vector.complex.MeaningRepresentation;

public class Importer extends AbstractModel {
    
    private File folder;
    private File[] files;
    
    public Importer(File folder){
        if(folder.exists()){
            this.folder = folder;
            files = folder.listFiles();
        }else{
            files = new File[0];
            report("Folder " + folder.getAbsolutePath() + " does not exist!");
        }
    }
    
    public static Importer create(File folder){
        return new Importer(folder);
    }

    
	@Override
	public synchronized void signalSuperior(AbstractModelThread thread, Label label, PipelineSignal signal) throws IOException{
        
        super.signalSuperior(thread, label, signal);
        if(amountOfFinishedThreads >= files.length) allowSuccessorToFinish();
        
		if(signal instanceof BagOfMeaningRepresentations){ //label: bag of MR file
			BagOfMeaningRepresentations bag = (BagOfMeaningRepresentations) signal;
            Label bagLabel = bag.getLabel(); 
			ContextWindow contextWindow = (ContextWindow) bagLabel.getObject(0);
            
			//header of bag
			//Label contextWindowBagDescriptionLabel = new Label(new Comparable[]{ contextWindow, bag.getDescription()});
			//signalPipeline(contextWindowBagDescriptionLabel, new StartSignal());

            Label contextWindowLabel = new Label(contextWindow);
            signalPipeline(contextWindowLabel, new StartSignal());
            signalPipeline(contextWindowLabel, bag.getContentDescription());
            
            //signal to pipeline: words and their meaning representation
            for(Entry<ExportableAndComparable, MeaningRepresentation> entry : bag.entrySet()){
                Word targetWord = (Word) entry.getKey();
                signalPipeline(contextWindowLabel, targetWord);
                MeaningRepresentation mr = entry.getValue();
                signalPipeline(contextWindowLabel, mr);
            }
                        
            signalPipeline(contextWindowLabel, new FinishSignal());
		}
	}
    
    @Override
    protected AbstractModelThread createThread(Label threadLabel) { //label: file
        File file = (File) threadLabel.getObject(0);
        ImporterThread thread = new ImporterThread(this, threadLabel, file);
        
        return thread;
    }
    
    @Override
    protected void startModel() throws IOException{
        report("Importing bags of meaning representations from " + folder.getAbsolutePath());

        for(File file : files){
            Label fileLabel = new Label(file);
            startThread(fileLabel);
        }
    }

    @Override
    protected void finishModel() {
        report("...Finished importing " + amountOfFinishedThreads + " bags of meaning representations from " + folder.getAbsolutePath());
    }

    @Override
    public String protocol() {
        return "INPUT:\n" +
            "label: master label, signal: start signal\n\n" +
            
            "OUTPUT:\n" +
            "label: master label, signal: start signal\n\n" +
            "for each bag-of-MR file (context window in file header):\n" +
            "  label: context window, signal: start signal\n" +
            "  label: context window, signal: description in bag-of-MR\n" +
            "  for each target word:\n" +
            "    label: context window, signal: target word\n" +
            "    label: context window, signal: meaning representation\n" +
            "  label: context window, signal: finish signal";
    }
	
}



class ImporterThread extends AbstractModelThread{

	private final File inputFile;
	
	public ImporterThread(Importer superior, Label threadLabel, File inputFile){
		super(superior, threadLabel);
        
		this.inputFile = inputFile;
	}
	
	@Override
	public void run() {
		report(inputFile.getName(), "Importing meaning representations from " + inputFile.getName() + "...");
		
		BagOfMeaningRepresentations bag = null;
        try(BufferedReader reader = Helper.getFileReader(inputFile)) {
            bag = BagOfMeaningRepresentations.importFrom(reader);
            
            report(inputFile.getName(), "...Finished importing " + (bag == null ? "0" : "" + bag.size()) + " meaning representations from " + inputFile.getName());
		
            signalSuperior(threadLabel, bag);
            finishThread();
        }catch(IOException e){
			e.printStackTrace();
		}
	}

}
