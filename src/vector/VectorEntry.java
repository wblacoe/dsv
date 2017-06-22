package vector;

public class VectorEntry {

	private int dimension;
	private Number value;
	
	public VectorEntry(int dimension, Number value){
		this.dimension = dimension;
		this.value = value;
	}
    
    
    public int getDimension(){
        return dimension;
    }
    
    public Number getValue(){
        return value;
    }
    
    public int getIntValue(){
        return value.intValue();
    }
    
    public float getFloatValue(){
        return value.floatValue();
    }
	
    public double getDoubleValue(){
        return value.doubleValue();
    }
	
}
