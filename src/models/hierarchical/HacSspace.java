/*package models.hierarchical;

import vector.complex.VectorTree;
import vector.complex.VectorNode;
import edu.ucla.sspace.clustering.HierarchicalAgglomerativeClustering;
import edu.ucla.sspace.clustering.Merge;
import edu.ucla.sspace.common.Similarity;
import edu.ucla.sspace.matrix.Matrix;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lingunit.flattext.Word;
import models.Parameters;
import models.SspaceModel;
import vector.DenseFloatVector;
import vocabulary.Description;

//dataset matrix: each row is a datapoint
public class HacSspace extends SspaceModel{
    
    public VectorTree senseTree;
    
    public HacSspace(ArrayList<Word> targetWords){
        super(targetWords);
        
        senseTree = null;
    }

	@Override
    public void run(){
        HierarchicalAgglomerativeClustering hac = new HierarchicalAgglomerativeClustering();
        List<Merge> merges = hac.buildDendogram(dataset, HierarchicalAgglomerativeClustering.ClusterLinkage.MEAN_LINKAGE, Similarity.SimType.EUCLIDEAN);
        senseTree = merges2VectorTree(merges);
    }
    
    private VectorTree merges2VectorTree(List<Merge> merges){
        
        VectorNode[] nodes = new VectorNode[dataset.rows()];
        for(int i=0; i<dataset.rows(); i++){
            VectorNode vn = new VectorNode();
            vn.vector = new DenseFloatVector(dataset.getRow(i));
            nodes[i] = vn;
        }
        
        VectorNode rootNode = null;
        for(Merge merge : merges){
            System.out.println(merge); //DEBUG
            
            int index1 = merge.remainingCluster();
            int index2 = merge.mergedCluster();
            VectorNode node1 = nodes[index1];
            VectorNode node2 = nodes[index2];
            int parentIndex = (index1 < index2 ? index1 : index2);
            VectorNode parentNode = new VectorNode();
            //TODO assign vector to parent node, e.g. mean or sum of child nodes
            parentNode.childNodes.add(node1);
            parentNode.childNodes.add(node2);
            node1.parentNode = parentNode;
            node2.parentNode = parentNode;
            nodes[parentIndex] = parentNode;
            rootNode = parentNode;
        }
        VectorTree tree = new VectorTree(rootNode);
        
        return tree;
    }
    
    private static void printMatrix(String matrixName, Matrix m){
        System.out.println("Matrix " + matrixName + ":");
        for(int i=0; i<m.rows(); i++){
            double[] row = m.getRow(i);
            String s = "";
            for(int j=0; j<row.length; j++){
                double value = row[j];
                s += "\t" + ((float) value);
            }
            System.out.println(s);
        }
    }
	
	/*private static SparseMatrix getRandomSparseMatrix(int maxAmountOfRows, int maxAmountOfColumns, int cardinality, double minValue, double maxValue){
		SparseMatrix m = new AtomicGrowingSparseMatrix();
		for(int i=0; i<cardinality; i++){
			int row = (int) (Math.random() * maxAmountOfRows);
			int column = (int) (Math.random() * maxAmountOfColumns);
			double value = Math.random() * (maxValue - minValue) + minValue;
			m.set(row, column, value);
			if(i%50 == 0) System.out.println("random values filled into " + maxAmountOfRows + " x " + maxAmountOfColumns + " sparse matrix: " + i);
		}
		
		return m;
	}
	
	private static SparseMatrix getRandomDenseMatrix(int amountOfRows, int amountOfColumns){
		SparseMatrix m = new AtomicGrowingSparseMatrix();
        for(int i=0; i<amountOfRows; i++){
            for(int j=0; j<amountOfColumns; j++){
                double value = Math.random() - 0.5;
                m.set(i, j, value);
            }
        }
		
		return m;
	}

    public static void main(String[] args){
        //create random matrix
        //SparseMatrix m = getRandomSparseMatrix(20, 1000000, 100000, -0.5, 0.5);
        //printMatrix("m", m);
        
        //cluster
        HacSspace exp  = new HacSspace();
        exp.cluster();
        System.out.println(exp.senseTree.toString(5));
    }
	*/

	/*@Override
	public ArrayList<String> requiredParameters(){
		return super.requiredParameters();
	}
    */
/*	
	public static void main(String[] args){
		Parameters.setParameter("amount of data points", 20);
		Parameters.setParameter("data dimensionality", 100);
        
        HacSspace exp = new HacSspace(null);
		exp.randomlyPopulateDataset(0.1f);
		exp.run();
		System.out.println(exp.senseTree.toString(true));
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter("/home/VD/wblacoe/Desktop/senseTree.txt"));
            exp.senseTree.exportTo(out);
            out.close();
        }catch(IOException e){
            e.printStackTrace();
        }
	}

    @Override
    public Description getDescription() {
        Description d = super.getDescription();
        
        return d;
    }
	
}
*/