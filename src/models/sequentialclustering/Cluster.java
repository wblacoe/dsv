package models.sequentialclustering;

import vector.FloatVector;

public class Cluster {

	public FloatVector clusterCenter;
	public int clusterSize;
	
	public Cluster(FloatVector clusterCenter, int clusterSize){
        this.clusterCenter = clusterCenter;
		this.clusterSize = clusterSize;
	}
    
	
	public Cluster merge(Cluster cluster){
		//merged cluster center is linear combination of previous this and given cluster center weighted by their cluster sizes
		//merged cluster size is sum of this and given cluster sizes
		return new Cluster(
			(FloatVector) (clusterCenter.times(1f * clusterSize / (clusterSize + cluster.clusterSize)).plus(
			cluster.clusterCenter.times(1f * cluster.clusterSize / (clusterSize + cluster.clusterSize)))),
			clusterSize + cluster.clusterSize);
	}
	
}
