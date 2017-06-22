package lingunit.dependency;

import integerset.SetOfIntegers;
import integerset.interval.EmptyInterval;
import integerset.interval.IntegerInterval;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ListIterator;
import java.util.Stack;
import lingunit.flattext.Word;
import pattern.dependency.DepPattern;

public class DepArc extends DepPattern{

	private Word headWord, dependentWord;
	private DepRelation relation;
	
	public DepArc(Word headWord, DepRelation relation, Word dependentWord){
        this.headWord = headWord;
		this.relation = relation;
		this.dependentWord = dependentWord;
	}

	public Word getDependentWord(){
		return dependentWord;
	}
	
	public DepRelation getRelation(){
		return relation;
	}
	
	public Word getHeadWord(){
		return headWord;
	}
	
    public DepArc invert(){
        return new DepArc(dependentWord, relation.invert(), headWord);
    }
    
    //@Override
    public boolean matches(DepArc depArc){
        //DepArc depArc = (DepArc) depElement;
        return dependentWord.matches(depArc.dependentWord) && relation.matches(depArc.relation) && headWord.matches(depArc.headWord);
    }
    
	@Override
	public int getSize() {
		return 2;
	}

	
	private SetOfIntegers getNodesThatAreDependentsOfMatchingArcsIn(DepTree depTree){
		//collect all nodes that are the dependent node of an arc in given tree that matches this bag of arcs
		SetOfIntegers arcDependents = new SetOfIntegers(depTree.getAmountOfNodes());
		//go through all arcs in the tree
		Stack<DepNode> nodeStack = new Stack<>();
        nodeStack.push(depTree.getRoodNode());
		while(!nodeStack.isEmpty()){
            DepNode node = nodeStack.pop();

            if(node != null && node.hasAnyChildrenNodes()){
                for(ListIterator<DepNode> iterator = node.getChildrenNodes().listIterator(node.getAmountOfChildren()); iterator.hasPrevious();){
                    DepNode childNode = iterator.previous();
                    nodeStack.push(childNode);
                    
                    //check if current arc matches this bag of dep arcs
                    DepArc arc = new DepArc(node.getWord(), childNode.getRelationWithHead(), childNode.getWord());
					//System.out.println("does arc " + arc.toString(wordForm) + " match bag? " + matches(arc, wordForm)); //DEBUG
                    if(matches(arc)){
						arcDependents.add(childNode.getNodeIndex()); //keep only the dependent of this arc
                        //System.out.println("# " + childNode.toString(wordForm)); //DEBUG
					}
                }
            }
        }
		
		return arcDependents;
	}
	
	@Override
    //returns the set of nodes that are the dependent node of an arc that matches this pattern
	protected SetOfIntegers getMatchingNodes(DepTree depTree){
		return getNodesThatAreDependentsOfMatchingArcsIn(depTree);
	}
	
	@Override
    //returns the set of nodes whose neighbourhood (defined by given interval) contains this pattern
	protected SetOfIntegers getTargetWordIndices(DepTree depTree, int[] nodeIndices, IntegerInterval in){
		SetOfIntegers unionOfNeighbourhoods = new SetOfIntegers(depTree.getAmountOfNodes());
		if(in instanceof EmptyInterval) return null;
		
        IntegerInterval inverseIn = in.invert();
        inverseIn = inverseIn.shiftLowerBoundaryBy(1); //this incrementation covers the found arc, but this arc's dependent is not necessarily a match in the output interval
		for(int nodeIndex : nodeIndices){
            SetOfIntegers neighbourhood = depTree.getNeighbourhood(nodeIndex, inverseIn);
            unionOfNeighbourhoods.addAll(neighbourhood);
        }

        return unionOfNeighbourhoods;
	}

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String asString(Object o){
        return "[" + headWord.asString(o) + " " + relation.asString(o) + " " + dependentWord.asString(o) + "]";
    }
    
}