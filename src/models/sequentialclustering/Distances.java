package models.sequentialclustering;

import java.util.Arrays;

public class Distances {

	private float[][] distances;
	public float nearestDistance, secondNearestDistance;
	public int index1OfNearestDistanceClusterPair, index2OfNearestDistanceClusterPair, index1OfSecondNearestDistanceClusterPair, index2OfSecondNearestDistanceClusterPair; //index1 is always larger than index2
	
	public Distances(int n){
		distances = new float[n][];
		for(int i=0; i<n; i++){
			distances[i] = new float[i];
			for(int j=0; j<i; j++){
				distances[i][j] = Float.POSITIVE_INFINITY;
			}
		}
		
		nearestDistance = Float.POSITIVE_INFINITY;
		secondNearestDistance = Float.POSITIVE_INFINITY;
		index1OfNearestDistanceClusterPair = -1;
		index2OfNearestDistanceClusterPair = -1;
	}
	
	//removes and returns nearest distance
	public int[] removeNearestDistance(){
		nearestDistance = secondNearestDistance;
		secondNearestDistance = Float.POSITIVE_INFINITY;
		int[] output = new int[]{ index1OfNearestDistanceClusterPair, index2OfNearestDistanceClusterPair };
		index1OfNearestDistanceClusterPair = index1OfSecondNearestDistanceClusterPair;
		index2OfNearestDistanceClusterPair = index2OfSecondNearestDistanceClusterPair;
		index1OfSecondNearestDistanceClusterPair = -1;
		index2OfSecondNearestDistanceClusterPair = -1;
		return output;
	}
	
	//returns whether given distance is nearest distance
	public void update(int i1, int i2, float distance){
		if(i1 == i2) return;
		if(i1 < i2){
			int t = i1;
			i1 = i2;
			i2 = t;
		}
		distances[i1][i2] = distance;
		if(distance < nearestDistance){
			secondNearestDistance = nearestDistance;
			index1OfSecondNearestDistanceClusterPair = index1OfNearestDistanceClusterPair;
			index2OfSecondNearestDistanceClusterPair = index2OfNearestDistanceClusterPair;
			nearestDistance = distance;
			index1OfNearestDistanceClusterPair = i1;
			index2OfNearestDistanceClusterPair = i2;
		}else if(distance < secondNearestDistance){
			secondNearestDistance = distance;
			index1OfSecondNearestDistanceClusterPair = i1;
			index2OfSecondNearestDistanceClusterPair = i2;
		}
	}
	
	@Override
	public String toString(){
		return Arrays.deepToString(distances);
	}
	
}
