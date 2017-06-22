package pattern.dependency;

import integerset.SetOfIntegers;
import integerset.interval.IntegerInterval;
import integerset.interval.LimitedInterval;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;
import lingunit.dependency.DepArc;
import lingunit.dependency.DepNode;
import lingunit.dependency.DepRelation;
import lingunit.dependency.DepTree;
import lingunit.flattext.Word;
import pattern.flattext.TargetWord;
import vocabulary.Vocabulary;

public class DepTreePattern extends DepPattern{
	
	private DepNode rootNode;
	private ArrayList<DepNode> nodes;
	private ArrayList<DepArc> depArcs;
	private int amountOfTargetNodes;
    public DepNode targetNode;
    LimitedInterval smallestIntervalContainingThisPatternAtTargetNode;
	
	public DepTreePattern(){
		
	}
	
	public DepTreePattern(DepNode rootNode){
		this.rootNode = rootNode;
	}
	
	
	public ArrayList<DepNode> getNodes(){
		if(nodes == null){
            int i=0;
			nodes = new ArrayList<>();
			Stack<DepNode> nodeStack = new Stack<>();
			nodeStack.push(rootNode);
			while(!nodeStack.isEmpty()){
				DepNode node = nodeStack.pop();
                node.setNodeIndex(i);
                i++;
				nodes.add(node);
				for(DepNode childNode : node.getChildrenNodes()){
					nodeStack.push(childNode);
				}
			}
		}
		
		return nodes;
	}
    
    public boolean containsTargetNode(){
        return amountOfTargetNodes == 1;
    }
	
	public boolean isDepNode(){
		return !containsTargetNode() && rootNode != null && !rootNode.hasAnyChildrenNodes();
	}
	
	public DepNode toDepNode(){
		if(isDepNode()){
			return rootNode;
		}else{
			return null;
		}
	}
	
	public boolean isDepArc(){
		DepNode childNode;
		return !containsTargetNode() &&
            rootNode != null &&
            rootNode.hasWord() &&
            rootNode.getAmountOfChildren() == 1 &&
            (childNode = rootNode.getChildrenNodes().get(0)) != null &&
            childNode.hasWord() &&
            childNode.hasRelationWithHead() &&
            !childNode.hasAnyChildrenNodes();
	}
	
	public DepArc toDepArc(){
		if(isDepArc()){
			DepNode childNode = rootNode.getChildrenNodes().get(0);
			return new DepArc(rootNode.getWord(), childNode.getRelationWithHead(), childNode.getWord());
		}else{
			return null;
		}
	}
    
    private int getDistanceToFurthestUnvisitedRelative(DepNode node, SetOfIntegers visitedNodes){
        int distance = 0;
        //mark given node as visited
        visitedNodes.add(node.getNodeIndex());
        //visit its head node
        DepNode headNode;
        if((headNode = node.getHeadNode()) != null && !visitedNodes.contains(headNode.getNodeIndex())){
            distance = 1 + getDistanceToFurthestUnvisitedRelative(headNode, visitedNodes);
        }
        //visit its children nodes
        for(DepNode childNode : node.getChildrenNodes()){
            if(!visitedNodes.contains(childNode.getNodeIndex())){
                distance = Math.max(distance, 1 + getDistanceToFurthestUnvisitedRelative(childNode, visitedNodes));
            }
        }
        
        return distance;
    }
    
    public LimitedInterval getSmallestIntervalContainingThisPatternAtTargetNode(){
        if(smallestIntervalContainingThisPatternAtTargetNode == null){
            int upperDistance = 0;
            if(targetNode.hasHeadNode()){
                SetOfIntegers upperNodes = new SetOfIntegers(getSize());
                upperNodes.add(targetNode.getNodeIndex());
                upperDistance = 1 + getDistanceToFurthestUnvisitedRelative(targetNode.getHeadNode(), upperNodes);
            }
            
            int lowerDistance = 0;
            if(targetNode.hasAnyChildrenNodes()){
                SetOfIntegers lowerNodes = new SetOfIntegers(getSize());
                if(targetNode.hasHeadNode()) lowerNodes.add(targetNode.getHeadNode().getNodeIndex());
                lowerDistance = getDistanceToFurthestUnvisitedRelative(targetNode, lowerNodes);
            }

            smallestIntervalContainingThisPatternAtTargetNode = new LimitedInterval(-lowerDistance, upperDistance);
        }
        
        return smallestIntervalContainingThisPatternAtTargetNode;
    }

