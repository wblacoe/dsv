package pattern.dependency;

import experiment.common.BagOfContextWindows;
import integerset.SetOfIntegers;
import integerset.interval.IntegerInterval;
import java.util.TreeMap;
import lingunit.dependency.DepTree;
import lingunit.language.ParsedSentence;
import experiment.common.ContextWindow;
import pattern.language.LanguagePattern;

public abstract class DepPattern extends LanguagePattern{

	public static DepPattern create(String string){
		DepTreePattern t = DepTreePattern.create(string);
        if(t.containsTargetNode()){
			return t;
        }else if(t.isDepNode()){
			return t.toDepNode();
		}else if(t.isDepArc()){
			return t.toDepArc();
		}else{
            return null;
        }
	}
    
    //public abstract boolean matches(DepElement depElement, int wordForm); //RESTORE ME?
	
	protected abstract SetOfIntegers getMatchingNodes(DepTree depTree);
	protected abstract SetOfIntegers getTargetWordIndices(DepTree depTree, int[] nodeIndices, IntegerInterval in);
	
	@Override
	//returns a set of indices of nodes whose context window contains this pattern (repeat this per given context window)
	public SetOfIntegers[] getTargetWordIndices(ParsedSentence parsedSentence, BagOfContextWindows contextWindows){
		DepTree depTree = parsedSentence.getDepTree();
		if(depTree == null) return null;
		
		SetOfIntegers matchingNodesSet = getMatchingNodes(depTree);
		//System.out.println("matching nodes: " + matchingNodesSet.toString()); //DEBUG
		int[] matchingNodes = matchingNodesSet.toIntArray();
		contextElementCount += matchingNodes.length;
		
		SetOfIntegers[] matchesPerContextWindow = new SetOfIntegers[contextWindows.size()];
		TreeMap<IntegerInterval, SetOfIntegers> targetSentenceInternalWindowMatchesMap = new TreeMap<>();
		
		//don't compute target word indices redundantly (only one per distinct target sentence internal window)
		for(ContextWindow contextWindow : contextWindows){
			//if context window even covers target sentence, and its target sentence internal window has not been processed before
			if(contextWindow.documentInternalSentenceWindow.contains(0) && !targetSentenceInternalWindowMatchesMap.containsKey(contextWindow.targetSentenceInternalWindow)){
				//compute target word indices and cache them
				SetOfIntegers u = getTargetWordIndices(depTree, matchingNodes, contextWindow.targetSentenceInternalWindow);
				targetSentenceInternalWindowMatchesMap.put(contextWindow.targetSentenceInternalWindow, u);
			}
		}
		
		//go through all given context windows
		for(int i=0; i<contextWindows.size(); i++){
			//get the above-computed target sentence's target word indices. use multiply if needed by multiple context windows
			SetOfIntegers u = targetSentenceInternalWindowMatchesMap.get(contextWindows.get(i).documentInternalSentenceWindow);
			if(u == null){
				matchesPerContextWindow[i] = new SetOfIntegers(parsedSentence.getSize());
			}else{
				matchesPerContextWindow[i] = u;
			}
		}
		
		return matchesPerContextWindow;
	}

}
