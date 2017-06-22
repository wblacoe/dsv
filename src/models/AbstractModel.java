package models;

import experiment.AbstractExperiment;
import meta.Describable;
import experiment.common.Description;
import experiment.common.Label;
import java.io.IOException;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import models.sequentialclustering.sequentialKMeans.SequentialKMeans;
import pipeline.PipelineNode;
import pipeline.signals.FinishSignal;
import pipeline.signals.NotifySignal;
import pipeline.signals.PipelineSignal;
import pipeline.signals.StartSignal;

public abstract class AbstractModel implements Describable{

    private final TreeMap<Label, AbstractModelThread> threads;
	private final TreeMap<Label, BlockingQueue<PipelineSignal>> signalQueues;
	protected int amountOfFinishedThreads;
    protected boolean allowedToFinish;
	private PipelineNode pipelineNode;
	
	public AbstractModel(){
        threads = new TreeMap<>();
		signalQueues = new TreeMap<>();
		amountOfFinishedThreads = 0;
        allowedToFinish = false;
		pipelineNode = null;
	}
    
    public void report(String message){
        System.out.println("[" + this.getClass().getSimpleName() + "] " + message);
    }
    
    public static int getIndex(Object[] array, Object object){
        for(int i=0; i<array.length; i++){
            if(array[i] != null && array[i].equals(object)) return i;
        }
        
        return -1;
    }
    
    public void setPipelineNode(PipelineNode pipelineNode){
        this.pipelineNode = pipelineNode;
    }
    
    public PipelineNode getPipelineNode(){
        return pipelineNode;
    }
    
    @Override
    public Description getDescription(){
        Description d = new Description();
        d.setType("model");
        
        return d;
    }
    
    protected void signalPipeline(Label threadLabel, PipelineSignal signal) throws IOException{
        pipelineNode.signalPipeline(threadLabel, signal);
    }
    
    protected void notifyPredecessor(Label threadLabel){
        pipelineNode.notifyPipelineNode(threadLabel);
    }
    
    protected void notifyPredecessor(){
        pipelineNode.notifyPipelineNode();
    }
    
	public synchronized void signalSuperior(AbstractModelThread thread, Label threadLabel, PipelineSignal signal) throws IOException{ //signal from subordinate thread
        if(signal instanceof NotifySignal){ //thread is signalling that it is finished counting a document
            notifyPredecessor(threadLabel); //propagate notifying signal to previous model via pipeline
			
		}else if(signal instanceof FinishSignal){ //label: context window
			finishThread(threadLabel);
		}
    }
    
    protected void addSignalQueue(Label threadLabel, BlockingQueue<PipelineSignal> signalQueue){
        signalQueues.put(threadLabel, signalQueue);
    }
    
    private BlockingQueue<PipelineSignal> getSignalQueue(Label threadLabel){
        return signalQueues.get(threadLabel);
    }
    
    protected abstract AbstractModelThread createThread(Label threadLabel) throws IOException;
    
    protected void addThread(Label threadLabel, AbstractModelThread thread){
        threads.put(threadLabel, thread);
    }
	
	protected boolean hasThread(Label threadLabel){
		return threads.containsKey(threadLabel);
	}
    
    protected void startThread(Label threadLabel) throws IOException{
        AbstractModelThread thread = createThread(threadLabel);
        addThread(threadLabel, thread);
        addSignalQueue(threadLabel, thread.getSignalQueue());
        AbstractExperiment.startThread(thread);
    }
    
    private synchronized void finishThread(Label threadLabel) throws IOException{
      
        //if(threadLabel.equals(Label.MASTER_LABEL)) return;
        
        //finish this thread locally
        Runnable thread = threads.remove(threadLabel);

        //finish this thread globally
        AbstractExperiment.finishThread(thread);
        signalQueues.remove(threadLabel);
        amountOfFinishedThreads++;

        //Helper.report("[" + this.getClass().getSimpleName() + "] Closing thread... (open threads: " + threads.size() + ", closed threads: " + amountOfFinishedThreads + ")");

        //if locally all threads are finished, then finish this model
        if(allowedToFinish && threads.isEmpty()){
            finishModel();
            //signalPipeline(Label.MASTER_LABEL, new FinishSignal()); //...tell experiment and threads manager to finish
            //allowSuccessorToFinish();
        }

        signalPipeline(threadLabel, new FinishSignal());
    }
    
    protected void signalThread(Label threadLabel, PipelineSignal signal){
		BlockingQueue<PipelineSignal> signalQueue = getSignalQueue(threadLabel);
        if(signalQueue != null){
			signalQueue.add(signal);
            if(this instanceof SequentialKMeans && signal instanceof FinishSignal){
                this.amountOfFinishedThreads++;
                this.amountOfFinishedThreads--;
            }
		}
    }

    
    protected abstract void startModel() throws IOException;
    protected abstract void finishModel() throws IOException;
	
	public void signalModel(Label label, PipelineSignal signal) throws IOException{
        
        //if(signal instanceof FinishSignal) report("Receiving signal " + signal.getClass().getSimpleName() + " from predecessor (label: " + label.toString() + ")...");
        
		if(signal instanceof StartSignal){
            if(label.equals(Label.MASTER_LABEL)){
                signalPipeline(label, signal); //start downstream models first, then
                startModel();
            }else{
				//report("Starting thread with label: " + label + "...");
				startThread(label); //label: context window, prototype vector description
			}
			
		}else if(signal instanceof FinishSignal){
            if(label.equals(Label.MASTER_LABEL)){
                allowedToFinish = true;
            }else{
                //finishThread(label); //label: context window
                //report("Telling thread \"" + label + "\" to FINISH");
                signalThread(label, signal);
            }
		}
	}
    
    protected void allowSuccessorToFinish() throws IOException{
        signalPipeline(Label.MASTER_LABEL, new FinishSignal());
    }
    
    
    //notify given thread under this model
    public void notifyModel(Label threadLabel){
        AbstractModelThread thread = threads.get(threadLabel);
        if(thread != null) thread.notifyThread();
    }
    
    //notify this model, not a thread under it
    public synchronized void notifyModel(){
        notify();
    }
    
    public abstract String protocol();
    
}