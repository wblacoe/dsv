package models.withDataset;

import meta.Copyable;
import models.AbstractFunction;
import vector.AbstractVector;
import vector.VectorEntry;
import vector.VectorPairEntry;
import vector.VectorsIntersection;
import vector.complex.MeaningRepresentation;

public class CompositionFunction extends AbstractFunction implements Copyable{
    
    public static final int ADDITION = 0;
    public static final int POINTWISE_MULTIPLICATION = 1;
    public static final int CONCATENATION = 2;
    public static final int CIRCULAR_CONVOLUTION = 3;
    public static final String[] COMPOSITION_FUNCTIONS = new String[]{ "addition", "pointwise multiplication", "concatenation", "circular convolution" };
    
    public int compositionFunction;

    public CompositionFunction(int compositionFunction){
        this.compositionFunction = compositionFunction;
    }
    
    public static CompositionFunction create(int compositionFunction){
        return new CompositionFunction(compositionFunction);
    }
    
    public static CompositionFunction create(String compositionFunction) {
        return create(getIndex(COMPOSITION_FUNCTIONS, compositionFunction));
    }
    
    
    private AbstractVector applyToVectors(AbstractVector v1, AbstractVector v2){
        AbstractVector r = null;
        
        switch(compositionFunction){
            
            case ADDITION:
                r = v1.plus(v2);
                break;
                
            case POINTWISE_MULTIPLICATION:
                (r = v1.getCopy()).setToZero();
                for(VectorPairEntry entry : VectorsIntersection.iterable(v1, v2)){
                    r.add(entry.getDimension(), entry.getFloatValue1() * entry.getFloatValue2());
                }
                break;
                
            case CONCATENATION:
                r = v1.getCopy();
                int d1 = v1.getDimensionality();
                for(VectorEntry entry : v2){
                    r.add(d1 + entry.getDimension(), entry.getValue());
                }
                break;
        }
        
        return r;
    }
    
    public MeaningRepresentation apply(MeaningRepresentation mr1, MeaningRepresentation mr2){
        MeaningRepresentation r = null;
        
        if(mr1 instanceof AbstractVector && mr2 instanceof AbstractVector){
            r = applyToVectors((AbstractVector) mr1, (AbstractVector) mr2);
        }
        
        
        return r;
    }
    
    @Override
    public Object getCopy(){
        return new CompositionFunction(compositionFunction);
    }
    
}
