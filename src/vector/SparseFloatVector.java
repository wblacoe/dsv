package vector;

import experiment.AbstractExperiment;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import experiment.common.Description;
import java.io.BufferedWriter;

public class SparseFloatVector extends FloatVector{

    private int dimensionality;
    private TreeMap<Integer, Float> weights;
    
    public SparseFloatVector(int dimensionality){
        this.dimensionality = dimensionality;
		weights = new TreeMap<>();
    }
	public SparseFloatVector(float[] weights){
		this(weights.length);
		for(int d=0; d<weights.length; d++){
			if(weights[d] != 0) set(d, weights[d]);
		}
	}
	public SparseFloatVector(double[] weights){
		this(weights.length);
		for(int d=0; d<weights.length; d++){
			if(weights[d] != 0) set(d, (float) weights[d]);
		}
	}
    
    
    @Override
    public Integer getDimensionality() {
        return dimensionality;
    }

    @Override
    public Number get(int dimension) {
        return weights.get(dimension);
    }

    @Override
    public boolean set(int dimension, Number weight) {
        if(dimension >= dimensionality){
            return false;
        }else{
			if(weight.floatValue() == 0f){
				weights.remove(dimension);
			}else{
				weights.put(dimension, weight.floatValue());
			}
            return true;
        }
    }
    
    @Override
    public boolean isZero(){
        if(weights.isEmpty()) return true;

        for(Float weight : weights.values()){
            if(!weight.equals(0f)) return false;
        }
        return true;
    }
	
	@Override
	public void setToZero(){
		weights.clear();
	}

    @Override
    public Float dot(AbstractVector v) {
        if(isZero() || v.isZero()) return 0f;
        
        SparseFloatVector sparseVector = (SparseFloatVector) v;
        float sum = 0f;
		
        Iterator<VectorEntry> thisIter = this.iterator();
        Iterator<VectorEntry> givenIter = sparseVector.iterator();
        VectorEntry thisEntry = thisIter.next();
        VectorEntry givenEntry = givenIter.next();
        Integer thisDimension = thisEntry.getDimension();
        Integer givenDimension = givenEntry.getDimension();
        
        while(true){
            int c = thisDimension.compareTo(givenDimension);
            if(c < 0){
                thisEntry = thisIter.next();
                thisDimension = thisEntry.getDimension();
            }else if(c > 0){
                givenEntry = givenIter.next();
                givenDimension = givenEntry.getDimension();
            }else{
                sum += thisEntry.getFloatValue() * givenEntry.getFloatValue();
                if(!thisIter.hasNext() || !givenIter.hasNext()) break;
                thisEntry = thisIter.next();
                givenEntry = givenIter.next();
                thisDimension = thisEntry.getDimension();
                givenDimension = givenEntry.getDimension();
            }
        }
        
        return sum;
    }

    @Override
    public boolean dotIsLargerThan(AbstractVector v, Number threshold) {
        return dot(v) > threshold.floatValue();
    }

    @Override
    public boolean dotIsPositive(AbstractVector v) {
        return dot(v) > 0;
    }

