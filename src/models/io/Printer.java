package models.io;

import experiment.common.Label;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import meta.Exportable;
import meta.Printable;
import models.AbstractModel;
import models.AbstractModelThread;
import pipeline.signals.FinishSignal;
import pipeline.signals.PipelineSignal;

public class Printer extends AbstractModel{
    
    private boolean prettyPrint;
    
	public Printer(){
        prettyPrint = false;
    }
    
    public Printer(boolean prettyPrint){
        this.prettyPrint = prettyPrint;
    }
    
    public static Printer create(){
        return new Printer();
    }
    
    public static Printer create(boolean prettyPrint){
        return new Printer(prettyPrint);
    }
    
    public static Printer create(String s){
        boolean prettyPrint = s.equals("pretty print");
        return new Printer(prettyPrint);
    }
    
    @Override
    public AbstractModelThread createThread(Label threadLabel) throws IOException{ //label: context window
        BlockingQueue<PipelineSignal> signalQueue = new LinkedBlockingDeque<>();
        return new AbstractModelThread(this, threadLabel, signalQueue) {
            @Override
            public void run() {
                try{
                    while(true){
                        PipelineSignal signal = signalQueue.take();
                        if(signal instanceof FinishSignal){
                            break;
                        }
                    }

                    finishThread();
                }catch(InterruptedException | IOException e){
                    e.printStackTrace();
                }
            }
        };
    }
    
    @Override
    public synchronized void signalSuperior(AbstractModelThread thread, Label threadLabel, PipelineSignal signal) throws IOException{ //signal from subordinate thread
        if(!(signal instanceof FinishSignal)){
            signalPipeline(threadLabel, signal);
        }
        
        super.signalSuperior(thread, threadLabel, signal);
    }

	@Override
	public void signalModel(Label label, PipelineSignal signal) throws IOException{
        if(signal instanceof FinishSignal){
            signalThread(label, signal);
        }
        
		if(signal instanceof Exportable){ //label: context window, signal: description or target word or meaning representation
			//report("Label: " + label.toString() + "\n" + signal.toString());
            report(((Printable) signal).asString(null));
		}
        
        super.signalModel(label, signal);
	}

    @Override
    protected void startModel() {
        report("Ready to print objects...");
    }

    @Override
    protected void finishModel() throws IOException{
        allowSuccessorToFinish();
        report("...Finished printing objects");
    }

    @Override
    public String protocol() {
        return "INPUT:\n" +
            "label: master label, signal: start signal\n" +
            "while true:\n" +
			"  label: anything, signal: any exportable\n\n" +
            
            "OUTPUT:\n" +
            "label: master label, signal: start signal\n" +
            "while true:\n" +
			"  label: anything, signal: any exportable";            
    }
    
}