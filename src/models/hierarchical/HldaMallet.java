/*package models.hierarchical;

import vector.complex.VectorTree;
import vector.complex.VectorNode;
import cc.mallet.util.Randoms;
import java.util.ArrayList;
import lingunit.flattext.Word;
import models.MalletModel;
import models.Parameters;
import models.hierarchical.HierarchicalLDA.NCRPNode;
import vector.DenseFloatVector;
import vocabulary.Description;

public class HldaMallet extends MalletModel {

    public VectorTree senseTree;
    
    public HldaMallet(ArrayList<Word> targetWords){
        super(targetWords);
    }

	@Override
    public void run(){
        //InstanceList trainingSet = getInstanceListFromCorpusFile(corpusFile, targetWord, amountOfSentences);
        //InstanceList trainingSet = getInstanceListFromDocument();
		//InstanceList trainingSet = getRandomSparseInstanceList(20, 100000, 1000);
        
        HierarchicalLDA hlda = new HierarchicalLDA();
		hlda.initialize(dataset, null, Parameters.getIntParamter("depth of topic tree"), new Randoms());
		hlda.estimate(Parameters.getIntParamter("amount of iterations"));
        
        senseTree = crpTree2VectorTree(hlda.rootNode);
    }
    
    private VectorTree crpTree2VectorTree(NCRPNode crpRootNode){
        VectorNode vectorRootNode = crpNode2VectorNode(crpRootNode);
        VectorTree vectorTree = new VectorTree(vectorRootNode);
        
        return vectorTree;
    }
    //transform a CRP tree into a Vector Tree top-down
    private VectorNode crpNode2VectorNode(NCRPNode crpNode){
        //create new vector node and use type counts as vector, TODO normalise counts?
        VectorNode vectorNode = new VectorNode();
        double[] vector = new double[alphabet.size()];
        for(int i=0; i<alphabet.size(); i++){
            vector[i] = crpNode.typeCounts[i];
        }
        vectorNode.vector = new DenseFloatVector(vector);
        
        //recurse
        for(NCRPNode crpChildNode : crpNode.children){
            VectorNode vectorChildNode = crpNode2VectorNode(crpChildNode);
            vectorNode.childNodes.add(vectorChildNode);
        }
        
        return vectorNode;
    }
    
/*    
    public static void main1(String[] args){
        File corpusFile = new File(args[0]);
        String targetWord = "wear";
        int amountOfSentences = Integer.parseInt(args[1]);
        int depthOfTopicTree = Integer.parseInt(args[2]);
        int amountOfIterations = Integer.parseInt(args[3]);

        HldaMallet a = new HldaMallet(depthOfTopicTree);
        a.estimate(corpusFile, targetWord, amountOfSentences, depthOfTopicTree, amountOfIterations);
		
    }
    
    
    public static void main(String[] args){
        String targetWord = "wear";
        int amountOfSentences = -1;
        int depthOfTopicTree = 5;
        int amountOfIterations = 500;

        HldaMallet a = new HldaMallet(depthOfTopicTree);
        a.estimate(targetWord, amountOfSentences, depthOfTopicTree, amountOfIterations);
		
        System.out.println("\nsense tree:\n" + a.senseTree.toString(5));
    }
*/
	
	
	/*@Override
	public ArrayList<String> requiredParameters() {
		ArrayList<String> rp = super.requiredParameters();
		rp.add("depth of topic tree");
		rp.add("amount of iterations");
		return rp;
	}
    */

/*
	public static void main(String[] args){
		Parameters.setParameter("amount of data points", 20);
		Parameters.setParameter("data dimensionality", 100000);
        Parameters.setParameter("depth of topic tree", 5);
		Parameters.setParameter("amount of iterations", 500);
        
        HldaMallet exp = new HldaMallet(null);
		exp.randomlyPopulateDataset(0.01f);
		exp.run();
		System.out.println(exp.senseTree.toString(true));
	}

    @Override
    public Description getDescription() {
        Description d = super.getDescription();
        
        return d;
    }
	
}
*/