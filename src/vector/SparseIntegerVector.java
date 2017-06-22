package vector;

import experiment.AbstractExperiment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import experiment.common.Description;

public class SparseIntegerVector extends IntegerVector{
	
    private int dimensionality;
    private TreeMap<Integer, Integer> weights; //map from dimension index to integer value
    
    public SparseIntegerVector(int dimensionality){
        this.dimensionality = dimensionality;
		weights = new TreeMap<>();
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
			if(weight.intValue() == 0){
				weights.remove(dimension);
			}else{
				weights.put(dimension, (Integer) weight);
			}
            return true;
        }
    }
    
    @Override
    public boolean isZero(){
        if(weights.isEmpty()) return true;

        for(Integer weight : weights.values()){
            if(!weight.equals(0)) return false;
        }
        return true;
    }
	
	@Override
	public void setToZero(){
		weights.clear();
	}

    @Override
    public Integer dot(AbstractVector v) {
        if(isZero() || v.isZero()) return 0;
        
        SparseIntegerVector sparseVector = (SparseIntegerVector) v;
        int sum = 0;
        
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
                sum += thisEntry.getIntValue() * (Integer) givenEntry.getIntValue();
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

    public SparseIntegerVector minusWrong(AbstractVector v) {
        SparseIntegerVector resultVector = new SparseIntegerVector(dimensionality);
        
        SparseIntegerVector sparseVector = (SparseIntegerVector) v;
        
        Iterator<VectorEntry> thisIter = this.iterator();
        Iterator<VectorEntry> givenIter = sparseVector.iterator();
        VectorEntry thisEntry = thisIter.next();
        VectorEntry givenEntry = givenIter.next();
        Integer thisDimension = thisEntry.getDimension();
        Integer givenDimension = givenEntry.getDimension();
        Integer thisValue, givenValue;
        
        while(true){
            int c = thisDimension.compareTo(givenDimension);
            if(c < 0){
                thisEntry = thisIter.next();
                thisDimension = thisEntry.getDimension();
                thisValue = thisEntry.getIntValue();
                resultVector.set(thisDimension, thisValue);
            }else if(c > 0){
                givenEntry = givenIter.next();
                givenDimension = givenEntry.getDimension();
                givenValue = givenEntry.getIntValue();
                resultVector.set(givenDimension, -givenValue);
            }else{
                if(!thisIter.hasNext() || !givenIter.hasNext()) break;
                thisEntry = thisIter.next();
                givenEntry = givenIter.next();
                thisDimension = thisEntry.getDimension();
                givenDimension = givenEntry.getDimension();
                thisValue = thisEntry.getIntValue();
                givenValue = givenEntry.getIntValue();
                resultVector.set(thisDimension, thisValue - givenValue);
            }
        }
        
        return resultVector;
    }

    public SparseIntegerVector plusWrong(AbstractVector v) {
        SparseIntegerVector resultVector = new SparseIntegerVector(dimensionality);
        
        SparseIntegerVector sparseVector = (SparseIntegerVector) v;
        
        Iterator<VectorEntry> thisIter = this.iterator();
        Iterator<VectorEntry> givenIter = sparseVector.iterator();
        VectorEntry thisEntry = thisIter.next();
        VectorEntry givenEntry = givenIter.next();
        Integer thisDimension = thisEntry.getDimension();
        Integer givenDimension = givenEntry.getDimension();
        Integer thisValue, givenValue;
        
        while(true){
            int c = thisDimension.compareTo(givenDimension);
            if(c < 0){
                thisEntry = thisIter.next();
                thisDimension = thisEntry.getDimension();
                thisValue = thisEntry.getIntValue();
                resultVector.set(thisDimension, thisValue);
            }else if(c > 0){
                givenEntry = givenIter.next();
                givenDimension = givenEntry.getDimension();
                givenValue =  givenEntry.getIntValue();
                resultVector.set(givenDimension, givenValue);
            }else{
                if(!thisIter.hasNext() || !givenIter.hasNext()) break;
                thisEntry = thisIter.next();
                givenEntry = givenIter.next();
                thisDimension = thisEntry.getDimension();
                givenDimension = givenEntry.getDimension();
                thisValue = thisEntry.getIntValue();
                givenValue = givenEntry.getIntValue();
                resultVector.set(thisDimension, thisValue + givenValue);
            }
        }
        
        return resultVector;
    }
	
	@Override
    public SparseIntegerVector minus(AbstractVector v) {
        SparseIntegerVector resultVector = new SparseIntegerVector(dimensionality);
		
		for(Object o : this){
			VectorEntry entry = (VectorEntry) o;
			resultVector.add(entry.getDimension(), entry.getValue());
		}
		
		for(Object o : v){
			VectorEntry entry = (VectorEntry) o;
			resultVector.subtract(entry.getDimension(), entry.getValue());
		}
        
		return resultVector;
	}
	
	@Override
    public AbstractVector plus(AbstractVector v) {
        AbstractVector resultVector;
		if(v instanceof IntegerVector){
			resultVector = new SparseIntegerVector(dimensionality);
		}else if(v instanceof FloatVector){
			resultVector = new SparseFloatVector(dimensionality);
		}else{
			return null;
		}
		
		for(VectorEntry ve : this){
			resultVector.add(ve.getDimension(), ve.getValue());
		}
		
		for(VectorEntry ve : v){
			resultVector.add(ve.getDimension(), ve.getValue());
		}
        
		return resultVector;
	}

    @Override
    public boolean add(int dimension, Number weight) {
        if(dimension >= dimensionality) return false;
        
        Number existingWeight = get(dimension);
        if(existingWeight == null){
            set(dimension, weight);
        }else{
            set(dimension, (Integer) existingWeight + (Integer) weight);
        }
        
        return true;
    }
    
    @Override
    public boolean add(AbstractVector v) {
        for(VectorEntry ve : v){
            if(!add(ve.getDimension(), ve.getIntValue())){
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
            set(dimension, (Integer) existingWeight - (Integer) weight);
        }
        
        return true;
    }

    @Override
    public AbstractVector times(Number scalar) {
		AbstractVector resultVector;
		
		if(scalar instanceof Integer){
			int s = scalar.intValue();
			resultVector = new SparseIntegerVector(dimensionality);

			for(VectorEntry entry : this){
				resultVector.set(entry.getDimension(), entry.getIntValue() * s);
			}
		}else if(scalar instanceof Float){
			float s = scalar.floatValue();
			resultVector = new SparseFloatVector(dimensionality);

			for(VectorEntry entry : this){
				resultVector.set(entry.getDimension(), entry.getIntValue() * s);
			}
		}else{
			resultVector = null;
		}
        
        return resultVector;
    }
    
    //relative cardinality is assumed to be between 0 and 1
    public static SparseIntegerVector createRandom(int dimensionality, float relativeCardinality){
        SparseIntegerVector v = new SparseIntegerVector(dimensionality);
        
        for(int d=0; d<dimensionality; d++){
            if(Math.random() < relativeCardinality){
                v.set(d, (int) (Math.random() * 100));
            }
        }
        
        return v;
    }
    
    public static SparseIntegerVector importFrom(String line, int dimensionality) throws IOException{
        SparseIntegerVector v = new SparseIntegerVector(dimensionality);
        
        if(line.isEmpty()) return v;
        String[] entries = line.split(" ");
        if(entries.length > dimensionality) return null;
        for(int d=0; d<entries.length; d++){
            String[] dimensionAndValue = entries[d].split(":");
            Integer dimension = Integer.parseInt(dimensionAndValue[0]);
            Integer value = Integer.parseInt(dimensionAndValue[1]);
            v.set(dimension, value);
        }
        
        return v;
    }
    
    public static SparseIntegerVector importFrom(BufferedReader in, int dimensionality) throws IOException{
        String line = in.readLine();
        if(line == null) return null;
        return importFrom(line, dimensionality);
    }
    
	@Override
	public synchronized Iterator<VectorEntry> iterator() {
		return new Iterator<VectorEntry>() {

			Iterator<Entry<Integer, Integer>> it = weights.entrySet().iterator();
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public VectorEntry next() {
				Entry<Integer, Integer> entry = it.next();
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
    
    public static SparseIntegerVector create(Description d){
        int dimensionality = Integer.parseInt(d.getParameterValue("dimensionality"));
        return new SparseIntegerVector(dimensionality);
    }

	@Override
	public SparseIntegerVector getCopy() {
		SparseIntegerVector v = new SparseIntegerVector(dimensionality);
		for(VectorEntry ve : this){
			v.add(ve.getDimension(), ve.getIntValue());
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
            writer.write(ve.getDimension() + ":" + ve.getIntValue() + " ");
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
            s += (pretty ? AbstractExperiment.getContextElement(d) : d) + ":" + ve.getIntValue() + " ";
            i++;
            if(i >= until) break;
        }
        
        return s + "...";
    }
    
}