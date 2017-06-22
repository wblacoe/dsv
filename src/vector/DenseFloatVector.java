package vector;

import experiment.AbstractExperiment;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import experiment.common.Description;
import java.io.BufferedWriter;

public class DenseFloatVector extends FloatVector {

    private float[] weights;
    
    public DenseFloatVector(int dimensionality){
        weights = new float[dimensionality];
    }
    public DenseFloatVector(float[] weights){
        this.weights = weights;
    }
    public DenseFloatVector(double[] weights){
        this.weights = new float[weights.length];
        for(int i=0; i<weights.length; i++) this.weights[i] = (float) weights[i];
    }
    
    
    @Override
    public Integer getDimensionality(){
        if(weights == null) return null;
        
        return weights.length;
    }
    
    @Override
    public Float get(int dimension){
        if(weights == null) return null;
        
        return weights[dimension];
    }
    
    @Override
    //dimensions start with 0
    public boolean set(int dimension, Number weight){
        if(weights == null || weight == null || !(weight instanceof Float)) return false;
        if(dimension >= getDimensionality()) return false;
        
        weights[dimension] = weight.floatValue();
        return true;
    }
    
    @Override
    public boolean isZero(){
        for(float weight : weights){
            if(weight != 0) return false;
        }
        return true;
    }
	
	@Override
	public void setToZero(){
		for(int i=0; i<weights.length; i++){
			weights[i] = 0f;
		}
	}
    
    @Override
    public Float dot(AbstractVector v){
        if(v == null || !(v instanceof DenseFloatVector) || !v.getDimensionality().equals(getDimensionality())) return null;
            
        Float sum = 0.0f;
        for(int d=0; d<v.getDimensionality(); d++){
            sum += get(d) * v.get(d).floatValue();
        }
        
        return sum;
    }
    
    @Override
    public boolean dotIsLargerThan(AbstractVector v, Number threshold){
        return dot(v) > threshold.floatValue();
    }
    
    @Override
    public boolean dotIsPositive(AbstractVector v){
        return dot(v) > 0;
    }
    
    @Override
    public DenseFloatVector minus(AbstractVector v){
        if(v == null || !getDimensionality().equals(v.getDimensionality())) return null;
            
        DenseFloatVector vv = new DenseFloatVector(getDimensionality());
        for(int d=0; d<getDimensionality(); d++){
            vv.set(d, get(d) - v.get(d).floatValue());
        }
        
        return vv;
    }
    
    @Override
    public DenseFloatVector plus(AbstractVector v){
        if(v == null || !getDimensionality().equals(v.getDimensionality())) return null;
            
        DenseFloatVector vv = new DenseFloatVector(getDimensionality());
        for(int d=0; d<getDimensionality(); d++){
            vv.set(d, get(d) + v.get(d).floatValue());
        }
        
        return vv;
    }
    
    @Override
    public boolean add(int dimension, Number weight) {
        if(weights == null || weight == null || !(weight instanceof Float)) return false;
        if(dimension >= getDimensionality()) return false;
        
        weights[dimension] += weight.floatValue();
        return true;
    }
    
    @Override
    public boolean add(AbstractVector v){
        if(v == null || !getDimensionality().equals(v.getDimensionality())) return false;
            
        for(int d=0; d<getDimensionality(); d++){
            add(d, v.get(d).floatValue());
        }
        
        return true;
    }

    @Override
    public boolean subtract(int dimension, Number weight) {
        if(weights == null || weight == null || !(weight instanceof Float)) return false;
        if(dimension >= getDimensionality()) return false;
        
        weights[dimension] -= weight.floatValue();
        return true;
    }

    
    @Override
    public DenseFloatVector times(Number scalar){
        float s = scalar.floatValue();
        DenseFloatVector v = new DenseFloatVector(getDimensionality());
        for(int d=0; d<getDimensionality(); d++){
            v.set(d, get(d) * s);
        }
        
        return v;
    }
	
    //relative cardinality is assumed to be between 0 and 1
    public static DenseFloatVector createRandom(int dimensionality, float relativeCardinality){
        DenseFloatVector v = new DenseFloatVector(dimensionality);
        
        for(int d=0; d<dimensionality; d++){
            if(Math.random() < relativeCardinality){
                v.set(d, (float) (Math.random() - 0.5));
            }
        }
        
        return v;
    }
    
    public static DenseFloatVector importFrom(String line, int dimensionality) throws IOException{
        DenseFloatVector v = new DenseFloatVector(dimensionality);
        
        if(line.isEmpty()) return v;
        Float value;
        String[] entries = line.split(" ");
        if(entries.length != dimensionality) return null;
        for(int d=0; d<dimensionality; d++){
            value = Float.parseFloat(entries[d]);
            v.weights[d] = value;
            d++;
        }
        
        return v;
    }
    
    public static DenseFloatVector importFrom(BufferedReader in, int dimensionality) throws IOException{
        String line = in.readLine();
        if(line == null) return null;
        return importFrom(line, dimensionality);
    }
    
	@Override
	public Iterator<VectorEntry> iterator() {
		return new Iterator<VectorEntry>() {
			
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < weights.length;
			}

			@Override
			public VectorEntry next() {
				if(hasNext()){
					VectorEntry ve = new VectorEntry(i, weights[i]);
					i++;
					return ve;
				}else{
					return null;
				}
			}
		};

	}
    
    @Override
    public Description getDescription(){
        Description d = super.getDescription();
        d.addParameter("density", "dense");
        d.addParameter("dimensionality", "" + getDimensionality());
        
        return d;
    }
    
    public static DenseFloatVector create(Description d){
        int dimensionality = Integer.parseInt(d.getParameterValue("dimensionality"));
        return new DenseFloatVector(dimensionality);
    }
    
	@Override
	public DenseFloatVector getCopy() {
		DenseFloatVector v = new DenseFloatVector(getDimensionality());
		for(VectorEntry ve : this){
			v.add(ve.getDimension(), ve.getValue());
		}
		
		return v;
	}

	@Override
	public boolean isDense() {
		return true;
	}
    
    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        for(int d=0; d<getDimensionality(); d++){
            writer.write(get(d) + " ");
        }
        writer.write("\n");
        return true;
    }

    @Override
    public String asString(Object o) {
        boolean pretty = (o instanceof String && ((String) o).equals("pretty"));
        
        String s = "";
        int until = Math.min(AbstractVector.MAX_TEXT_LENGTH, getDimensionality());
        for(int d=0; d<until; d++){
            s += (pretty ? AbstractExperiment.getContextElement(d) : d) + ":" + get(d) + " ";
        }
        
        return s + "...";
    }

}