	//format: [word1 rel1 [word2] rel2 [word3 rel3 [word4]]]
	//tree must contain exactly one target node, otherwise it is not a valid tree pattern
	private static DepNode create(DepTreePatternTokeniser tokeniser, DepTreePattern pattern){
		DepNode node = null;
		String nodeName;
		String s;
		if(tokeniser.hasNext()){
			s = tokeniser.next();
            //System.out.println("token: " + s); //DEBUG
			if(s.equals("[") && tokeniser.hasNext()){ //begin a new subtree
				nodeName = tokeniser.next();
				//Word word = Word.create(nodeName, wordForm);
                Word word = Vocabulary.getWord(nodeName);
				node = new DepNode(word);
                //System.out.println("node name: " + nodeName + " (" + word.getClass() + ")"); //DEBUG
				if(word instanceof TargetWord){
                    pattern.amountOfTargetNodes++;
                    pattern.targetNode = node;
                }

				String a;
				while(tokeniser.hasNext()){
					a = tokeniser.next();
                    //System.out.println("token: " + a); //DEBUG
					if(a.equals("]")){ //finish this node
						return node;
					}else if(a.equals("[")){ //unallowed
						return null;
					}else{ //discover arc to a child node: a is relation name
						DepNode childNode = create(tokeniser, pattern);
						if(childNode == null){
							return null;
						}else{
							DepRelation rel = DepRelation.create(a);
							childNode.setRelationWithHead(rel);
							childNode.setHeadNode(node);
							node.addChildNode(childNode);
						}
					}
				}
			}else if(s.equals("]")){ //unallowed
				return null;
			}else{ //discover a node name with no brackets around it
				nodeName = s;
				//Word word = Word.create(nodeName, wordForm);
                Word word = Vocabulary.getWord(nodeName);
                //System.out.println("node name: " + nodeName + " (" + word.getClass() + ")"); //DEBUG
                node = new DepNode(word);
				if(word instanceof TargetWord){
                    pattern.amountOfTargetNodes++;
                    pattern.targetNode = node;
                }
				return node;
			}
		}
		
		return node;
	}
	
