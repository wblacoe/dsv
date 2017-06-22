package models.flat;

import edu.ucla.sspace.matrix.factorization.NonNegativeMatrixFactorizationMultiplicative;
import edu.ucla.sspace.matrix.Matrix;
import experiment.common.Parameters;
import experiment.common.Description;
import experiment.common.Label;
import java.io.IOException;
import models.AbstractModelThread;
import models.AbstractSspaceModel;
import pipeline.signals.FinishSignal;
import pipeline.signals.PipelineSignal;
import pipeline.signals.StartSignal;
import vector.IntegerVector;
import vector.SparseFloatVector;
import vector.complex.DistributionOfVectors;

//dataset matrix: each column is a datapoint (really not rows?)
public class NmfSspace extends AbstractSspaceModel{
    
    public Matrix w, h;
    
    public NmfSspace(){
        
    }
    
    public DistributionOfVectors getDistributionOfVectors(){
        NonNegativeMatrixFactorizationMultiplicative nmf = new NonNegativeMatrixFactorizationMultiplicative(1, 10); //nmf-mult parameters: inner loop, outer loop
        int k = Parameters.getIntParamter("k");
        nmf.factorize(dataset, k);
        w = nmf.dataClasses(); //each column is a probability vector over classes
        h = nmf.classFeatures(); //each row is a class representation vector over context words
        
        DistributionOfVectors dov = new DistributionOfVectors();
        for(int classIndex=0; classIndex<k; classIndex++){
            SparseFloatVector classVector = new SparseFloatVector(h.getRowVector(classIndex).toArray());
            dov.addWeightedVector(classVector, 1f); //uniform vector weights
        }
        
        return dov;
    }
    

    @Override
    public Description getDescription() {
        Description d = super.getDescription();
		d.addParameter("type", "nmf");
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