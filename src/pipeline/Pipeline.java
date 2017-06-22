package pipeline;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import meta.Describable;
import experiment.common.Description;
import meta.Exportable;
import meta.Printable;
import models.AbstractModel;

//a pipeline is a graph of pipeline nodes that should be a DAG
public class Pipeline implements Describable, Iterable<PipelineNode>, Exportable, Printable{
	
	private PipelineNode root, lastNode;
	
	public Pipeline(){
        
    }
    
    public Pipeline(PipelineNode node){
        this();
        addNode(node);
    }
    
    public Pipeline(ArrayList<PipelineNode> nodes){
        this();
        addNodes(nodes);
    }
    
    public Pipeline(PipelineNode[] nodes){
        this();
        addNodes(nodes);
    }
    
    public Pipeline(AbstractModel[] models){
        for(AbstractModel model : models){
            addNode(new PipelineNode(model));
        }
    }
    
    
    public void clear(){
        root = null;
        lastNode = null;
    }
    
    public boolean isEmpty(){
        return root == null;
    }
    
    public void addNode(PipelineNode node){
        if(isEmpty()){
            root = node;
        }else{
            lastNode.setSuccessor(node);
            node.setPredecessor(lastNode);
        }
        lastNode = node;
    }
    
    public void addNodes(ArrayList<PipelineNode> nodes){
        for(PipelineNode node : nodes) addNode(node);
    }
    
    public void addNodes(PipelineNode[] nodes){
        for(PipelineNode node : nodes) addNode(node);
    }
	
	public void run() throws IOException{
		if(!isEmpty()){
			root.run();
		}
	}

    @Override
    public Description getDescription() {
        Description d = new Description();
        d.setTypeAttribute("pipeline");
        for(PipelineNode node : this){
            d.addChild(node.getDescription());
        }
        
        return d;
    }

    @Override
    public Iterator<PipelineNode> iterator() {
        return new Iterator<PipelineNode>() {
            
            private PipelineNode node = root;

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public PipelineNode next() {
                PipelineNode ref = node;
                node = node.getSuccessor();
                
                return ref;
            }
            
        };
    }

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        this.getDescription().exportTo(writer);
        writer.close();
        return true;

    }

    @Override
    public String asString(Object o) {
        return getDescription().asString(o);
    }

}
