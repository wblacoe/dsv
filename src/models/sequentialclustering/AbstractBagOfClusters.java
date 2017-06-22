package models.sequentialclustering;

import java.util.Iterator;
import lingunit.flattext.Word;
import vector.AbstractVector;
import vector.complex.DistributionOfVectors;

public abstract class AbstractBagOfClusters implements Iterable<Cluster> {
    
    public int amountOfDatapoints, amountOfClusters;
    public float pnorm;
    public Word targetWord;
    public Cluster[] clusters;
    
    public AbstractBagOfClusters(int amountOfClusters, float pnorm){
        amountOfDatapoints = 0;
        this.amountOfClusters = amountOfClusters;
        this.pnorm = pnorm;
        //clusters = new Cluster[amountOfClusters];
    }
	
	public abstract void addDataVector(AbstractVector dataVector);
    
    public DistributionOfVectors toDistributionOfVectors(){
        DistributionOfVectors dov = new DistributionOfVectors();
        for(Cluster cluster : this){
            if(cluster != null){
                dov.addWeightedVector(cluster.clusterCenter, cluster.clusterSize);
            }
        }
        
        return dov;
    }

    @Override
    public Iterator<Cluster> iterator() {
        return new Iterator<Cluster>() {

            int i = 0;
            
            @Override
            public boolean hasNext() {
                return i < clusters.length;
            }

            @Override
            public Cluster next() {
                return clusters[i++];
            }
            
        };
    }
    
}
