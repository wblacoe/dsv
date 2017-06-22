package models.sequentialclustering.miniBatchKMeans;

import java.util.Collections;
import experiment.common.Parameters;
import java.util.LinkedList;
import experiment.common.Description;
import vector.AbstractVector;
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
import models.sequentialclustering.SequentialClusteringModel;
import models.sequentialclustering.SequentialClusteringModelThread;
import pipeline.signals.FinishSignal;
import pipeline.signals.PipelineSignal;
import vector.FloatVector;
import vector.IntegerVector;
import vector.complex.DistributionOfVectors;

//from https://www.eecs.tufts.edu/~dsculley/papers/fastkmeans.pdf
public class MiniBatchKMeans extends SequentialClusteringModel{

    public MiniBatchKMeans(){

	}

    @Override
    public Description getDescription() {
        Description d = super.getDescription();
		d.setTypeAttribute("mini batch kmeans");
        //d.addParameter("mini batch capacity", "" + miniBatchCapacity);
        //d.addParameter("amount of iterations", "" + amountOfIterations);
        
        return d;
    }

    @Override
    protected AbstractModelThread createThread(Label threadLabel) {
        BlockingQueue<PipelineSignal> signalQueue = new LinkedBlockingDeque<>();
		
		int amountOfClusters = Parameters.getIntParamter("amount of clusters");
		float pnorm = Parameters.getFloatParamter("p norm");
		int miniBatchCapacity = Parameters.getIntParamter("mini batch capacity");
        int amountOfIterations = Parameters.getIntParamter("amount of iterations");
        
        return new MiniBatchKMeansThread(this, threadLabel, signalQueue, amountOfClusters, pnorm, miniBatchCapacity, amountOfIterations);
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


class MiniBatchKMeansThread extends SequentialClusteringModelThread{

	int miniBatchCapacity, amountOfIterations;
        
	
	public MiniBatchKMeansThread(AbstractModel superior, Label threadLabel, BlockingQueue<PipelineSignal> signalQueue, int amountOfClusters, float pnorm, int miniBatchCapacity, int amountOfIterations) {
		super(superior, threadLabel, signalQueue);
		
		this.amountOfClusters = amountOfClusters;
		this.pnorm = pnorm;
		this.miniBatchCapacity = miniBatchCapacity;
		this.amountOfIterations = amountOfIterations;
	}

	@Override
    public AbstractBagOfClusters getBagOfClusters(Word word){
		AbstractBagOfClusters bag = bagsOfClusters.get(word);
		if(bag == null){
			bag = new BagOfClusters(amountOfClusters, pnorm, miniBatchCapacity, amountOfIterations);
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
            
            for(Map.Entry<Word, AbstractBagOfClusters> entry : bagsOfClusters.entrySet()){
                Word tw = entry.getKey();
                AbstractBagOfClusters bag = entry.getValue();
                DistributionOfVectors dov = bag.toDistributionOfVectors();
                dov.normaliseVectorWeights();
                signalSuperior(threadLabel, tw);
                signalSuperior(threadLabel, dov);
            }

            report("k=" + amountOfClusters, " ...Finished clustering data points");
            finishThread();
            
		}catch(InterruptedException | IOException e){
			e.printStackTrace();
		}
    }
	
}


class BagOfClusters extends AbstractBagOfClusters{
	
	private int miniBatchCapacity;
    private LinkedList<FloatVector> miniBatch; //content of mini batch is not picked randomly from dataset, rather it comes from corpus online
    private int amountOfIterations, amountOfVectorsInMiniBatch; //, amountOfBatches;
	
	public BagOfClusters(int amountOfClusters, float pnorm, int miniBatchCapacity, int amountOfIterations) {
		super(amountOfClusters, pnorm);
		
		this.miniBatchCapacity = miniBatchCapacity;
        miniBatch = new LinkedList<>();
		this.amountOfIterations = amountOfIterations;
		amountOfVectorsInMiniBatch = 0;
	}
	
	
	private int getNearestClusterIndex(AbstractVector dataVector){
        float nearestClusterDistance = Float.POSITIVE_INFINITY;
        int nearestClusterIndex = -1;
        for(int j=0; j<amountOfClusters; j++){
			float distance = dataVector.minus(clusters[j].clusterCenter).norm(pnorm);
            if(distance < nearestClusterDistance){
                nearestClusterDistance = distance;
                nearestClusterIndex = j;
            }
        }
        
        //Helper.report("(" + dataIndex + ") #" + nearestClusterIndex + " is the nearest cluster (distance: " + nearestClusterDistance + ") for vector " + x.toString(true));
        return nearestClusterIndex;
    }

	private void initialiseClusters(){
		clusters = new Cluster[amountOfClusters];
		
		//use the first [amount of clusters] data vectors from (the already shuffled) mini batch as cluster centers
		for(int j=0; j<amountOfClusters; j++){
			clusters[j] = new Cluster(miniBatch.remove(0), 1);
        }
	}
	
	public void processMiniBatch(){
		//Helper.report("processing mini batch " + amountOfBatches);
		
		//shuffle content of mini batch
		Collections.shuffle(miniBatch);
			
		if(clusters == null) initialiseClusters();
		
        //iterate k-means
        for(int t=0; t<amountOfIterations; t++){
            //Helper.report("starting iteration " + t);

            //save nearest clusters for each data vector
            int[] nearestClusterIndices = new int[miniBatch.size()];
            for(int j=0; j<miniBatch.size(); j++){
                nearestClusterIndices[j] = getNearestClusterIndex(miniBatch.get(j));
            }

            //update cluster centers
            for(int j=0; j<miniBatch.size(); j++){
                AbstractVector dataVector = miniBatch.get(j);
                int nearestClusterIndex = nearestClusterIndices[j];
				clusters[nearestClusterIndex].clusterSize++;
				float influence = 1f / clusters[nearestClusterIndex].clusterSize;
                //Helper.report("(" + j + ") updating cluster " + nearestClusterIndex + " with influence " + influence + " from vector " + v.toString(true));
				clusters[nearestClusterIndex].clusterCenter = (FloatVector) clusters[nearestClusterIndex].clusterCenter.times(1f - influence).plus(dataVector.times(influence));
            }
        }
		
		miniBatch.clear();
		amountOfVectorsInMiniBatch = 0;
		//amountOfBatches++;
    }

	@Override
	public void addDataVector(AbstractVector dataVector) {
        //Helper.report("(" + i + ") adding vector " + x.toString(true));
        
        //fill mini batch until full
        if(amountOfVectorsInMiniBatch < miniBatchCapacity){
            //inputDescription = dataVector.getDescription();
            
            FloatVector addedVector;
            if(dataVector instanceof IntegerVector){
                addedVector = (FloatVector) dataVector.times(1f);
            }else{
                addedVector = (FloatVector) dataVector;
            }
            miniBatch.add(addedVector);
            
            //outputDescription = addedVector.getDescription();
            
            amountOfVectorsInMiniBatch++;
        }
            
        //process current mini batch
        if(amountOfVectorsInMiniBatch == miniBatchCapacity){
            processMiniBatch();
        }
    }
	
}