	//assumes that given string contains no inverted relations
	public static DepTreePattern create(String s){
		DepTreePatternTokeniser tokeniser = new DepTreePatternTokeniser(s);
		
		DepTreePattern pattern = new DepTreePattern();
		DepNode patternRootNode = create(tokeniser, pattern);
		pattern.rootNode = patternRootNode;
        
        //DEBUG
        //System.out.println("dep pattern string: " + s);
        //System.out.println("root node: " + pattern.rootNode.toString(wordForm));
        //System.out.println("target node: " + pattern.targetNode.toString(wordForm));
        //System.out.println("dep pattern: " + pattern.toString(wordForm));
		
		return pattern;
	}
	
    
 	@Override
	public int getSize() {
		return getNodes().size();
	}
	
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

    
    //pattern node is initially this pattern's root node
    //recursively reduces set of matching tree nodes more and more
    //returns the set of tree nodes that remain from pattern root node's matches after the full pattern tree has been checked for matching
    private SetOfIntegers updateTreeNodesMatchingSubtree(DepNode patternNode, SetOfIntegers[] matchingTreeNodesPerPatternNode, DepTree depTree){
        SetOfIntegers fromGivenNode = matchingTreeNodesPerPatternNode[patternNode.getNodeIndex()];
        for(DepNode patternChildNode : patternNode.getChildrenNodes()){
            SetOfIntegers fromChildNode = updateTreeNodesMatchingSubtree(patternChildNode, matchingTreeNodesPerPatternNode, depTree);
            fromGivenNode.intersectWith(fromChildNode);
        }
        
        if(patternNode.hasHeadNode()){
            SetOfIntegers forHeadNode = new SetOfIntegers(fromGivenNode.getCapacity());
            for(int i : fromGivenNode.toIntArray()){
                forHeadNode.add(depTree.getNode(i).getHeadNode().getNodeIndex());
            }
            return forHeadNode;
        }else{
            return fromGivenNode;
        }
    }
    private void updateTreeNodesMatchingThisPattern(SetOfIntegers[] matchingTreeNodesPerPatternNode, DepTree depTree){
        updateTreeNodesMatchingSubtree(rootNode, matchingTreeNodesPerPatternNode, depTree);
    }

    
    //pattern node is initially this pattern's target node
    //moves up the pattern tree until it reaches the root node
    //on the way back down, keep reducing set of matching tree nodes until back at the target node
    //returns the set of matching tree (target) nodes that match this pattern's target node and its defined relatives
    private SetOfIntegers getMatchingTreeTargetNodes(DepNode patternNode, SetOfIntegers[] matchingTreeNodesPerPatternNode, DepTree depTree){
        SetOfIntegers fromGivenNode = matchingTreeNodesPerPatternNode[patternNode.getNodeIndex()];
        
        if(patternNode.hasHeadNode()){
            SetOfIntegers fromHeadNode = getMatchingTreeTargetNodes(patternNode.getHeadNode(), matchingTreeNodesPerPatternNode, depTree);
            for(int i : fromGivenNode.toIntArray()){
                int headNodeIndex = depTree.getNode(i).getHeadNode().getNodeIndex();
                if(!fromHeadNode.contains(headNodeIndex)){
                    fromGivenNode.remove(i);
                }
            }
        }
        
        return fromGivenNode;
    }
    private SetOfIntegers getMatchingTreeTargetNodes(SetOfIntegers[] matchingTreeNodesPerPatternNode, DepTree depTree){
        return getMatchingTreeTargetNodes(targetNode, matchingTreeNodesPerPatternNode, depTree);
    }

	
    @Override
    //returns a set of tree nodes that match this pattern's target node and whose tree relatives match the relatives as defined by this pattern
	protected SetOfIntegers getMatchingNodes(DepTree depTree) {
        ArrayList<DepNode> patternNodes = getNodes();
        SetOfIntegers[] dependentNodesOfMatchingArcs = new SetOfIntegers[patternNodes.size()]; //maps each node in pattern tree to a set of nodes in given tree
        for(int i=0; i<patternNodes.size(); i++) dependentNodesOfMatchingArcs[i] = new SetOfIntegers(depTree.getAmountOfNodes());
        for(DepNode givenNode : depTree.getNodes()){
            for(DepNode patternNode : patternNodes){
                //record dependent nodes of arcs that match in pattern tree and given tree
                if(patternNode.matches(givenNode) &&
                    (patternNode.getRelationWithHead() == null || (givenNode.getRelationWithHead() != null && patternNode.getRelationWithHead().matches(givenNode.getRelationWithHead()))) && //root node of this pattern tree has no relation with head or head node...
                    (patternNode.getHeadNode() == null || (givenNode.getHeadNode() != null && patternNode.getHeadNode().matches(givenNode.getHeadNode())))){ //...but it might still match a node in given tree
                    dependentNodesOfMatchingArcs[patternNode.getNodeIndex()].add(givenNode.getNodeIndex());
                    //System.out.println("adding tree node " + givenNode.toString(wordForm) + " to pattern node " + patternNode.toString(wordForm)); //DEBUG
                }
            }
        }

        //updates the indices sets by keeping only those that jointly match this pattern
        updateTreeNodesMatchingThisPattern(dependentNodesOfMatchingArcs, depTree); //RESTORE ME
        //get the tree node indices that match this pattern's target node and its relatives defined in this pattern
        SetOfIntegers treeNodesMatchingPatternAtTargetNode = getMatchingTreeTargetNodes(dependentNodesOfMatchingArcs, depTree);
        
        return treeNodesMatchingPatternAtTargetNode;
    }


