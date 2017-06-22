package models.sequentialclustering;

import experiment.common.Label;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import lingunit.flattext.Word;
import models.AbstractModel;
import models.AbstractModelThread;
import pipeline.signals.PipelineSignal;
import vector.complex.BagOfMeaningRepresentations;

public abstract class SequentialClusteringModelThread extends AbstractModelThread{
    
    public int amountOfClusters;
	public float pnorm;
    public TreeMap<Word, AbstractBagOfClusters> bagsOfClusters;
    protected BagOfMeaningRepresentations emptyBag;

    public SequentialClusteringModelThread(AbstractModel superior, Label threadLabel, BlockingQueue<PipelineSignal> signalQueue) {
        super(superior, threadLabel, signalQueue);
        
        bagsOfClusters = new TreeMap<>();
        emptyBag = new BagOfMeaningRepresentations(threadLabel);
    }
	
	public abstract AbstractBagOfClusters getBagOfClusters(Word word);
    
    @Override
    public String asString(Object o){
        String s = "sequential clustering model -> cluster sizes: ";
        //for(Cluster cluster : clusters) s += pair.clusterSize + ", ";
        
        return s;
    }
       
}