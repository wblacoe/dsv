package models;

import experiment.common.Parameters;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import vector.AbstractVector;
import vector.VectorEntry;

public abstract class AbstractApacheModel extends AbstractModel implements AddDataVectors{

	public RealMatrix dataset; //dataset matrix, each column is a datapoint (can this be sparse?)
	protected int amountOfAddedDataVectors;
	
	public AbstractApacheModel() {
        dataset = new Array2DRowRealMatrix(
            Parameters.getIntParamter("amount of data points"),
            Parameters.getIntParamter("data dimensionality")
        );
        amountOfAddedDataVectors = 0;
	}
	
    @Override
	public void addDataVector(AbstractVector dataVector) { //assumes given vector to be float value
        for(VectorEntry ve : dataVector){
            dataset.setEntry(amountOfAddedDataVectors, ve.getDimension(), ve.getFloatValue());
        }
        amountOfAddedDataVectors++;
	}

	/*@Override
	public void randomlyPopulateDataset(float relativeCardinality){
		Integer amountOfDataPoints, dataDimensionality;
		if((amountOfDataPoints = Parameters.getIntParamter("amount of data points")) != null && (dataDimensionality = Parameters.getIntParamter("data dimensionality")) != null){
			for(int i=0; i<amountOfDataPoints; i++){
				AbstractVector dataVector = SparseFloatVector.createRandom(dataDimensionality, relativeCardinality);
				addDataVector(dataVector);
			}
		}
	}
	*/
	
	/*@Override
	public ArrayList<String> requiredParameters() {
		ArrayList<String> rp = super.requiredParameters();
		rp.add("amount of data points");
		rp.add("data dimensionality");
		return rp;
	}
    */

    /*@Override
    public Description getDescription(){
        Description d = super.getDescription();
        
        
        return d;
    }
	*/
	
}
