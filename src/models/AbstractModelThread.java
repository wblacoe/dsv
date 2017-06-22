package models;

import experiment.common.Label;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import meta.Printable;
import pipeline.signals.FinishSignal;
import pipeline.signals.PipelineSignal;

public abstract class AbstractModelThread implements Runnable, Printable{

    protected AbstractModel superior;
	protected Label threadLabel;
	protected BlockingQueue<PipelineSignal> signalQueue;
    
    public AbstractModelThread(AbstractModel superior, Label threadLabel, BlockingQueue<PipelineSignal> signalQueue){
        this.superior = superior;
		this.threadLabel = threadLabel;
		this.signalQueue = signalQueue;
    }
    
    public AbstractModelThread(AbstractModel superior, Label threadLabel){
        this(superior, threadLabel, null);
    }
    
    public void report(String label, String message){
        System.out.println("[" + this.getClass().getSimpleName() + "] (" + label + ") " + message);
    }
    
    public BlockingQueue<PipelineSignal> getSignalQueue(){
        return signalQueue;
    }
    
    protected void signalSuperior(Label label, PipelineSignal signal) throws IOException{
        superior.signalSuperior(this, threadLabel, signal);
    }
    
    public synchronized void notifyThread(){
        notify();
    }
    
    protected void finishThread() throws IOException{
        signalSuperior(threadLabel, new FinishSignal());
    }
    
    @Override
    public String asString(Object o){
        return threadLabel.asString(o);
    }
    
}