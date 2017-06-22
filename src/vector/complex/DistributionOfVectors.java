package vector.complex;

import experiment.common.Parameters;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import vector.AbstractVector;
import experiment.common.Description;

public class DistributionOfVectors extends MeaningRepresentation{
    
    public static final int ALL_SENSE_PAIRS = 0;
    public static final int ONLY_TOP_SENSE_PAIR = 1;
    public static final int ALIGNED_SENSES = 2;
    public static final String[] SIMILARITY_FUNCTIONS = new String[]{ "all sense pairs", "only top sense pair", "aligned senses" };
    
    private ArrayList<AbstractVector> vectors;
    private ArrayList<Float> vectorWeights;
    private Description contentDescription;
    
    public DistributionOfVectors(){
        vectors = new ArrayList<>();
        vectorWeights = new ArrayList<>();
        contentDescription = null;
    }

    public int getSize(){
        return vectors.size();
    }
    
    public AbstractVector getVector(int i){
        return vectors.get(i);
    }
    
    public void addWeightedVector(AbstractVector vector, float vectorWeight){
        vectors.add(vector);
        vectorWeights.add(vectorWeight);
        if(contentDescription == null) contentDescription = vector.getDescription();
    }
    
    public Float getVectorWeight(int i){
        return vectorWeights.get(i);
    }
    
    public static DistributionOfVectors importFrom(BufferedReader in, Description d) throws IOException{
        DistributionOfVectors dov = new DistributionOfVectors();
        
        String line;
        while((line = in.readLine()) != null && !line.isEmpty()){
            String[] entries = line.split("\t");
            float vectorWeight = Float.parseFloat(entries[0]);
            AbstractVector vector = AbstractVector.importFrom(entries[1], d);
            dov.addWeightedVector(vector, vectorWeight);
        }
            
        return dov;
    }
    
    @Override
    public Description getDescription(){
        Description d = new Description();
        d.setTypeAttribute("distribution of vectors");
        d.addParameter("size", "" + getSize());
        if(contentDescription != null){
            for(Description contentParam : contentDescription.getAllParameterObjectDescriptions()){
                d.addChild(contentParam);
            }
        }
        
        return d;
    }
    
    public static DistributionOfVectors create(Description d){
        //TODO
        return null;
    }
    
    //assumes that all vector weights are non-negative
    public void normaliseVectorWeights(){
        ArrayList<Float> normalisedVectorWeights = new ArrayList<>();
        float sum = 0f;
        for(Float vectorWeight : vectorWeights) sum += vectorWeight;
        for(Float vectorWeight : vectorWeights) normalisedVectorWeights.add(vectorWeight / sum);
        vectorWeights = normalisedVectorWeights;
    }
    
    public void normaliseVectors(){
        float p = Parameters.getFloatParamter("p norm");
        ArrayList<AbstractVector> normalisedVectors = new ArrayList<>();
        for(AbstractVector vector : vectors){
            normalisedVectors.add(vector.normalise(p));
        }
        vectors = normalisedVectors;
    }

	@Override
	public ArrayList<AbstractVector> getContainedVectors() {
		return vectors;
	}

	@Override
	public DistributionOfVectors getCopy() {
		DistributionOfVectors dov = new DistributionOfVectors();
		for(AbstractVector vector : vectors){
			dov.vectors.add(vector.getCopy());
		}
		for(Float vectorWeight : vectorWeights){
			dov.vectorWeights.add(vectorWeight);
		}
		//dov.contentDescription = contentDescription.getCopy();
		
		return dov;
	}
    
    public AbstractVector getWeightedSumOfVectors(){
        if(getSize() == 0) return null;
        
        AbstractVector sum = getVector(0).normalise(2).times(getVectorWeight(0));
        for(int i=1; i<getSize(); i++){
            sum.add(getVector(i).normalise(2).times(getVectorWeight(i)));
        }
        
        return sum;
    }

	@Override
	public Float similarity(MeaningRepresentation mr) {
        
        if(mr == null || !(mr instanceof DistributionOfVectors)) return null;
        DistributionOfVectors dov = (DistributionOfVectors) mr;
        
        if(getSize() == 0 || dov.getSize() == 0) return 0f;
        Float sim;
        
        int similarityType = 1;
        float tempSim, maxSim;
        //switch(Experiment.vectorDistributionSimilarityFunction){
        switch(similarityType){
            
            case 0: //problematic because sim(dov1,dov1) may be smaller than sim(dov1,dov2)
                AbstractVector s1 = getWeightedSumOfVectors();
                AbstractVector s2 = dov.getWeightedSumOfVectors();
                sim = s1.dot(s2).floatValue();
                break;
                
            case 1:
                tempSim = -1f;
                maxSim = -1f;
                for(int i1=0; i1<getSize(); i1++){
                    AbstractVector v1 = getVector(i1);
                    for(int i2=0; i2<dov.getSize(); i2++){
                        AbstractVector v2 = dov.getVector(i2);
                        tempSim = v1.similarity(v2);
                        if(tempSim > maxSim){
                            maxSim = tempSim;
                        }
                    }
                }
                sim = tempSim;
                break;

            case 2:
                tempSim = -1f;
                maxSim = -1f;
                float weightProduct = 0f;
                for(int i1=0; i1<getSize(); i1++){
                    AbstractVector v1 = getVector(i1);
                    for(int i2=0; i2<dov.getSize(); i2++){
                        AbstractVector v2 = dov.getVector(i2);
                        tempSim = v1.similarity(v2);
                        if(tempSim > maxSim){
                            maxSim = tempSim;
                            weightProduct = getVectorWeight(i1) * dov.getVectorWeight(i2);
                        }
                    }
                }
                sim = weightProduct * tempSim;
                break;

            case 3:
                tempSim = -1f;
                maxSim = -1f;
                for(int i1=0; i1<getSize(); i1++){
                    AbstractVector v1 = getVector(i1);
                    float vw1 = getVectorWeight(i1);
                    for(int i2=0; i2<dov.getSize(); i2++){
                        AbstractVector v2 = dov.getVector(i2);
                        float vw2 = dov.getVectorWeight(i2);
                        tempSim = v1.similarity(v2) * vw1 * vw2;
                        if(tempSim > maxSim){
                            maxSim = tempSim;
                        }
                    }
                }
                sim = tempSim;
                break;
                
            default:
                sim = null;
        }
        
        return sim;
	}

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        for(int i=0; i<getSize(); i++){
            writer.write(vectorWeights.get(i) + "\t");
            vectors.get(i).exportTo(writer);
        }
        writer.write("\n");
        return true;
    }

    @Override
    public String asString(Object o) {
        String s = "";
        for(int i=0; i<getSize(); i++){
            s += vectorWeights.get(i) + "\t" + vectors.get(i).asString(o);
        }
        
        return s;
    }
    
}