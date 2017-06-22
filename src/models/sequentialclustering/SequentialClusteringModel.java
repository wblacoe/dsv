package models.sequentialclustering;

import models.sequentialclustering.sequentialKMeans.SequentialKMeans;
import models.sequentialclustering.miniBatchKMeans.MiniBatchKMeans;
import models.sequentialclustering.sequentialAgglomerativeClustering.SequentialAgglomerativeClustering;
import experiment.common.Label;
import experiment.common.Parameters;
import java.io.IOException;
import lingunit.flattext.Word;
import models.AbstractModel;
import models.AbstractModelThread;
import pipeline.signals.PipelineSignal;
import pipeline.signals.StartSignal;
import vector.AbstractVector;
import vector.complex.BagOfMeaningRepresentations;
import vector.complex.DistributionOfVectors;

public abstract class SequentialClusteringModel extends AbstractModel{

	public static final int MINI_BATCH_KMEANS = 0;
	public static final int SEQUENTIAL_AGGLOMERATIVE_CLUSTERING = 1;
	public static final int SEQUENTIAL_KMEANS = 2;
	public static final String[] SEQUENTIAL_CLUSTERING_MODELS = new String[]{ "mini batch kmeans", "sequential agglomerative clustering", "sequential kmeans" };
	
	
    public SequentialClusteringModel(){

    }
	
	public static SequentialClusteringModel create(int sequentialClusteringModel){
		SequentialClusteringModel clusterer;
		
		switch(sequentialClusteringModel){
			case MINI_BATCH_KMEANS:
				clusterer = new MiniBatchKMeans();
				break;
			case SEQUENTIAL_AGGLOMERATIVE_CLUSTERING:
				clusterer = new SequentialAgglomerativeClustering();
				break;
			case SEQUENTIAL_KMEANS:
				clusterer = new SequentialKMeans();
				break;
			default:
				clusterer = null;
		}
		
		return clusterer;
	}
	
    public static SequentialClusteringModel create(){
        return create(Parameters.getConstantParameter("sequential clustering model", SEQUENTIAL_CLUSTERING_MODELS));
    }
    
    public static SequentialClusteringModel create(String modelName){
        return create(getIndex(SEQUENTIAL_CLUSTERING_MODELS, modelName));
    }
	
	@Override
    public void signalSuperior(AbstractModelThread thread, Label label, PipelineSignal signal) throws IOException{
        if(signal instanceof BagOfMeaningRepresentations || signal instanceof Word || signal instanceof DistributionOfVectors){ //label: context window
            signalPipeline(label, signal);
		}
		
        super.signalSuperior(thread, label, signal);
    }
 
	@Override
	public void signalModel(Label label, PipelineSignal signal) throws IOException{
		if(signal instanceof StartSignal && !label.equals(Label.MASTER_LABEL)){
			signalPipeline(label, signal);
            allowSuccessorToFinish();
		}else if(signal instanceof Word){ //label: context window
            Word targetWord = (Word) signal;
            signalThread(label, targetWord);
        }else if(signal instanceof AbstractVector){ //label: context window
            AbstractVector vector = (AbstractVector) signal;
            signalThread(label, vector);
        }
        
        super.signalModel(label, signal);
	}
	
}