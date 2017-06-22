package integerset;

import java.util.Arrays;

public class SetOfIntegers extends AbstractSetOfIntegers{

	private boolean[] containedIndices;
	
	public SetOfIntegers(int size){
		containedIndices = new boolean[size];
	}
	public SetOfIntegers(int size, boolean isSetComplete){
		this(size);
		if(isSetComplete) Arrays.fill(containedIndices, true);
	}
    public SetOfIntegers(int size, int[] containedIndices){
        this(size);
        addAll(containedIndices);
    }
	
	public boolean[] getIndices(){
		return containedIndices;
	}
	
	public int getCapacity(){
		return containedIndices.length;
	}
	
	@Override
	public boolean isEmpty(){
		for(boolean b : containedIndices){
			if(b) return false;
		}
		return true;
	}
	
	public boolean add(int index){
        if(index < getCapacity()){
            containedIndices[index] = true;
            return true;
        }else{
            return false;
        }
	}
	
	public boolean addAll(SetOfIntegers indices){
		if(getCapacity() == indices.getCapacity()){
			for(int i=0; i<indices.getCapacity(); i++){
				if(indices.contains(i)) add(i);
			}
			return true;
		}else{
			//System.out.println(getCapacity() + " ? " + indices.getCapacity()); //DEBUG
			return false;
		}
	}
    
    public boolean addAll(int[] indices){
        for(int i=0; i<indices.length; i++){
            if(!add(indices[i])) return false;
        }
        return true;
    }
	
	public boolean unifyWith(SetOfIntegers indices){
		return addAll(indices);
	}
	
	public boolean intersectWith(SetOfIntegers indices){
		if(getCapacity() == indices.getCapacity()){
			for(int i=0; i<indices.getCapacity(); i++){
				if(!indices.contains(i)) remove(i);
			}
			return true;
		}else{
			//System.out.println(getCapacity() + " ? " + indices.getCapacity()); //DEBUG
			return false;
		}
	}
	
	public void remove(int index){
		containedIndices[index] = false;
	}
	
	public void removeAll(SetOfIntegers indices){
		if(this.getCapacity() == indices.getCapacity()){
			for(int i=0; i<indices.getCapacity(); i++){
				if(indices.contains(i)) remove(i);
			}
		}
	}
	
	public boolean contains(int index){
		return containedIndices[index];
	}
	
	@Override
	public Integer getSize(){
		int size = 0;
		for(boolean b : containedIndices){
			if(b) size++;
		}
		return size;
	}
	
	public int[] toIntArray(){
		int size = getSize();
		int[] intArray = new int[size];
		int j=0;
		for(int i=0; i<getCapacity(); i++){
			if(contains(i)){
				intArray[j] = i;
				j++;
			}
		}
		
		return intArray;
	}

    @Override
    public String asString(Object o) {
        String s = "{";
		for(int i=0; i<getCapacity(); i++){
			if(contains(i)) s += "" + i + ", ";
		}
		s += "}";
        
		return s;
    }
	
}
