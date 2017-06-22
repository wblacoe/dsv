package vector;

import java.util.Iterator;

//represents the intersection of given vectors
public class VectorsIntersection implements Iterable<VectorPairEntry>{

    private AbstractVector v1, v2;
    
    public VectorsIntersection(AbstractVector v1, AbstractVector v2){
        this.v1 = v1;
        this.v2 = v2;
    }
    
    public static Iterator<VectorPairEntry> iterator(AbstractVector v1, AbstractVector v2){
        return (new VectorsIntersection(v1, v2)).iterator();
    }
    
    public static Iterable<VectorPairEntry> iterable(AbstractVector v1, AbstractVector v2){
        return new VectorsIntersection(v1, v2);
    }
    
    
    @Override
    //returns an iterator that iterates only over
    //vector pair entries <dim, v1[dim], v2[dim]>
    //where v1[dim] and v2[dim] are not zero
    public Iterator<VectorPairEntry> iterator() {
        return new Iterator<VectorPairEntry>() {
            
            Iterator<VectorEntry> it1 = v1.iterator();
            Iterator<VectorEntry> it2 = v2.iterator();
            VectorEntry ve1, ve2;
            Integer dim1, dim2;
            
            VectorPairEntry next = null;
            
            private boolean update1(){
                if(it1.hasNext()){
                    ve1 = it1.next();
                    dim1 = ve1.getDimension();
                    return true;
                }else{
                    return false;
                }
            }
            
            private boolean update2(){
                if(it2.hasNext()){
                    ve2 = it2.next();
                    dim2 = ve2.getDimension();
                    return true;
                }else{
                    return false;
                }
            }

            @Override
            public boolean hasNext() {
                
                Boolean hasNext = null;
                
                if(!update1() || !update2()){
                    next = null;
                    hasNext = false;
                }

                int c;
                while(hasNext == null){
                    c = dim1.compareTo(dim2);
                    if(c == 0){
                        next = new VectorPairEntry(dim1, ve1.getValue(), ve2.getValue());
                        hasNext = true;
                    }else if(c < 0 && !update1()){
                        next = null;
                        hasNext = false;
                    }else if(c > 0 && !update2()){
                        next = null;
                        hasNext = false;
                    }
                }
                
                return hasNext;
            }

            @Override
            public VectorPairEntry next() {
                return next;
            }
        };
    }

}