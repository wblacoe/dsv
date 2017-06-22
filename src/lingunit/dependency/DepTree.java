package lingunit.dependency;

import integerset.SetOfIntegers;
import integerset.interval.EmptyInterval;
import integerset.interval.IntegerInterval;
import integerset.interval.LimitedInterval;
import lingunit.flattext.Sentence;
import lingunit.flattext.Word;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;

//is made up of nodes and arcs (no wildcards allowed)
public class DepTree {

	private final static int NEG_INF = Integer.MIN_VALUE + 1;
    private final static int POS_INF = Integer.MAX_VALUE;

	private ArrayList<DepNode> nodes; //contains all nodes, possibly including non-root nodes with no head node
	private DepNode rootNode;
    private ArrayList<DepArc> depArcs; //use as cache rather than recomputing bag of dep arcs repeatedly

	public DepTree(){
		nodes = new ArrayList<>();
		rootNode = null;
        depArcs = null;
	}
	
    public int add(DepNode node){
        int nodeIndex = nodes.size();
		node.setNodeIndex(nodeIndex);
        nodes.add(node);
        return nodeIndex;
    }
	
	public int add(Word word){
        DepNode node = new DepNode(word);
		return add(node);
	}
    
    public DepNode getNode(int index){
        if(index < 0 || index >= getAmountOfNodes()){
            return null;
        }else{
            return nodes.get(index);
        }
    }
	
	public ArrayList<DepNode> getNodes(){
		return nodes;
	}
	
	public int getAmountOfNodes(){
		return nodes.size();
	}
	
	public boolean isEmpty(){
		return nodes.isEmpty();
	}
    
    public boolean isRootNode(DepNode node){
        return rootNode != null && node == rootNode;
    }
	
	public DepNode getRoodNode(){
		return rootNode;
	}
	
	public void setRootNode(DepNode node){
		rootNode = node;
	}

    //traverses the tree (starting at the root) and collects all dep arcs
	public ArrayList<DepArc> toDepArcs(){
		if(depArcs != null) return depArcs;
		
		ArrayList<DepArc> listOfDepArcs = new ArrayList<>();
		
        Stack<DepNode> nodeStack = new Stack<>();
        nodeStack.push(rootNode);
        
        while(!nodeStack.isEmpty()){
            DepNode node = nodeStack.pop();

            if(node != null && node.hasAnyChildrenNodes()){
                for(ListIterator<DepNode> iterator = node.getChildrenNodes().listIterator(node.getAmountOfChildren()); iterator.hasPrevious();){
                    DepNode childNode = iterator.previous();
                    nodeStack.push(childNode);
                    
                    //add arcs from current node to its children nodes
                    DepArc arc = new DepArc(node.getWord(), childNode.getRelationWithHead(), childNode.getWord());
                    listOfDepArcs.add(arc);
                }
            }
        }
		
		return listOfDepArcs;
	}
    
	public Sentence toSentence(){
		Sentence sentence = new Sentence();
		for(DepNode node : nodes){
			sentence.add(node.getWord());
		}
		
		return sentence;
	}
    
	
	private SetOfIntegers getLowerNeighbours(DepNode node, SetOfIntegers set, int d){
        set.add(node.getNodeIndex());
		if(d == 0){
			return set;
		}else{
			for(DepNode childNode : node.getChildrenNodes()){
				set = getLowerNeighbours(childNode, set, d - 1);
			}
			return set;
		}
	}
	
	public SetOfIntegers getLowerNeighbours(DepNode node, int d){
		if(d < 0) d = -d;
        return getLowerNeighbours(node, new SetOfIntegers(getAmountOfNodes()), d);
	}

