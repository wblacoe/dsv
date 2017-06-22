package models.withDataset;

import meta.Copyable;
import models.AbstractFunction;
import vector.complex.MeaningRepresentation;

public class SimilarityFunction extends AbstractFunction implements Copyable{
    
    public static final int COSINE = 0;
    public static final String[] SIMILARITY_FUNCTIONS = new String[]{ "cosine" };
    
    public int similarityFunction;
    
    public SimilarityFunction(){ //default function is cosine
        similarityFunction = COSINE;
    }
    
    public SimilarityFunction(int similarityFunction){
        this.similarityFunction = similarityFunction;
    }
    
    public static SimilarityFunction create(int similarityFunction){
        return new SimilarityFunction(similarityFunction);
    }
    
    public static SimilarityFunction create(){
        return create(COSINE);
    }
    
    public static SimilarityFunction create(String similarityFunction) {
        return create(getIndex(SIMILARITY_FUNCTIONS, similarityFunction));
    }
    
    @Override
    public Object getCopy(){
        return new SimilarityFunction(similarityFunction);
    }
    
    
    public Float apply(MeaningRepresentation mr1, MeaningRepresentation mr2){
        Float r = null;
        
        if(similarityFunction == COSINE){
            r = mr1.similarity(mr2);
        }
        
        return r;
    }
    
}
