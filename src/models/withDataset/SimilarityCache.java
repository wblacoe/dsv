package models.withDataset;

import experiment.common.Label;
import java.util.HashMap;
import vector.complex.MeaningRepresentation;

public class SimilarityCache {
    
    public SimilarityFunction similarityFunction; //not used yet
    private HashMap<String, Float> similarities;
    public HashMap<String, MeaningRepresentation> meaningRepresentations;
    
    public SimilarityCache(Label label, SimilarityFunction similarityFunction, HashMap<String, MeaningRepresentation> meaningReprentations){
        this.similarityFunction = similarityFunction;
        similarities = new HashMap<>();
        this.meaningRepresentations = meaningReprentations;
    }
    
    public SimilarityCache(Label label, SimilarityFunction similarityFunction){
        this.similarityFunction = similarityFunction;
        similarities = new HashMap<>();
        meaningRepresentations = new HashMap<>();
    }
    
    
    public SimilarityCache(SimilarityFunction similarityFunction){
        this(new Label(), similarityFunction);
    }
    
    public SimilarityCache(){
        this(new Label(), new SimilarityFunction());
    }
    
    
    private void saveSimilarity(String s1, String s2, Float similarity){
        String sortedConcatString = (s1.compareTo(s2) <= 0 ? s1 + "\t" + s2 : s2 + "\t" + s1);
        similarities.put(sortedConcatString, similarity);
    }
    
    public Float getSimilarity(MeaningRepresentation mr1, MeaningRepresentation mr2){
        return similarityFunction.apply(mr1, mr2);
    }
    
    public Float getSimilarity(String s1, MeaningRepresentation mr1, String s2, MeaningRepresentation mr2){
        String sortedConcatString = (s1.compareTo(s2) <= 0 ? s1 + "\t" + s2 : s2 + "\t" + s1);
        Float similarity = similarities.get(sortedConcatString);
        if(similarity == null){
            similarity = getSimilarity(mr1, mr2);
            saveSimilarity(s1, s2, similarity);
        }
        
        return similarity;
    }
    
    public Float getSimilarity(String s1, String s2){
        String sortedConcatString = (s1.compareTo(s2) <= 0 ? s1 + "\t" + s2 : s2 + "\t" + s1);
        Float similarity = similarities.get(sortedConcatString);
        if(similarity == null){
            MeaningRepresentation mr1 = meaningRepresentations.get(s1);
            MeaningRepresentation mr2 = meaningRepresentations.get(s2);
            similarity = getSimilarity(s1, mr1, s2, mr2);
        }
        
        return similarity;
    }
    
    
    
}
