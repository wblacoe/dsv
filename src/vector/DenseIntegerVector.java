package vector;

import experiment.AbstractExperiment;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import experiment.common.Description;
import java.io.BufferedWriter;

public class DenseIntegerVector extends IntegerVector{
    
	private int[] weights;
	
    public DenseIntegerVector(int dimensionality){
        weights = new int[dimensionality];
    }
    /*public DenseVector(int[] weights){
        this.weights = weights;
    }
    */
    
    @Override
    public Integer getDimensionality(){
        if(weights == null) return null;
        
        return weights.length;
    }
    
    @Override
    public Integer get(int dimension){
		if(weights == null) return null;

        return weights[dimension];
    }
    
    @Override
    //dimensions start with 0
    public boolean set(int dimension, Number weight){
        if(weights == null || weight == null || !(weight instanceof Integer)) return false;
        if(dimension >= getDimensionality()) return false;
        
        weights[dimension] = weight.intValue();
        return true;
    }
    
    @Override
    public boolean isZero(){
        for(int weight : weights){
            if(weight != 0) return false;
        }
        return true;
    }
	
	@Override
	public void setToZero(){
		for(int i=0; i<weights.length; i++){
			weights[i] = 0;
		}
	}
    
    @Override
    public Integer dot(AbstractVector v){
        if(v == null || !(v instanceof DenseIntegerVector) || !v.getDimensionality().equals(getDimensionality())) return null;
            
        Integer sum = 0;
        for(int d=0; d<v.getDimensionality(); d++){
            sum += get(d) * v.get(d).intValue();
        }
        
        return sum;
    }
    
    @Override
    public boolean dotIsLargerThan(AbstractVector v, Number threshold){
        return dot(v) > threshold.intValue();
    }
    
    @Override
    public boolean dotIsPositive(AbstractVector v){
        return dot(v) > 0;
    }
    
    @Override
    public DenseIntegerVector minus(AbstractVector v){
        if(v == null || !getDimensionality().equals(v.getDimensionality())) return null;
            
        DenseIntegerVector vv = new DenseIntegerVector(getDimensionality());
        for(int d=0; d<getDimensionality(); d++){
            vv.set(d, get(d) - v.get(d).intValue());
        }
        
        return vv;
    }
    
    @Override
    public DenseIntegerVector plus(AbstractVector v){
        if(v == null || !getDimensionality().equals(v.getDimensionality())) return null;
            
        DenseIntegerVector vv = new DenseIntegerVector(getDimensionality());
        for(int d=0; d<getDimensionality(); d++){
            vv.set(d, get(d) + v.get(d).intValue());
        }
        
        return vv;
    }
    
    @Override
    public boolean add(int dimension, Number weight) {
        if(weights == null || weight == null || !(weight instanceof Integer)) return false;
        if(dimension >= getDimensionality()) return false;
        
        weights[dimension] += weight.intValue();
        return true;
    }
    
    @Override
    public boolean add(AbstractVector v){
        if(v == null || !getDimensionality().equals(v.getDimensionality())) return false;
            
        for(int d=0; d<getDimensionality(); d++){
            add(d, v.get(d).intValue());
        }
        
        return true;
    }

    @Override
    public boolean subtract(int dimension, Number weight) {
        if(weights == null || weight == null || !(weight instanceof Integer)) return false;
        if(dimension >= getDimensionality()) return false;
        
        weights[dimension] -= weight.intValue();
        return true;
    }

    
    @Override
    public AbstractVector times(Number scalar){
		AbstractVector v;
		
		if(scalar instanceof Integer){
			int s = scalar.intValue();
			v = new DenseIntegerVector(getDimensionality());
			for(int d=0; d<getDimensionality(); d++){
				v.set(d, get(d) * s);
			}
		}else if(scalar instanceof Float){
			float s = scalar.floatValue();
			v = new DenseFloatVector(getDimensionality());
			for(int d=0; d<getDimensionality(); d++){
				v.set(d, get(d) * s);
			}
		}else{
			v = null;
		}
        
        return v;
    }

    //relative cardinality is assumed to be between 0 and 1
    public static DenseIntegerVector createRandom(int dimensionality, float relativeCardinality){
        DenseIntegerVector v = new DenseIntegerVector(dimensionality);
        
        for(int d=0; d<dimensionality; d++){
            if(Math.random() < relativeCardinality){
                v.set(d, (int) ((Math.random() - 0.5) * 1000));
            }
        }
        
        return v;
    }
    
    public static DenseIntegerVector importFrom(String line, int dimensionality) throws IOException{
        DenseIntegerVector v = new DenseIntegerVector(dimensionality);
        
        if(line.isEmpty()) return v;
        Integer value;
        String[] entries = line.split(" ");
        if(entries.length != dimensionality) return null;
        for(int d=0; d<dimensionality; d++){
            value = Integer.parseInt(entries[d]);
            v.weights[d] = value;
            d++;
        }
        
        return v;
    }
    
    public static DenseIntegerVector importFrom(BufferedReader in, int dimensionality) throws IOException{
        String line = in.readLine();
        if(line == null) return null;
        return importFrom(line, dimensionality);
    }
    
    @Override
	public Iterator<VectorEntry> iterator() {
		return new Iterator<VectorEntry>() {

			int i=0;
			
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
    
    public static DenseIntegerVector create(Description d){
        int dimensionality = Integer.parseInt(d.getParameterValue("dimensionality"));
        return new DenseIntegerVector(dimensionality);
    }

	@Override
	public DenseIntegerVector getCopy() {
		DenseIntegerVector v = new DenseIntegerVector(getDimensionality());
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