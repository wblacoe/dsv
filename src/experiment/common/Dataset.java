package experiment.common;

import java.util.HashMap;
import java.util.Set;

public class Dataset extends HashMap<Integer, AbstractInstance>{
    
    public Dataset(){
        
    }
    
    public boolean hasInstance(Integer index){
        return containsKey(index);
    }
    
    public void setInstance(Integer index, AbstractInstance instance){
        put(index, instance);
    }
    
    public synchronized AbstractInstance getInstance(Integer index){
        return get(index);
    }
    
    public void addInstance(AbstractInstance instance){
        setInstance(size(), instance);
    }

    public Set<Integer> getIndicesSet(){
        return keySet();
    }
    
}
