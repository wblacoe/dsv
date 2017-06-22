package vector.complex;

import experiment.common.Description;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import meta.Copyable;
import meta.Describable;
import meta.Exportable;
import meta.Helper;
import meta.Printable;
import pipeline.signals.PipelineSignal;
import vector.AbstractVector;

public abstract class MeaningRepresentation implements Exportable, Copyable, PipelineSignal, Describable, Printable{

    public static final int VECTOR = 0;
    public static final int DISTRIBUTION_OF_VECTORS = 1;
    public static final int VECTOR_TREE = 2;
    public static final String[] MEANING_STRUCTURES = new String[]{ "vector", "distribution of vectors", "vector tree" };

	public abstract ArrayList<AbstractVector> getContainedVectors();
    
    public static MeaningRepresentation importFrom(BufferedReader in, Description contentD) throws IOException{
        
        String meaningStructureString = contentD.getTypeAttribute();
        int meaningStructure = Helper.getIndex(MeaningRepresentation.MEANING_STRUCTURES, meaningStructureString);
        
        MeaningRepresentation mr;
        switch(meaningStructure){
        
            case MeaningRepresentation.VECTOR:
                mr = AbstractVector.importFrom(in, contentD);
                break;
                
            case MeaningRepresentation.DISTRIBUTION_OF_VECTORS:
                mr = DistributionOfVectors.importFrom(in, contentD);
                break;
                
            case MeaningRepresentation.VECTOR_TREE:
                mr = VectorTree.importFrom(in); //TODO
                break;
                
            default:
                mr = null;
                break;
        }
        
        return mr;
    }
    
    /*public static MeaningRepresentation importFrom(File f) throws IOException{
        BufferedReader in = new BufferedReader(new FileReader(f));
        MeaningRepresentation mr = importFrom(in);
        in.close();
        
        return mr;
    }
    */
	
	public abstract Float similarity(MeaningRepresentation mr);
    
}
