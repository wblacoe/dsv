package pipeline;

import experiment.AbstractExperiment;
import experiment.common.Description;
import pipeline.signals.PipelineSignal;
import java.io.IOException;
import meta.Describable;
import experiment.common.Label;
import models.AbstractModel;
import pipeline.signals.FinishSignal;
import pipeline.signals.StartSignal;

public class PipelineNode implements Describable{
    
    private final AbstractModel model;
    private PipelineNode predecessor, successor;
    
    
    public PipelineNode(AbstractModel model){
        this.model = model;
        model.setPipelineNode(this);
        predecessor = null;
        successor = null;
    }
    
    
    public boolean hasSuccessor(){
        return successor != null;
    }
    
    public PipelineNode getSuccessor(){
        return successor;
    }
    
    public void setSuccessor(PipelineNode node){
        successor = node;
    }
    
    public PipelineNode removeSuccessor(){
        PipelineNode ref = successor;
        successor = null;
        
        return ref;
    }
    
    public boolean hasPredecessor(){
        return predecessor != null;
    }
    
    public PipelineNode getPredecessor(){
        return predecessor;
    }
    
    public void setPredecessor(PipelineNode node){
        predecessor = node;
    }
    
    public PipelineNode removePredecessor(){
        PipelineNode ref = predecessor;
        predecessor = null;
        
        return ref;
    }
    
    
    
    //pass on signal from model to next pipeline node
    //final pipeline node has no successor
    public synchronized void signalPipeline(Label label, PipelineSignal signal) throws IOException{ //this node's model calls this to send given signal on to the model inside a/the child node of this node
        
        //Helper.report("[PipelineNode] from model: " + model.toString() + ", label: " + label.toString() + ", signal: " + signal.toString()); //DEBUG
        if(signal instanceof FinishSignal){
            
            if(hasSuccessor()){
                //Helper.report("[" + model.getClass().getSimpleName() + "] Sending signal (" + signal.getClass().getSimpleName() + ") to " + successor.getModel().getClass().getSimpleName() + " (label: " + label.toString() + ")...");
                successor.signalModel(label, signal);
            }
            
            if(!hasSuccessor() && label.equals(Label.MASTER_LABEL)){
                //Helper.report("[PipelineNode] TELLING SIGNAL QUEUE TO SHUT DOWN...");
                AbstractExperiment.closeThreadsManager();
            }
        }else{
            if(hasSuccessor()) successor.signalModel(label, signal);
        }
        
    }
    
    
    public AbstractModel getModel(){
        return model;
    }
	
    public void run() throws IOException{
        model.signalModel(Label.MASTER_LABEL, new StartSignal());
        model.signalModel(Label.MASTER_LABEL, new FinishSignal());
	}
    
    protected void signalModel(Label label, PipelineSignal signal) throws IOException{
        
        //if this node has a model, send signal to that model
        if(model != null){
            model.signalModel(label, signal);
            
        //if this node has no model, skip this node and send signal to next node
        }else{
            successor.signalPipeline(label, signal);
        }
    }

    @Override
    public Description getDescription() {
        Description d = new Description();
        d.setTypeAttribute("pipe simple node");
        d.addChild(model.getDescription());
        
        return d;
    }
 
    public static PipelineNode create(AbstractModel model){
        PipelineNode node = new PipelineNode(model);
        
        return node;
    }

    protected void notifyModel(Label threadLabel) {
        model.notifyModel(threadLabel);
    }
    
    protected void notifyModel() {
        model.notifyModel();
    }
    
    public void notifyPipelineNode(Label threadLabel){
        if(hasPredecessor()) predecessor.notifyModel(threadLabel);
    }
    
    public void notifyPipelineNode(){
        if(hasPredecessor()) predecessor.notifyModel();
    }
    
}
