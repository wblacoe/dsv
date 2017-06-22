package vector;

public class VectorPairEntry {

    private int dimension;
	private Number value1, value2;
	
	public VectorPairEntry(int dimension, Number value1, Number value2){
		this.dimension = dimension;
		this.value1 = value1;
        this.value2 = value2;
	}
    
    public int getDimension(){
        return dimension;
    }
    
    public Number getValue1(){
        return value1;
    }
    
    public Number getValue2(){
        return value2;
    }
    
    public float getFloatValue1(){
        return value1.floatValue();
    }
    
    public float getFloatValue2(){
        return value2.floatValue();
    }

}
