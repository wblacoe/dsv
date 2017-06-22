package vector;

import java.io.BufferedReader;
import java.io.IOException;
import experiment.common.Description;

public abstract class IntegerVector extends AbstractVector {
 
    public static IntegerVector create(boolean areCountVectorsSparse, int dimensionality){
        if(areCountVectorsSparse){
            return new SparseIntegerVector(dimensionality);
        }else{
            return new DenseIntegerVector(dimensionality);
        }
    }

    @Override
    public Description getDescription(){
        Description d = super.getDescription();
        d.addParameter("field", "integer");
        
        return d;
    }
    
    public static IntegerVector importFrom(String line, Description d) throws IOException{
        IntegerVector v;
        int dimensionality = Integer.parseInt(d.getParameterValue("dimensionality"));
        switch(d.getParameterValue("density")){
            case "dense":
                v = DenseIntegerVector.importFrom(line, dimensionality);
                break;
            case "sparse":
                v = SparseIntegerVector.importFrom(line, dimensionality);
                break;
            default:
                v = null;
        }
        
        return v;
    }
    
    public static IntegerVector importFrom(BufferedReader in, Description d) throws IOException{
        IntegerVector v;
        int dimensionality = Integer.parseInt(d.getParameterValue("dimensionality"));
        switch(d.getParameterValue("density")){
            case "dense":
                v = DenseIntegerVector.importFrom(in, dimensionality);
                break;
            case "sparse":
                v = SparseIntegerVector.importFrom(in, dimensionality);
                break;
            default:
                v = null;
        }
        
        return v;
    }

	public static IntegerVector create(Description d){
		IntegerVector v;
		switch(d.getParameterValue("density")){
			case "dense":
				v = DenseIntegerVector.create(d);
				break;
            case "sparse":
				v = SparseIntegerVector.create(d);
				break;
			default:
				v = null;
		}
		
		return v;
	}
	
}
