package lingunit.dependency;

import integerset.SetOfIntegers;
import integerset.interval.EmptyInterval;
import integerset.interval.IntegerInterval;
import java.io.BufferedWriter;
import java.io.IOException;
import lingunit.flattext.Word;
import java.util.ArrayList;
import pattern.dependency.DepPattern;

public class DepNode extends DepPattern{

	private int nodeIndex;
	private Word word;
	private DepRelation relationWithHead;
    
	private DepNode headNode;
    private ArrayList<DepNode> childrenNodes;

    
    public DepNode(Word word){
		nodeIndex = -1;
		this.word = word;
		this.relationWithHead = null;
        
		headNode = null; //head node stays null if this is a tree's root node
        childrenNodes = new ArrayList<>();
	}
    public DepNode(Word word, DepRelation relationWithHead){
        this(word);
        this.relationWithHead = relationWithHead;
    }
	
	//returns index of this node in a particular tree
	public int getNodeIndex(){
		return nodeIndex;
	}
	
	public void setNodeIndex(int nodeIndex){
		this.nodeIndex = nodeIndex;
	}
	
	public boolean hasWord(){
		return word != null;
	}
	
	public Word getWord(){
		return word;
	}
	
	public void setWord(Word word){
		this.word = word;
	}
	
	public boolean hasRelationWithHead(){
		return relationWithHead != null;
	}
	
	public DepRelation getRelationWithHead(){
		return relationWithHead;
	}
	
	public void setRelationWithHead(DepRelation relationWithHead){
        this.relationWithHead = relationWithHead;
    }
	
	public boolean hasHeadNode(){
        return headNode != null;
    }
	
	public DepNode getHeadNode(){
		return headNode;
	}
	
	public void setHeadNode(DepNode headNode){
		this.headNode = headNode;
	}
	
	public ArrayList<DepNode> getChildrenNodes(){
		return childrenNodes;
	}
	
    public void addChildNode(DepNode childNode){
        childrenNodes.add(childNode);
    }
    
    public boolean hasAnyChildrenNodes(){
        return !childrenNodes.isEmpty();
    }
	
	public int getAmountOfChildren(){
		return childrenNodes.size();
	}

	//@Override
	public boolean matches(DepNode depNode) {
		//return getWord().matches(((DepNode) depElement).getWord(), wordForm);
        return getWord().matches(depNode.getWord());
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
    //returns the set of nodes that match this pattern
	protected SetOfIntegers getMatchingNodes(DepTree depTree) {
		SetOfIntegers matchingNodes = new SetOfIntegers(depTree.getAmountOfNodes());
		for(DepNode node : depTree.getNodes()){
            //System.out.println("does " + toString(wordForm) + " match " + node.toString(wordForm) + " ? " + matches(node, wordForm)); //DEBUG
			if(matches(node)){
				matchingNodes.add(node.getNodeIndex());
			}
		}
		
		return matchingNodes;
	}

	@Override
    //returns the set of nodes whose neighbourhood (defined by given interval) contains this pattern
	protected SetOfIntegers getTargetWordIndices(DepTree depTree, int[] nodeIndices, IntegerInterval in) {
		SetOfIntegers unionOfNeighbourhoods = new SetOfIntegers(depTree.getAmountOfNodes());
		if(in instanceof EmptyInterval) return null;
		
        IntegerInterval inverseIn = in.invert();
		for(int ni : nodeIndices){
            SetOfIntegers neighbourhood = depTree.getNeighbourhood(ni, inverseIn);
            unionOfNeighbourhoods.addAll(neighbourhood);
        }
		
		return unionOfNeighbourhoods;
	}

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        writer.write("[");
        getWord().exportTo(writer);
        for(DepNode childNode : getChildrenNodes()){
            writer.write(" ");
            childNode.getRelationWithHead().exportTo(writer);
            writer.write(" ");
            childNode.exportTo(writer);
        }
        
        return true;
    }

    @Override
    public String asString(Object o) {
        String s = "[" + getWord().asString(o);
        for(DepNode childNode : getChildrenNodes()){
            s += " " + childNode.getRelationWithHead().asString(o) + " " + childNode.asString(o);
        }
        
        return s + "]";
    }
	
}
