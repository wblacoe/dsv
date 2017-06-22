package vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import experiment.common.Description;
import java.util.Iterator;
import vector.complex.MeaningRepresentation;

public abstract class AbstractVector extends MeaningRepresentation implements Iterable<VectorEntry> {
    
    //protected static final Pattern vectorPattern = Pattern.compile("<vector field=\"([^\"]*)\" density=\"([^\"]*)\" dimensionality=\"([^\"]*)\">");
    protected static final int MAX_TEXT_LENGTH = 8;
	
	public static final int DENSE = 0;
	public static final int SPARSE = 1;
	public static final String[] DENSITIES = new String[]{ "dense", "sparse" };
    
    //methods to be implemented
    public abstract Integer getDimensionality();
    public abstract Number get(int dimension);
    public abstract boolean set(int dimension, Number weight);
        
    public abstract boolean isZero();
	public abstract void setToZero();
    public abstract Number dot(AbstractVector v);
    public abstract boolean dotIsLargerThan(AbstractVector v, Number threshold);
    public abstract boolean dotIsPositive(AbstractVector v);
    public abstract AbstractVector minus(AbstractVector v);
    public abstract AbstractVector plus(AbstractVector v);
    public abstract boolean add(int dimension, Number weight);
    public abstract boolean add(AbstractVector v);
    public abstract boolean subtract(int dimension, Number weight);
    public abstract AbstractVector times(Number scalar);
    
    public float manhattanNorm(){
		float sum = 0;
        for(VectorEntry ve : this){
            sum += Math.abs(ve.getFloatValue());
		}
		
		return sum;
	}
    
    
    public float norm(float p){
        if(p == 1f){
            return manhattanNorm();
        }else{
            double sum = 0.0;
            for(VectorEntry entry : this){
                sum += Math.pow(Math.abs(entry.getDoubleValue()), p);
            }
            return (float) Math.pow(sum, 1.0 / p);
        }
    }

    public AbstractVector normalise(float p) {
        return times(1/norm(p));
    }
	
    //cosine similarity
	public Number normalisedDot(AbstractVector v){
        
        Iterator<VectorEntry> it1 = iterator();
        Iterator<VectorEntry> it2 = v.iterator();
        if(!it1.hasNext() || !it2.hasNext()) return 0; //if either vector is zero

        VectorEntry ve1, ve2;
        float value1, value2;
        float innerProduct = 0f, euclideanNorm1 = 0f, euclideanNorm2 = 0f;

        ve1 = it1.next();
        //value1 = ve1.value.floatValue();
        //euclideanNorm1 += value1 * value1;

        ve2 = it2.next();
        //value2 = ve2.value.floatValue();
        //euclideanNorm2 += value2 * value2;

        while(true){
            int c = Integer.compare(ve1.getDimension(), ve2.getDimension());
            
            if(c < 0){
                value1 = ve1.getFloatValue();
                euclideanNorm1 += value1 * value1;
                if(!it1.hasNext()) break;
                ve1 = it1.next();
                
            }else if(c > 0){
                value2 = ve2.getFloatValue();
                euclideanNorm2 += value2 * value2;
                if(!it2.hasNext()) break;
                ve2 = it2.next();
                
            }else{
                value1 = ve1.getFloatValue();
                euclideanNorm1 += value1 * value1;
                value2 = ve2.getFloatValue();
                euclideanNorm2 += value2 * value2;
                innerProduct += value1 * value2;
                if(!it1.hasNext()) break;
                ve1 = it1.next();
                if(!it2.hasNext()) break;
                ve2 = it2.next();
            }
        }

        return innerProduct / (float) Math.sqrt(euclideanNorm1 * euclideanNorm2);
	}
    
    @Override
    public Float similarity(MeaningRepresentation mr) {
        if(mr instanceof AbstractVector){
            return normalisedDot((AbstractVector) mr).floatValue();
        }else{
            return null;
        }
    }
    
    //@Override
    //public abstract Iterator<VectorEntry> iterator();
    
    public static AbstractVector importFrom(String line, Description d) throws IOException{
        AbstractVector v;
        switch(d.getParameterValue("field")){
            case "integer":
                v = IntegerVector.importFrom(line, d);
                break;
            case "float":
                v = FloatVector.importFrom(line, d);
                break;
            default:
                v = null;
        }
        
        return v;
    }
    
    public static AbstractVector importFrom(BufferedReader in, Description d) throws IOException{
        AbstractVector v;
        switch(d.getParameterValue("field")){
            case "integer":
                v = IntegerVector.importFrom(in, d);
                break;
            case "float":
                v = FloatVector.importFrom(in, d);
                break;
            default:
                v = null;
        }
        
        return v;
    }
    
    @Override
    public Description getDescription(){
        Description d = new Description();
        d.setTypeAttribute("vector");
        
        return d;
    }
	
	@Override
	public ArrayList<AbstractVector> getContainedVectors() {
		ArrayList<AbstractVector> containedVectors = new ArrayList<>();
		containedVectors.add(this);
		return containedVectors;
	}
	
	@Override
	public AbstractVector getCopy(){
		return getCopy();
	}
	
	public abstract boolean isDense();
    
    public static AbstractVector create(Description d){
        AbstractVector v;
        switch(d.getParameterValue("field")){
            case "integer":
                v = IntegerVector.create(d);
                break;
            case "float":
                v = FloatVector.create(d);
                break;
            default:
                v = null;
        }
        
        return v;
    }
	
}
