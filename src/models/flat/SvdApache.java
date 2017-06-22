package models.flat;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import models.AbstractApacheModel;
import experiment.common.Parameters;
import experiment.common.Description;
import experiment.common.Label;
import java.io.IOException;
import models.AbstractModelThread;
import pipeline.signals.FinishSignal;
import pipeline.signals.PipelineSignal;
import pipeline.signals.StartSignal;
import vector.IntegerVector;
import vector.SparseFloatVector;
import vector.complex.DistributionOfVectors;

public class SvdApache extends AbstractApacheModel{

    public RealMatrix u, vt;
	public double[] s, es;
    
    public SvdApache(){
        
    }
    
    public void truncate(int k){
        RealMatrix u1 = u.getSubMatrix(0, u.getRowDimension()-1, 0, k-1);
        double[] s1 = new double[k];
        System.arraycopy(s, 0, s1, 0, k);
        //RealMatrix s1 = s.getSubMatrix(0, k-1, 0, k-1);
        RealMatrix vt1 = vt.getSubMatrix(0, k-1, 0, vt.getColumnDimension()-1);
        u = u1;
        s = s1;
        vt = vt1;
    }
    
    /*public static double frobeniusDistance(RealMatrix m1, RealMatrix m2){
        return m1.add(m2.scalarMultiply(-1)).getFrobeniusNorm();
    }

    private void printMatrix(String matrixName, RealMatrix m){
        System.out.println("Matrix " + matrixName + ":");
        for(int i=0; i<m.getRowDimension(); i++){
            double[] row = m.getRow(i);
            String s = "";
            for(int j=0; j<row.length; j++){
                double value = row[j];
                s += "\t" + ((float) value);
            }
            System.out.println(s);
        }
    }
    */
	
	private double[] getExponentiatedSingularValues(double exponent){
		double[] es1 = new double[s.length];
		for(int i=0; i<s.length; i++){
			es1[i] = Math.pow(s[i], exponent);
		}
		
		return es1;
	}
	
	public DistributionOfVectors getDistributionOfVectors(){
        SingularValueDecomposition decomp = new SingularValueDecomposition(dataset);
        u = decomp.getU();
        s = decomp.getSingularValues();
        //s = decomp.getS();
		es = getExponentiatedSingularValues(Parameters.getDoubleParamter("singular values exponent"));
        vt = decomp.getVT();
		
		int k = Parameters.getIntParamter("k");
		truncate(k); //not always necessary
		
		
		//output = new MeaningRepresentation[amountOfAddedDataVectors];
		/*int i=0;
		for(Word targetWord : Vocabulary.asIterable()){
			double[] uRow = u.getRow(i);
			for(int d=0; d<k; d++) uRow[d] *= es[d];
			//output[i] = new SparseFloatVector(uRow);
			targetWord.bagOfMeaningRepresentations.put(outputDescription, new SparseFloatVector(uRow));
			i++;
		}
		*/

        DistributionOfVectors dov = new DistributionOfVectors();
        for(int i=0; i<k; i++){
            double[] vtRow = vt.getRow(i);
			//for(int d=0; d<k; d++) vtRow[d] *= es[d]; //if weights are to be applied to the output vectors directly
			SparseFloatVector outputVector = new SparseFloatVector(vtRow);
            dov.addWeightedVector(outputVector, (float) es[i]);
        }
        
        return dov;
    }

    @Override
    public Description getDescription() {
        Description d = super.getDescription();
		d.addParameter("type", "svd");
		//TODO model parameters
        
        return d;
    }


    @Override
    public void signalModel(Label label, PipelineSignal signal) throws IOException{
        if(signal instanceof StartSignal){
            signalPipeline(label, signal);
            
        }else if(signal instanceof IntegerVector){
            IntegerVector countVector = (IntegerVector) signal;
            addDataVector(countVector);
            
        }else if(signal instanceof FinishSignal){
            DistributionOfVectors dov = getDistributionOfVectors();
            signalPipeline(label, dov);
            signalPipeline(label, signal);
        }
    }

    @Override
    public void signalSuperior(AbstractModelThread thread, Label label, PipelineSignal signal) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }	

    @Override
    protected AbstractModelThread createThread(Label threadLabel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void startModel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void finishModel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String protocol() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	
}