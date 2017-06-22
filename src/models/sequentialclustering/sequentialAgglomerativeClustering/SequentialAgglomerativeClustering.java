package models.sequentialclustering.sequentialAgglomerativeClustering;

import experiment.common.Parameters;
import experiment.common.Description;
import experiment.common.Label;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import lingunit.flattext.Word;
import models.AbstractModel;
import models.AbstractModelThread;
import models.sequentialclustering.AbstractBagOfClusters;
import models.sequentialclustering.Cluster;
import models.sequentialclustering.Distances;
import models.sequentialclustering.SequentialClusteringModel;
import models.sequentialclustering.SequentialClusteringModelThread;
import pipeline.signals.FinishSignal;
import pipeline.signals.PipelineSignal;
import vector.AbstractVector;
import vector.FloatVector;
import vector.complex.DistributionOfVectors;

//a variant of Algorithm 2.4 in https://papers.nips.cc/paper/5608-incremental-clustering-the-case-for-extra-clusters.pdf
//does not work. when using sparse vectors, there is always one cluster that ends up with nearly all datapoints.
public class SequentialAgglomerativeClustering extends SequentialClusteringModel{

	
	public SequentialAgglomerativeClustering(){
		
	}
	
    @Override
    public Description getDescription() {
        Description d = super.getDescription();
		d.setTypeAttribute("sequential agglomerative clustering");
        
        return d;
    }

    @Override
    protected AbstractModelThread createThread(Label threadLabel) {
		BlockingQueue<PipelineSignal> signalQueue = new LinkedBlockingDeque<>();
		
		int amountOfClusters = Parameters.getIntParamter("amount of clusters");
		float pnorm = Parameters.getFloatParamter("p norm");
        
        return new SequentialAgglomerativeClusteringThread(this, threadLabel, signalQueue, amountOfClusters, pnorm);
	}

    @Override
    public String protocol() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    protected void startModel() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void finishModel() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

 
}



class SequentialAgglomerativeClusteringThread extends SequentialClusteringModelThread{

	public SequentialAgglomerativeClusteringThread(AbstractModel superior, Label threadLabel, BlockingQueue<PipelineSignal> signalQueue, int amountOfCluster, float pnorm) {
		super(superior, threadLabel, signalQueue);
		
		this.amountOfClusters = amountOfCluster;
		this.pnorm = pnorm;
	}

	@Override
	public AbstractBagOfClusters getBagOfClusters(Word word) {
		AbstractBagOfClusters bag = bagsOfClusters.get(word);
		if(bag == null){
			bag = new BagOfClusters(amountOfClusters, pnorm);
			bagsOfClusters.put(word, bag);
		}
		
		return bag;

	}

	@Override
	public void run() {
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
            
            for(Map.Entry<Word, AbstractBagOfClusters> entry : bagsOfClusters.entrySet()){
                Word tw = entry.getKey();
                AbstractBagOfClusters bag = entry.getValue();
                DistributionOfVectors dov = bag.toDistributionOfVectors();
                dov.normaliseVectorWeights();
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
	
	private Distances distances;
	private int amountOfDatapoints;

	public BagOfClusters(int amountOfClusters, float pnorm) {
		super(amountOfClusters, pnorm);
		
		this.amountOfClusters = amountOfClusters;
		clusters = new Cluster[amountOfClusters];
		distances = new Distances(amountOfClusters);
		amountOfDatapoints = 0;
		this.pnorm = pnorm;
	}

	@Override
	public void addDataVector(AbstractVector vector) {
        
        FloatVector dataVector;
        if(vector instanceof FloatVector){
            dataVector = (FloatVector) vector;
        }else{
            dataVector = (FloatVector) vector.times(1f);
        }
        
		//initially each adistancesed vector starts a new cluster
		if(amountOfDatapoints < amountOfClusters){
			clusters[amountOfDatapoints] = new Cluster(dataVector, 1);
			
			for(int i=0; i<amountOfDatapoints; i++){
				float d = clusters[amountOfDatapoints].clusterCenter.minus(clusters[i].clusterCenter).norm(pnorm);
				distances.update(amountOfDatapoints, i, d);
			}
			
		//update the clusters
		}else{
			//Helper.report(distances.toString());
			//Helper.report(Arrays.toString(clusters));
			
			//merge the two closest-to-each-other cluster centers
			int[] nearestPairIndices = distances.removeNearestDistance();
			int i1 = nearestPairIndices[0];
			int i2 = nearestPairIndices[1];
			Cluster mergedPair = clusters[i1].merge(clusters[i2]);
			clusters[i1] = mergedPair;
			
			//insert given vector as new cluster center
			clusters[i2] = new Cluster(dataVector, 1);
			
			//update distances
			for(int i=0; i<amountOfClusters; i++){
				if(i != i1){
					//update distances between merged cluster and existing clusters
					distances.update(i1, i, clusters[i1].clusterCenter.minus(clusters[i].clusterCenter).norm(pnorm));
					if(i != i2){
						//update distances between new singleton cluster and existing clusters
						distances.update(i2, i, clusters[i2].clusterCenter.minus(clusters[i].clusterCenter).norm(pnorm));
					}
				}
			}
			
		}
		amountOfDatapoints++;
	}	
	
}