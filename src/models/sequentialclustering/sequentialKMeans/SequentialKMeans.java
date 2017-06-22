package models.sequentialclustering.sequentialKMeans;

import experiment.AbstractExperiment;
import experiment.common.ContextWindow;
import experiment.common.Parameters;
import experiment.common.Description;
import experiment.common.Label;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import lingunit.flattext.Word;
import models.AbstractModel;
import models.AbstractModelThread;
import models.sequentialclustering.AbstractBagOfClusters;
import models.sequentialclustering.Cluster;
import models.sequentialclustering.SequentialClusteringModel;
import models.sequentialclustering.SequentialClusteringModelThread;
import pipeline.signals.FinishSignal;
import pipeline.signals.PipelineSignal;
import vector.AbstractVector;
import vector.FloatVector;
import vector.complex.DistributionOfVectors;

//also callled "online k-means" in https://papers.nips.cc/paper/989-convergence-properties-of-the-k-means-algorithms.pdf
public class SequentialKMeans extends SequentialClusteringModel{

	
	public SequentialKMeans(){
		
	}
	
    
    @Override
    protected AbstractModelThread createThread(Label threadLabel) {
        BlockingQueue<PipelineSignal> signalQueue = new LinkedBlockingDeque<>();
		int amountOfClusters = Parameters.getIntParamter("amount of clusters");
		float pnorm = Parameters.getFloatParamter("p norm");
        return new SequentialKMeansThread(this, threadLabel, signalQueue, amountOfClusters, pnorm);
    }

    @Override
    public Description getDescription() {
        Description d = super.getDescription();
		d.setTypeAttribute("sequential kmeans");
        
        return d;
    }

    @Override
    public String protocol() {
        return "INPUT:\n" +
            "label: master label, signal: start signal\n" +
			"for each context window:\n" +
			"  label: context window, signal: start signal\n" +
            "  label: context window, signal: empty bag-of-MRs with header\n" +
			"  for each target word:\n" +
			"    for each occurence of target word within context window in document:" +
			"      label: context window, signal: target word\n" +
            "      label: context window, signal: count vector\n" +
			"  label: context window, signal: finish signal";
    }

	@Override
	protected void startModel() {
		
	}

	@Override
	protected void finishModel() throws IOException{
        //for(ContextWindow cw : Experiment.getContextWindows()){
            //Label cwLabel = new Label(cw);
            //signalPipeline(cwLabel, new FinishSignal());
        //}
        FinishSignal finishSignal = new FinishSignal();
        for(ContextWindow contextWindow : AbstractExperiment.getContextWindows()){
			Label newLabel = new Label(contextWindow);
            signalPipeline(newLabel, finishSignal);
        }
    }

}



class SequentialKMeansThread extends SequentialClusteringModelThread{

    public SequentialKMeansThread(AbstractModel superior, Label threadLabel, BlockingQueue<PipelineSignal> signalQueue, int amountOfClusters, float pnorm) {
        super(superior, threadLabel, signalQueue);
        
        this.amountOfClusters = amountOfClusters;
		this.pnorm = pnorm;
    }
    
	@Override
    public AbstractBagOfClusters getBagOfClusters(Word word){
		AbstractBagOfClusters bag = bagsOfClusters.get(word);
		if(bag == null){
			bag = new BagOfClusters(amountOfClusters, pnorm);
			bagsOfClusters.put(word, bag);
		}
		
		return bag;
	}
    
    @Override
    public void run(){
        report("k=" + amountOfClusters, "Starting to cluster data points...");
        
        Word targetWord = null;
		try{
			while(true){
				PipelineSignal signal = signalQueue.take();
				if(signal instanceof Word){
                    targetWord = (Word) signal;
                }else if(signal instanceof AbstractVector){
                    //if(amountOfDatapoints % 100 == 0) Helper.report("[OfflineCounterThread] (" + threadLabel.toString() + ") has clustered " + amountOfDatapoints + " data points..."); //DEBUG
                    AbstractVector vector = (AbstractVector) signal;
                    getBagOfClusters(targetWord).addDataVector(vector);
                    //amountOfDatapoints++;
				}else if(signal instanceof FinishSignal){
					break;
				}
			}
            
            for(Entry<Word, AbstractBagOfClusters> entry : bagsOfClusters.entrySet()){
                Word tw = entry.getKey();
                AbstractBagOfClusters bag = entry.getValue();
                DistributionOfVectors dov = bag.toDistributionOfVectors();
                if(!emptyBag.hasContentDescription()){ //signal empty bag-of-MRs with header
                    emptyBag.setContentDescription(dov.getDescription());
                    signalSuperior(threadLabel, emptyBag);
                }
                dov.normaliseVectorWeights(); //keep this?
                signalSuperior(threadLabel, tw);
                signalSuperior(threadLabel, dov);
            }

            report("k=" + amountOfClusters, "...Finished clustering data points");
            finishThread();

		}catch(InterruptedException | IOException e){
			e.printStackTrace();
		}
    }
    
}



class BagOfClusters extends AbstractBagOfClusters{

	public BagOfClusters(int amountOfClusters, float pnorm) {
		super(amountOfClusters, pnorm);
		
		clusters = new Cluster[amountOfClusters];
	}
	
	@Override
	public void addDataVector(AbstractVector vector){
        
        FloatVector dataVector;
        if(vector instanceof FloatVector){
            dataVector = (FloatVector) vector;
        }else{
            dataVector = (FloatVector) vector.times(1f);
        }
		
		//initially each added vector starts a new cluster
		if(amountOfDatapoints < amountOfClusters){
			clusters[amountOfDatapoints] = new Cluster(dataVector, 1);
			
		//update the clusters
		}else{
			int j = -1;
			float distanceToNearestClusterCenter = Float.POSITIVE_INFINITY;
			for(int i=0; i<amountOfClusters; i++){
				//float distance = dataVector.minus(clusterCenters[i]).norm(pnorm);
				float distance = dataVector.minus(clusters[i].clusterCenter).norm(pnorm);
				if(distance < distanceToNearestClusterCenter){
					distanceToNearestClusterCenter = distance;
					j = i;
				}
			}
			clusters[j].clusterSize++;
			float influence = 1f / clusters[j].clusterSize;
			clusters[j].clusterCenter = (FloatVector) clusters[j].clusterCenter.times(1 - influence).plus(dataVector.times(influence));
		}
			
		amountOfDatapoints++;
	}
	
}