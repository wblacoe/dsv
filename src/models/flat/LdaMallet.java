package models.flat;

import cc.mallet.topics.ParallelTopicModel;
import models.AbstractMalletModel;
import experiment.common.Parameters;
import experiment.common.Description;
import experiment.common.Label;
import java.io.IOException;
import models.AbstractModelThread;
import pipeline.signals.FinishSignal;
import pipeline.signals.PipelineSignal;
import pipeline.signals.StartSignal;
import vector.IntegerVector;
import vector.SparseFloatVector;
import vector.complex.DistributionOfVectors;

/**
 * takes the first [amountOfSentences] sentences from corpus file [corpusFile] to create a training set
 * each instance is the set of all words in a document (possibly ignoring stop words) (=alphabet) and their counts
 * LDA learns [amountOfTopics] many topic vectors from the training set
 * topic vectors are over all words in the alphabet
 * then this topics-words matrix gets transposed, giving us word vectors over topics
 * 
 * TODO: ppmi, consider all 4 types of contexts
 */
public class LdaMallet extends AbstractMalletModel {
    
    //double[][] topicWordMatrix;
    //double[] topicProbabilities; //use LDA alphas for this
    
    public LdaMallet(){
        //output
        //topicWordMatrix = null;
        //topicProbabilities = null;
    }
	
    //run LDA model and get output as distribution of vectors
    public DistributionOfVectors getDistributionOfVectors() throws IOException{
        ParallelTopicModel lda = new ParallelTopicModel(Parameters.getIntParamter("amount of topics"));
        lda.printLogLikelihood = true;
        lda.setTopicDisplay(50, 12);
        lda.addInstances(dataset);
        lda.setNumThreads(Parameters.getIntParamter("amount of threads"));
        //standard parameters used for alpha, beta and...
        //burn in period = 200
        //iterations = 1000
        lda.estimate();
        
        //lda.getTopicWords(,) returns raw counts in a topic-word matrix
        //smoothing=true adds the beta value to all entries
        //normalisation=true does some strange normalisation depending on alpha values (per topic)
        double[][] topicWordMatrix = lda.getTopicWords(false, true); //normalisation and/or smoothing?
        double[] topicProbabilities = lda.alpha;
        
        DistributionOfVectors dov = new DistributionOfVectors();
        for(int topicIndex=0; topicIndex<topicProbabilities.length; topicIndex++){
            double topicProbability = topicProbabilities[topicIndex];
            double[] topicWordVector = topicWordMatrix[topicIndex];
            SparseFloatVector outputVector = new SparseFloatVector(topicWordVector);
            dov.addWeightedVector(outputVector, (float) topicProbability);
        }
        
        return dov;
    }
    
    /*public void normaliseTopicVectors(){
        //go through all topics
        for(int i=0; i<topicWordMatrix.length; i++){
            //get topic vector
            double[] topicVector = topicWordMatrix[i];
            double normSquared = 0.0;
            for(int j=0; j<topicVector.length; j++){
                normSquared += topicVector[j] * topicVector[j];
            }
            double oneOverNorm = Math.pow(normSquared, -0.5);
            for(int j=0; j<topicVector.length; j++){
                topicVector[j] *= oneOverNorm; //divide entry by norm
            }
        }
    }
    
    public void compareTopicVectors(){
        for(int i=0; i<topicWordMatrix.length; i++){
            double[] topicVector1 = topicWordMatrix[i];
            for(int j=0; j<=i; j++){
                double[] topicVector2 = topicWordMatrix[j];
                double sim = 0.0;
                for(int d=0; d<topicVector1.length; d++){
                    sim += topicVector1[d] * topicVector2[d];
                }
                Helper.report("[LdaMallet] sim(topic" + i + ", topic" + j + ") = " + sim);
            }
        }
    }
    
    
    //print topic word matrix
    public void printOutput(){
        int amountOfRows = topicWordMatrix.length;
        for(int i=0; i<amountOfRows; i++){ //each row represents a topic and is a vector over words
            String s = "topic: topic" + i + ", prob: " + ((float) topicProbabilities[i]) + ", [";
            //for(int j=0; j<topicWordMatrix[i].length; j++){
            double normSquared = 0.0;
            for(int j=0; j<topicWordMatrix[i].length; j++){
                if(j < 10){
                    s += " " + ((String) alphabet.lookupObject(j)) + ":" + ((float) topicWordMatrix[i][j]);
                }
                normSquared += topicWordMatrix[i][j] * topicWordMatrix[i][j];
            }
            s += "], dim: " + topicWordMatrix[i].length + ", norm^2: " + ((float) normSquared) + ", norm: " + ((float) Math.sqrt(normSquared));
            System.out.println(s);
        }
    }
    
    //TODO: check word order in topic word matrix
    //uses Manhattan norm
    public void transposeAndNormaliseWordVectors(){
        int amountOfWords = Parameters.getIntParamter("data dimensionality");
        int amountOfTopics = Parameters.getIntParamter("amount of topics");
        double[][] normalisedWordTopicMatrix = new double[amountOfWords][];
        
        for(int i=0; i<amountOfWords; i++){
            double[] v = new double[amountOfTopics];
            double sum = 0f;
            for(int j=0; j<amountOfTopics; j++) sum += topicWordMatrix[j][i];
            for(int j=0; j<amountOfTopics; j++) v[j] = topicWordMatrix[j][i] / sum;
            normalisedWordTopicMatrix[i] = v;
        }
    }
    */

	/*public static void main(String[] args){
		Parameters.setParameter("amount of data points", 20);
		Parameters.setParameter("data dimensionality", 100000);
		Parameters.setParameter("amount of topics", 5);
		Parameters.setParameter("amount of threads", 2);
        
        LdaMallet exp = new LdaMallet(null, null);
        //exp.randomlyPopulateDataset(0.01f);
        exp.run();
		exp.printOutput();
		exp.normaliseTopicVectors();
		exp.printOutput();
		exp.compareTopicVectors();
	}
	*/

    @Override
    public Description getDescription() {
        Description d = super.getDescription();
		d.addParameter("type", "lda");
		//TODO model parameters
        
        return d;
    }

    @Override
    public void signalModel(Label label, PipelineSignal signal) throws IOException{
        
        if(signal instanceof StartSignal){
            signalPipeline(label, signal);
            
        }else if(signal instanceof IntegerVector){
            IntegerVector countVector = (IntegerVector) signal;
            addDataVector(countVector);
            
        }else if(signal instanceof FinishSignal){
            DistributionOfVectors dov = getDistributionOfVectors();
            signalPipeline(label, dov);
            signalPipeline(label, signal);
        }
        
    }

    @Override
    protected AbstractModelThread createThread(Label threadLabel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void startModel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void finishModel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String protocol() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}