    public SparseFloatVector minusWrong(AbstractVector v) {
        SparseFloatVector resultVector = new SparseFloatVector(dimensionality);
        
        SparseFloatVector sparseVector = (SparseFloatVector) v;
        float sum = 0f;
        
        Iterator<VectorEntry> thisIter = this.iterator();
        Iterator<VectorEntry> givenIter = sparseVector.iterator();
        VectorEntry thisEntry = thisIter.next();
        VectorEntry givenEntry = givenIter.next();
        Integer thisDimension = thisEntry.getDimension();
        Integer givenDimension = givenEntry.getDimension();
        Float thisValue, givenValue;
        
        while(true){
            int c = thisDimension.compareTo(givenDimension);
            if(c < 0){
                thisEntry = thisIter.next();
                thisDimension = thisEntry.getDimension();
                thisValue = thisEntry.getFloatValue();
                resultVector.set(thisDimension, thisValue);
            }else if(c > 0){
                givenEntry = givenIter.next();
                givenDimension = givenEntry.getDimension();
                givenValue =  givenEntry.getFloatValue();
                resultVector.set(givenDimension, -givenValue);
            }else{
                sum +=  thisEntry.getFloatValue() *  givenEntry.getFloatValue();
                if(!thisIter.hasNext() || !givenIter.hasNext()) break;
                thisEntry = thisIter.next();
                givenEntry = givenIter.next();
                thisDimension = thisEntry.getDimension();
                givenDimension = givenEntry.getDimension();
                thisValue =  thisEntry.getFloatValue();
                givenValue =  givenEntry.getFloatValue();
                resultVector.set(thisDimension, thisValue - givenValue);
            }
        }
        
        return resultVector;
    }

    public SparseFloatVector plusWrong(AbstractVector v) {
        SparseFloatVector resultVector = new SparseFloatVector(dimensionality);
        
        SparseFloatVector sparseVector = (SparseFloatVector) v;
        float sum = 0f;
        
        Iterator<VectorEntry> thisIter = this.iterator();
        Iterator<VectorEntry> givenIter = sparseVector.iterator();
        VectorEntry thisEntry = thisIter.next();
        VectorEntry givenEntry = givenIter.next();
        Integer thisDimension = thisEntry.getDimension();
        Integer givenDimension = givenEntry.getDimension();
        Float thisValue =  thisEntry.getFloatValue();
        Float givenValue =  givenEntry.getFloatValue();
        
        while(true){
            int c = thisDimension.compareTo(givenDimension);
            if(c < 0){
                thisEntry = thisIter.next();
                thisDimension = thisEntry.getDimension();
                thisValue =  thisEntry.getFloatValue();
                resultVector.set(thisDimension, thisValue);
            }else if(c > 0){
                givenEntry = givenIter.next();
                givenDimension = givenEntry.getDimension();
                givenValue =  givenEntry.getFloatValue();
                resultVector.set(givenDimension, givenValue);
            }else{
                sum +=  thisEntry.getFloatValue() *  givenEntry.getFloatValue();
                if(!thisIter.hasNext() || !givenIter.hasNext()) break;
                thisEntry = thisIter.next();
                givenEntry = givenIter.next();
                thisDimension = thisEntry.getDimension();
                givenDimension = givenEntry.getDimension();
                thisValue =  thisEntry.getFloatValue();
                givenValue =  givenEntry.getFloatValue();
                resultVector.set(thisDimension, thisValue + givenValue);
            }
        }
        
        return resultVector;
    }
	
	@Override
    public SparseFloatVector minus(AbstractVector v) {
        SparseFloatVector resultVector = new SparseFloatVector(dimensionality);
		SparseFloatVector givenVector = (SparseFloatVector) v;
		
		for(VectorEntry ve : this){
			resultVector.add(ve.getDimension(), ve.getValue());
		}
		
		for(VectorEntry ve : givenVector){
			resultVector.subtract(ve.getDimension(), ve.getValue());
		}
        
		return resultVector;
	}
	
	@Override
    public SparseFloatVector plus(AbstractVector v) {
        SparseFloatVector resultVector = new SparseFloatVector(dimensionality);
		SparseFloatVector givenVector = (SparseFloatVector) v;
		
		for(VectorEntry ve : this){
			resultVector.add(ve.getDimension(), ve.getValue());
		}
		
		for(VectorEntry ve : givenVector){
			resultVector.add(ve.getDimension(), ve.getValue());
		}
        
		return resultVector;
	}

    @Override
    public boolean add(int dimension, Number weight) {
        Number existingWeight = get(dimension);
        if(existingWeight == null){
            set(dimension, weight);
        }else{
            set(dimension, (Float) existingWeight + (Float) weight);
        }
        
        return true;
    }
    
