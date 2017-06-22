package models;

import edu.ucla.sspace.matrix.AtomicGrowingSparseHashMatrix;
import edu.ucla.sspace.matrix.SparseMatrix;
import vector.AbstractVector;
import vector.VectorEntry;

public abstract class AbstractSspaceModel extends AbstractModel implements AddDataVectors{

	public SparseMatrix dataset; //each row is a data vector (I think?)
	private int amountOfAddedDataVectors;

	public AbstractSspaceModel(){
		dataset = new AtomicGrowingSparseHashMatrix();
		amountOfAddedDataVectors = 0;
	}	

	@Override
	public void addDataVector(AbstractVector dataVector){ //assumes given vector to be float vector
		for(VectorEntry ve : dataVector){
			dataset.set(amountOfAddedDataVectors, ve.getDimension(), ve.getFloatValue());
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

	/*@Override
	public ArrayList<String> requiredParameters() {
		ArrayList<String> rp = super.requiredParameters();
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
