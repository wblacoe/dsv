package models.io;

import experiment.common.ContextWindow;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import meta.Exportable;
import experiment.common.Label;
import java.io.BufferedWriter;
import java.io.IOException;
import meta.Helper;
import models.AbstractModel;
import models.AbstractModelThread;
import pipeline.signals.PipelineSignal;
import pipeline.signals.FinishSignal;

public class Exporter extends AbstractModel {
	
	private final File exportFolder;
	
	public Exporter(File exportFolder){
		this.exportFolder = exportFolder;
	}
    
    public static Exporter create(File exportFolder){
        return new Exporter(exportFolder);
    }
    
    @Override
    public AbstractModelThread createThread(Label threadLabel){ //label: context window
        ContextWindow contextWindow = (ContextWindow) threadLabel.getObject(0);
        File exportFile = new File(exportFolder, "" + contextWindow.contextWindowIndex + ".txt");
        
        BlockingQueue<PipelineSignal> signalQueue = new LinkedBlockingDeque<>();
        ExporterThread thread = new ExporterThread(this, threadLabel, exportFile, signalQueue);
        
        return thread;
    }

	@Override
	public void signalModel(Label label, PipelineSignal signal) throws IOException{
        if(!(signal instanceof FinishSignal)){
            signalPipeline(label, signal); //flush every signal through to successor
        }
        
		if(signal instanceof Exportable){ //label: context window, signal: description or target word or meaning representation
			signalThread(label, signal);
		}
        
        super.signalModel(label, signal);
	}

    @Override
    protected void startModel() throws IOException{
        report("Ready...");
        allowSuccessorToFinish();
    }

    @Override
    protected void finishModel() {
        report("...Finished exporting objects to " + amountOfFinishedThreads + " files");
    }

    @Override
    public String protocol() {
        return "INPUT:\n" +
            "label: master label, signal: start signal\n" +
			"for each context window:\n" +
            "  label: context window, signal start signal\n" +
            "  label: context window, signal: empty bag of MRs with a header\n" +
            "  for each target word:" +
            "    label: context window, signal: target word\n" +
            "    label: context window, signal: meaning representation\n" +
            "  label: context window, signal finish signal\n\n" +
            
            "OUTPUT:\n" +
            "label: master label, signal: start signal";
    }
	
}



class ExporterThread extends AbstractModelThread{

	private final File file;
	private int amountOfExportedObjects;
	
	public ExporterThread(Exporter superior, Label threadLabel, File file, BlockingQueue<PipelineSignal> signalQueue){
        super(superior, threadLabel, signalQueue);
        
		this.file = file;
		amountOfExportedObjects = 0;
	}
    
	@Override
	public void run() {
        report(file.getName(), "Exporting objects to file...");
        
		try{
            BufferedWriter writer = Helper.getFileWriter(file);
			while(true){
				PipelineSignal signal = signalQueue.take();
				if(signal instanceof Exportable){
					amountOfExportedObjects++;
					((Exportable) signal).exportTo(writer);
				}else if(signal instanceof FinishSignal){
					break;
				}
			}
			writer.close();
            
            report(file.getName(), "...Finished exporting " + amountOfExportedObjects + " objects");
            finishThread();
		}catch(IOException | InterruptedException e){
			e.printStackTrace();
		}
	}
	
}