    @Override
    public boolean add(AbstractVector v) {
        for(VectorEntry ve : v){
            if(!add(ve.getDimension(), ve.getValue().floatValue())){
                return false;
            }
        }
        
        return true;
    }

    @Override
    public boolean subtract(int dimension, Number weight) {
        if(dimension >= dimensionality) return false;
        
        Number existingWeight = get(dimension);
        if(existingWeight == null){
            set(dimension, weight);
        }else{
            set(dimension, (Float) existingWeight - (Float) weight);
        }
        
        return true;
    }

    @Override
    public SparseFloatVector times(Number scalar) {
        float s = scalar.floatValue();
        SparseFloatVector resultVector = new SparseFloatVector(dimensionality);
        
        for(VectorEntry entry : this){
            resultVector.set(entry.getDimension(), (Float) entry.getValue() * s);
        }
        
        return resultVector;
    }
    
    //relative cardinality is assumed to be between 0 and 1
    public static SparseFloatVector createRandom(int dimensionality, float relativeCardinality){
        SparseFloatVector v = new SparseFloatVector(dimensionality);
        
        for(int d=0; d<dimensionality; d++){
            if(Math.random() < relativeCardinality){
                v.set(d, (float) (Math.random() - 0.5));
            }
        }
        
        return v;
    }
    
    public static SparseFloatVector importFrom(String line, int dimensionality) throws IOException{
        SparseFloatVector v = new SparseFloatVector(dimensionality);
        
        if(line.isEmpty()) return v;
        String[] entries = line.split(" ");
        if(entries.length > dimensionality) return null;
        for(int d=0; d<entries.length; d++){
            String[] dimensionAndValue = entries[d].split(":");
            Integer dimension = Integer.parseInt(dimensionAndValue[0]);
            Float value = Float.parseFloat(dimensionAndValue[1]);
            v.set(dimension, value);
        }
        
        return v;
    }
    
    public static SparseFloatVector importFrom(BufferedReader in, int dimensionality) throws IOException{
        String line = in.readLine();
        if(line == null) return null;
        return importFrom(line, dimensionality);
    }
    
	@Override
	public Iterator<VectorEntry> iterator() {
		return new Iterator<VectorEntry>() {

			Iterator<Entry<Integer, Float>> it = weights.entrySet().iterator();
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public VectorEntry next() {
				Entry<Integer, Float> entry = it.next();
				VectorEntry ve = new VectorEntry(entry.getKey(), entry.getValue());
				return ve;
			}
		};
	}

    @Override
    public Description getDescription(){
        Description d = super.getDescription();
        d.addParameter("density", "sparse");
        d.addParameter("dimensionality", "" + getDimensionality());
        
        return d;
    }
    
    public static SparseFloatVector create(Description d){
        int dimensionality = Integer.parseInt(d.getParameterValue("dimensionality"));
        return new SparseFloatVector(dimensionality);
    }
	
	@Override
	public SparseFloatVector getCopy() {
		SparseFloatVector v = new SparseFloatVector(dimensionality);
		for(VectorEntry ve : this){
			v.add(ve.getDimension(),  ve.getValue());
		}
		
		return v;
	}

	@Override
	public boolean isDense() {
		return false;
	}
    
    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        for(VectorEntry ve : this){
            writer.write(ve.getDimension() + ":" + ve.getFloatValue() + " ");
        }
        writer.write("\n");
        return true;
    }

    @Override
    public String asString(Object o) {
        boolean pretty = (o instanceof String && ((String) o).equals("pretty"));
        
        String s = "";
        int until = Math.min(AbstractVector.MAX_TEXT_LENGTH, getDimensionality());
        int i=0, d;
        for(VectorEntry ve : this){
            d = ve.getDimension();
            s += (pretty ? AbstractExperiment.getContextElement(d) : d) + ":" + ve.getFloatValue() + " ";
            i++;
            if(i >= until) break;
        }
        
        return s + "...";
    }
    
}