	@Override
    //returns the set of matching tree (target) nodes whose tree relatives fit inside the given interval
    //given node indices are not context nodes (as in sister classes), but potential target nodes
	protected SetOfIntegers getTargetWordIndices(DepTree depTree, int[] nodeIndices, IntegerInterval in) {
        LimitedInterval p = getSmallestIntervalContainingThisPatternAtTargetNode();
        //System.out.println("smallest: " + p.toString()); //DEBUG
        if(in.contains(p)){
            return new SetOfIntegers(depTree.getAmountOfNodes(), nodeIndices);
        }else{
            return new SetOfIntegers(depTree.getAmountOfNodes());
        }
	}
	

	/*public static void main1(String[] args){
        int wordForm = Word.TOKEN;
		Vocabulary.create(wordForm);
		
		String[] ss = new String[]{
			"w1",
			"[w1]",
			"[w1 a1 w2]",
			"[w1 a1 [w2]]",
			"[w1 a2 [w2] a3 [w3]]",
			"*TARGET*",
			"[*TARGET*]",
			"[*TARGET* a1 w2]",
			"[*TARGET* a1 *TARGET*]",
			"[*TARGET* a1 [w2]]",
			"[*TARGET* a2 [w2] a3 [w3]]"
		};
		
		for(String s : ss){
			DepTreePattern t = DepTreePattern.create(s);
			String ts;
			if(t == null){
				ts = s + " -> null\n";
			}else{
				ts = s + " -> " + t.toString() + ", amount of nodes: " + t.getSize() + ", to node: " + t.toDepNode() + ", to arc: " + t.toDepArc() + ", #targets: " + t.amountOfTargetNodes + "\n";
				ts += "arcs:\n";
				for(DepArc arc : t.toDepArcs()){
					ts += arc.toString() + "\n";
				}
			}
			System.out.println(ts);
		}
		
	}

    
    public static void main(String[] args){
 		String corpusString = "<text id=\"ukwac:http://observer.guardian.co.uk/osm/story/0,,1009777,00.html\">\n" +
		"<s>\n" +
		"1	Hooligans	hooligan	NNS	NNS	_	0	null	_	_\n" +
		"2	,	,	,	,	_	4	punct	_	_\n" +
		"3	unbridled	unbridled	JJ	JJ	_	4	amod	_	_\n" +
		"4	passion	passion	NN	NN	_	1	null	_	_\n" +
		"5	-	-	:	:	_	4	punct	_	_\n" +
		"6	and	and	CC	CC	_	4	cc	_	_\n" +
		"7	no	no	DT	DT	_	9	det	_	_\n" +
		"8	executive	executive	JJ	JJ	_	9	amod	_	_\n" +
		"9	boxes	box	NNS	NNS	_	4	conj	_	_\n" +
		"10	.	.	SENT	SENT	_	4	dep	_	_\n" +
		"\n" +
		"</s>\n" +
		"<s>\n" +
		"1	Portsmouth	portsmouth	NNP	NNP	_	4	nsubj	_	_\n" +
		"2	are	are	VBP	VBP	_	4	cop	_	_\n" +
		"3	a	a	DT	DT	_	4	det	_	_\n" +
		"4	reminder	reminder	NN	NN	_	0	null	_	_\n" +
		"5	of	of	IN	IN	_	4	prep	_	_\n" +
		"6	how	how	WRB	WRB	_	8	advmod	_	_\n" +
		"7	football	football	NN	NN	_	8	nsubj	_	_\n" +
		"8	used	use	VBD	VBD	_	5	pcomp	_	_\n" +
		"9	to	to	TO	TO	_	10	aux	_	_\n" +
		"10	be	be	VB	VB	_	8	xcomp	_	_\n" +
		"11	before	before	IN	IN	_	10	prep	_	_\n" +
		"12	the	the	DT	DT	_	14	det	_	_\n" +
		"13	corporate	corporate	JJ	JJ	_	14	amod	_	_\n" +
		"14	takeover	takeover	NN	NN	_	11	pobj	_	_\n" +
		"15	-	-	:	:	_	4	punct	_	_\n" +
		"16	and	and	CC	CC	_	4	cc	_	_\n" +
		"17	that	that	DT	DT	_	21	nsubj	_	_\n" +
		"18	's	be	VBZ	VBZ	_	21	cop	_	_\n" +
		"19	no	no	DT	DT	_	21	det	_	_\n" +
		"20	bad	bad	JJ	JJ	_	21	amod	_	_\n" +
		"21	thing	thing	NN	NN	_	4	conj	_	_\n" +
		"22	,	,	,	,	_	4	punct	_	_\n" +
		"23	say	say	VBP	VBP	_	4	prep	_	_\n" +
		"24	Ed	ed	NNP	NNP	_	25	nn	_	_\n" +
		"25	Vulliamy	vulliamy	NNP	NNP	_	23	nsubj	_	_\n" +
		"26	and	and	CC	CC	_	25	cc	_	_\n" +
		"27	Brian	brian	NNP	NNP	_	28	nn	_	_\n" +
		"28	Oliver	oliver	NNP	NNP	_	25	conj	_	_\n" +
		"29	in	in	IN	IN	_	25	prep	_	_\n" +
		"30	this	this	DT	DT	_	32	det	_	_\n" +
		"31	special	special	JJ	JJ	_	32	amod	_	_\n" +
		"32	report	report	NN	NN	_	29	pobj	_	_\n" +
		"33	Sunday	sunday	NNP	NNP	_	34	nn	_	_\n" +
		"34	August	august	NNP	NNP	_	25	dep	_	_\n" +
		"35	3	3	CD	CD	_	34	num	_	_\n" +
		"36	,	,	,	,	_	25	punct	_	_\n" +
		"37	2003	@card@	CD	CD	_	39	num	_	_\n" +
		"38	The	the	DT	DT	_	39	det	_	_\n" +
		"39	Observer	observer	NNP	NNP	_	25	conj	_	_\n" +
		"40	When	when	WRB	WRB	_	43	advmod	_	_\n" +
		"41	Roy	roy	NNP	NNP	_	42	nn	_	_\n" +
		"42	Keane	keane	NNP	NNP	_	43	nsubj	_	_\n" +
		"43	made	make	VBD	VBD	_	39	dep	_	_\n" +
		"44	his	his	PRP$	PRP$	_	45	poss	_	_\n" +
		"45	remarks	remark	NNS	NNS	_	43	dobj	_	_\n" +
		"46	about	about	IN	IN	_	45	prep	_	_\n" +
		"47	prawn	prawn	NN	NN	_	48	nn	_	_\n" +
		"48	sandwiches	sandwich	NNS	NNS	_	46	pobj	_	_\n" +
		"49	,	,	,	,	_	25	punct	_	_\n" +
		"50	and	and	CC	CC	_	25	cc	_	_\n" +
		"51	those	those	DT	DT	_	25	conj	_	_\n" +
		"52	who	who	WP	WP	_	53	nsubj	_	_\n" +
		"53	eat	eat	VBP	VBP	_	51	rcmod	_	_\n" +
		"54	them	them	PRP	PRP	_	53	dobj	_	_\n" +
		"55	at	at	IN	IN	_	53	prep	_	_\n" +
		"56	football	football	NN	NN	_	57	nn	_	_\n" +
		"57	matches	match	NNS	NNS	_	55	pobj	_	_\n" +
		"58	,	,	,	,	_	25	punct	_	_\n" +
		"59	three	three	CD	CD	_	60	num	_	_\n" +
		"60	years	year	NNS	NNS	_	61	dep	_	_\n" +
		"61	ago	ago	RB	RB	_	25	advmod	_	_\n" +
		"62	,	,	,	,	_	25	punct	_	_\n" +
		"63	he	he	PRP	PRP	_	65	nsubjpass	_	_\n" +
		"64	was	be	VBD	VBD	_	65	auxpass	_	_\n" +
		"65	lifted	lift	VBN	VBN	_	25	rcmod	_	_\n" +
		"66	in	in	IN	IN	_	65	prep	_	_\n" +
		"67	the	the	DT	DT	_	68	det	_	_\n" +
		"68	estimation	estimation	NN	NN	_	66	pobj	_	_\n" +
		"69	of	of	IN	IN	_	68	prep	_	_\n" +
		"70	hundreds	hundred	NNS	NNS	_	69	pobj	_	_\n" +
		"71	of	of	IN	IN	_	70	prep	_	_\n" +
		"72	thousands	thousand	NNS	NNS	_	71	pobj	_	_\n" +
		"73	of	of	IN	IN	_	72	prep	_	_\n" +
		"74	supporters	supporter	NNS	NNS	_	73	pobj	_	_\n" +
		"75	.	.	SENT	SENT	_	25	punct	_	_\n" +
		"\n" +
		"</s>\n" +
		"<s>\n" +
		"1	Not	not	RB	RB	_	3	neg	_	_\n" +
		"2	many	many	JJ	JJ	_	3	amod	_	_\n" +
		"3	footballers	footballer	NNS	NNS	_	4	nsubj	_	_\n" +
		"4	speak	speak	VBP	VBP	_	0	null	_	_\n" +
		"5	out	out	RP	RP	_	4	prt	_	_\n" +
		"6	against	against	IN	IN	_	4	prep	_	_\n" +
		"7	the	the	DT	DT	_	9	det	_	_\n" +
		"8	corporate	corporate	JJ	JJ	_	9	amod	_	_\n" +
		"9	makeover	makeover	NN	NN	_	6	pobj	_	_\n" +
		"10	,	,	,	,	_	9	punct	_	_\n" +
		"11	or	or	CC	CC	_	9	cc	_	_\n" +
		"12	takeover	takeover	NN	NN	_	9	conj	_	_\n" +
		"13	,	,	,	,	_	4	punct	_	_\n" +
		"14	of	of	IN	IN	_	4	prep	_	_\n" +
		"15	English	english	JJ	JJ	_	16	amod	_	_\n" +
		"16	football	football	NN	NN	_	14	pobj	_	_\n" +
		"17	.	.	SENT	SENT	_	4	punct	_	_\n" +
		"\n" +
		"</s>\n" +
		"<s>\n" +
		"1	One	one	PRP	PRP	_	2	nsubj	_	_\n" +
		"2	assumes	assume	VBZ	VBZ	_	0	null	_	_\n" +
		"3	that	that	IN	IN	_	7	complm	_	_\n" +
		"4	Keane	keane	NNP	NNP	_	7	nsubjpass	_	_\n" +
		"5	would	would	MD	MD	_	7	aux	_	_\n" +
		"6	have	have	VB	VB	_	7	aux	_	_\n" +
		"7	approved	approve	VBN	VBN	_	2	ccomp	_	_\n" +
		"8	of	of	IN	IN	_	7	prep	_	_\n" +
		"9	the	the	DT	DT	_	10	det	_	_\n" +
		"10	behaviour	behaviour	NN	NN	_	8	pobj	_	_\n" +
		"11	in	in	IN	IN	_	10	prep	_	_\n" +
		"12	the	the	DT	DT	_	16	det	_	_\n" +
		"13	Old	old	NNP	NNP	_	16	nn	_	_\n" +
		"14	Trafford	trafford	NNP	NNP	_	16	nn	_	_\n" +
		"15	executive	executive	NN	NN	_	16	nn	_	_\n" +
		"16	boxes	box	NNS	NNS	_	11	pobj	_	_\n" +
		"17	during	during	IN	IN	_	7	prep	_	_\n" +
		"18	Manchester	manchester	NNP	NNP	_	19	nn	_	_\n" +
		"19	United	united	NNP	NNP	_	24	poss	_	_\n" +
		"20	's	's	POS	POS	_	19	possessive	_	_\n" +
		"21	third-round	third-round	JJ	JJ	_	24	amod	_	_\n" +
		"22	FA	fa	NNP	NNP	_	24	nn	_	_\n" +
		"23	Cup	cup	NNP	NNP	_	24	nn	_	_\n" +
		"24	tie	tie	NN	NN	_	17	pobj	_	_\n" +
		"25	against	against	IN	IN	_	24	prep	_	_\n" +
		"26	Portsmouth	portsmouth	NNP	NNP	_	25	pobj	_	_\n" +
		"27	last	last	JJ	JJ	_	28	amod	_	_\n" +
		"28	January	january	NNP	NNP	_	7	tmod	_	_\n" +
		"29	.	.	SENT	SENT	_	7	punct	_	_\n" +
		"\n" +
		"</s>\n" +
		"<s>\n" +
		"1	Not	not	RB	RB	_	4	neg	_	_\n" +
		"2	many	many	JJ	JJ	_	4	amod	_	_\n" +
		"3	prawn	prawn	NN	NN	_	4	nn	_	_\n" +
		"4	sandwiches	sandwich	NNS	NNS	_	6	nsubjpass	_	_\n" +
		"5	were	be	VBD	VBD	_	6	auxpass	_	_\n" +
		"6	eaten	eat	VBN	VBN	_	0	null	_	_\n" +
		"7	.	.	SENT	SENT	_	6	punct	_	_\n" +
		"\n" +
		"</s>\n" +
		"</text>";
		
        int wordForm = Word.LEMMA_AND_POS;
		Vocabulary vocabulary = Vocabulary.create(wordForm);

        //arcs are ordered "[head relation dependent]"
        String string =
            //"[the#?]";
            //"[the#? DET *target*#NNS]";
            //"[the#? DET-1 *target*#NNS]";
            //"[*target*#? DET the#?]";
            //"[*target*#? DET the#? AMOD corporate#?]";
            //"[*target*#? DET the#? AMOD ?#?]";
            "[the#? DET-1 box#?]";
            
        DepPattern pattern = DepPattern.create(string);
		
		BagOfIntegerIntervals contextWindows = new BagOfIntegerIntervals();
        contextWindows.add(new DoubleUnlimitedInterval());
        contextWindows.add(new LimitedInterval(-1, 1));
        contextWindows.add(new LimitedInterval(0, 1));
        contextWindows.add(new LimitedInterval(-1, 0));

		Document document = null;
		try{
			BufferedReader textReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(corpusString.getBytes())));
			AbstractCorpusReader corpusReader = new ConllCorpusReader(textReader);
			document = corpusReader.readDocument();
			corpusReader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
        if(document == null) return;
		
		for(ParsedSentence ps : document.getParsedSentences()){
			DepTree depTree = ps.getDepTree();
			String s = "dep pattern: " + pattern.toString() + " (" + pattern.getClass() + ")\n";
            s += ps.getSentence().toString() + "\n";
            s += depTree.toString(wordForm);
            //
			SetOfIntegers[] matches = pattern.getTargetWordIndices(ps, contextWindows);
			for(int i=0; i<contextWindows.size(); i++){
				IntegerInterval window = contextWindows.get(i);
				s += "for window: " + window.toString() + "\n";
				s += "matches: " + ps.getWords(matches[i]).toString() + "\n";
				for(DepNode node : depTree.getNodes()){
                    //DEBUG
					//SetOfIntegers neighbourhood = depTree.getNeighbourhood(node, window);
					//s += "neighbourhood of " + node.toString(wordForm) + ": ";
					//for(int nodeIndex : neighbourhood.toIntArray()) s += depTree.getNode(nodeIndex).toString(wordForm) + ", ";
					//s += "\n";
				}
			}
			System.out.println(s);
        }
    }
	*/

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        return rootNode.exportTo(writer);
    }

    @Override
    public String asString(Object o) {
        return rootNode.asString(o);
    }
}