	//spread from the inside out
    private SetOfIntegers getUpperNeighbours(DepNode node, SetOfIntegers set, int i, int d){
        //System.out.println("getting upper neighbours. node: " + node.toString(Word.TOKEN_AND_POS) + ", i: " + i + ", d: " + d); //DEBUG
        
		//recurse no further than to this node
		if(i == d){
			set.add(node.getNodeIndex());
			return set;
			
		//recurse further over unseen head and children nodes
		}else{
			set.add(node.getNodeIndex());
			//recurse over unseen head node
			if(node.hasHeadNode() && !set.contains(node.getHeadNode().getNodeIndex())){
				set = getUpperNeighbours(node.getHeadNode(), set, i + 1, d);
			}
			//recurse over unseen children nodes
			for(DepNode childNode : node.getChildrenNodes()){
				if(childNode != null && !set.contains(childNode.getNodeIndex())){
					set = getUpperNeighbours(childNode, set, i + 1, d);
				}
			}
			return set;
		}
	}
	
	public SetOfIntegers getUpperNeighbours(DepNode node, int d){
		if(d < 0) d = -d;
        SetOfIntegers set = new SetOfIntegers(getAmountOfNodes());
        set.add(node.getNodeIndex());
        if(d > 0 && node.hasHeadNode()){
            set = getUpperNeighbours(node.getHeadNode(), set, 1, d);
        }
        return set;
	}
	
    //negative indices are for descendants of this node
    //positive indices are for ancestors of this node
    //if given interval contains 0, then output contains this node
	public SetOfIntegers getNeighbourhood(DepNode node, IntegerInterval in){
        SetOfIntegers set = new SetOfIntegers(getAmountOfNodes());
        if(in instanceof EmptyInterval) return set;
        
        int lowerBoundary = (in.lowerBoundary == null ? NEG_INF : in.lowerBoundary);
        int upperBoundary = (in.upperBoundary == null ? POS_INF : in.upperBoundary);
        
        if(lowerBoundary < 0 && upperBoundary < 0){
            set.addAll(getLowerNeighbours(node, lowerBoundary));
            set.removeAll(getLowerNeighbours(node, upperBoundary + 1)); //don't remove everything if in.lowerBoundary == in.upperBoundary
        }else if(lowerBoundary <= 0 && upperBoundary >= 0){
            set.addAll(getLowerNeighbours(node, lowerBoundary));
            set.addAll(getUpperNeighbours(node, upperBoundary));
        }else{
            set.addAll(getUpperNeighbours(node, upperBoundary));
            set.removeAll(getUpperNeighbours(node, lowerBoundary - 1));
        }
        
        return set;
    }
	
	public SetOfIntegers getNeighbourhood(int nodeIndex, IntegerInterval in){
		return getNeighbourhood(getNode(nodeIndex), in);
	}
    
    public SetOfIntegers getNeighbourhood(DepNode node, int radius){
        return getNeighbourhood(node, new LimitedInterval(-radius, radius));
    }
	
	public SetOfIntegers getNeighbourhood(int nodeIndex, int radius){
        return getNeighbourhood(getNode(nodeIndex), new LimitedInterval(-radius, radius));
    }
	
    public String toString(int wordForm){
        Stack<DepNode> nodeStack = new Stack<>();
        Stack<Integer> indentationStack = new Stack<>();
        
        nodeStack.push(rootNode);
        indentationStack.push(0);
        String s = "";
        
        while(!nodeStack.isEmpty()){
            DepNode node = nodeStack.pop();
            Integer indentation = indentationStack.pop();

            if(node != null){
                String indentationString = "";
                for(int i=0; i<indentation; i++) indentationString += " ";
                s += indentationString + node.toString() + ":" + node.getRelationWithHead() + "\n";

                //iterate through children nodes from end to beginning so that output string is from beginning to end
                for(ListIterator<DepNode> iterator = node.getChildrenNodes().listIterator(node.getAmountOfChildren()); iterator.hasPrevious();){
                    DepNode childNode = iterator.previous();
                    nodeStack.push(childNode);
                    indentationStack.push(indentation + 2);
                }
            }
        }
        
        return s;
    }
    
}