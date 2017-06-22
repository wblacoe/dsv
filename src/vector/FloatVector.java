package vector;

import java.io.BufferedReader;
import java.io.IOException;
import experiment.common.Description;

public abstract class FloatVector extends AbstractVector {

    public static FloatVector create(boolean areCountVectorsSparse, int dimensionality){
        if(areCountVectorsSparse){
            return new SparseFloatVector(dimensionality);
        }else{
            return new DenseFloatVector(dimensionality);
        }
    }

    @Override
    public Description getDescription(){
        Description d = super.getDescription();
        d.addParameter("field", "float");
        
        return d;
	}
    
    public static FloatVector importFrom(String line, Description d) throws IOException{
        FloatVector v;
        int dimensionality = Integer.parseInt(d.getParameterValue("dimensionality"));
        switch(d.getParameterValue("density")){
            case "dense":
                v = DenseFloatVector.importFrom(line, dimensionality);
                break;
            case "sparse":
                v = SparseFloatVector.importFrom(line, dimensionality);
                break;
            default:
                v = null;
        }
        
        return v;
    }
    
    public static FloatVector importFrom(BufferedReader in, Description d) throws IOException{
        FloatVector v;
        int dimensionality = Integer.parseInt(d.getParameterValue("dimensionality"));
        switch(d.getParameterValue("density")){
            case "dense":
                v = DenseFloatVector.importFrom(in, dimensionality);
                break;
            case "sparse":
                v = SparseFloatVector.importFrom(in, dimensionality);
                break;
            default:
                v = null;
        }
        
        return v;
    }

	public static FloatVector create(Description d){
		FloatVector v;
		switch(d.getParameterValue("density")){
			case "dense":
				v = DenseFloatVector.create(d);
				break;
            case "sparse":
				v = SparseFloatVector.create(d);
				break;
			default:
				v = null;
		}
		
		return v;
	}
    
}
