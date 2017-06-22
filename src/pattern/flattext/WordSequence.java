package pattern.flattext;

import experiment.common.BagOfContextWindows;
import integerset.SetOfIntegers;
import integerset.interval.EmptyInterval;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import lingunit.language.ParsedSentence;
import lingunit.flattext.Sentence;
import lingunit.flattext.Word;
import integerset.interval.IntegerInterval;
import integerset.interval.LimitedInterval;
import integerset.interval.UnionOfIntervals;
import java.util.TreeMap;
import experiment.common.ContextWindow;
import java.io.BufferedWriter;
import java.io.IOException;
import pattern.language.LanguagePattern;

public class WordSequence extends LanguagePattern {
    
    public LinkedList<WordSequenceElement> elements;
    
    public WordSequence(){
        elements = new LinkedList<>();
    }
    public WordSequence(WordSequenceElement e){
        this();
        elements.add(e);
    }
    public WordSequence(LinkedList<WordSequenceElement> elements){
        this.elements = elements;
    }
    
    
    public void add(WordSequenceElement element){
        elements.add(element);
    }
    
	@Override
    public int getSize(){
        return elements.size();
    }
    
    public boolean isEmpty(){
        return elements.isEmpty();
    }
	
	public boolean isSingleton(){
		return elements.size() == 1;
	}
    
    //make sequence such that its matching function becomes incrementally deterministic
    //**->*, *?->?*
    //because match() will search greedily from sentence beginning, prepend * to sequence and remove redundant final *'s
    //normalised sequences (1) get (if not yet present) an initial *, (2) will have no final *, (3) because they need not match exactly the sentence, but only some interval thereof
    private WordSequence normalise(){
        
        //copy sequence elements, except for all final *'s
        LinkedList<WordSequenceElement> list = new LinkedList<>();
        boolean finalStars = true;
        Iterator<WordSequenceElement> descIter = elements.descendingIterator();
        while(descIter.hasNext()){
            WordSequenceElement e = descIter.next();
            if(!finalStars || !(e instanceof AnyWords)){
                list.push(e);
                finalStars = false;
            }
        }
        //prepend *
        list.push(new AnyWords());
        
        //go through list and normalise bigrams
        ListIterator<WordSequenceElement> iter = list.listIterator();
        WordSequenceElement first = null;
        WordSequenceElement second = iter.next();
        while(second != null){
            //if(first != null) System.out.println("1: " + first.toString(Word.TOKEN_AND_POS) + ", 2: " + second.toString(Word.TOKEN_AND_POS)); //DEBUG
            
            if((first instanceof AnyWords) && (second instanceof AnyWords)){ //if **, ignore second *
                iter.remove();
                second = iter.next();
            }else if((first instanceof AnyWords) && (second instanceof AnyWord)){ //if *?, swap order to ?*
                iter.previous();
                iter.set(first);
                iter.previous();
                iter.set(second);
                iter.next();
                first = iter.hasNext() ? iter.next() : null;
                second = iter.hasNext() ? iter.next() : null;
            }else{ //otherwise change nothing
                first = second;
                second = iter.hasNext() ? iter.next() : null;
            }
        }
        
        //again copy sequence elements, except for all final *'s (in case bigram normalisation pushed any *'s to the end)
        LinkedList<WordSequenceElement> list1 = new LinkedList<>();
        finalStars = true;
        descIter = list.descendingIterator();
        while(descIter.hasNext()){
            WordSequenceElement e = descIter.next();
            if(!finalStars || !(e instanceof AnyWords)){
                list1.push(e);
                finalStars = false;
            }
        }
        
        WordSequence normalisedSequence = new WordSequence(list1);
        return normalisedSequence;
    }

    //string must have format (e e ... e) where e is word sequence element
    public static WordSequence create(String string){
        WordSequence sequence = new WordSequence();

		//single context word (without brackets)
		if(!string.contains(" ")){
            if(string.startsWith("(") && string.endsWith(")")){
                string = string.substring(1, string.length()-1); //remove round brackets if present
            }
            WordSequenceElement contextWord = WordSequenceElement.create(string);
            if(contextWord == null) return null;
			sequence.add(contextWord);
			
		//space-delimited word sequence
		}else if(string.startsWith("(") && string.endsWith(")")){
            String stringWithoutBrackets = string.substring(1, string.length()-1);
            String[] elementStrings = stringWithoutBrackets.split(" +");
            for(String elementString : elementStrings){
                WordSequenceElement element = WordSequenceElement.create(elementString);
                if(element == null) return null;
                sequence.add(element);
            }
			sequence = sequence.normalise();
        }else{
            sequence = null;
        }

        return sequence;
    }
    
	//returns the smallest sentence interval that contains this word sequence
    private IntegerInterval getSequenceMatchStartingAt(int index, Sentence sentence){
		LimitedInterval in = new LimitedInterval();
        boolean match = true;
        
		int sentenceIterIndex = index;
        if(sentence != null && !sentence.isEmpty() && !isEmpty()){
            //Iterator<Word> sentenceIter = sentence.getWords().iterator();
            Iterator<Word> sentenceIter = sentence.getWords().subList(sentenceIterIndex, sentence.getLength()-1).iterator();
            Iterator<WordSequenceElement> sequenceIter = elements.iterator();
            
            //get first elements
            Word sentenceElement;
            WordSequenceElement sequenceElement;
            
            //iterate through sentence and sequence simultaneously as long as sequence is matching sentence
            sentenceIterIndex--;
            while(match){
                
                //if the sequence has been fully processed
                if(!sequenceIter.hasNext()){
                    //the remainder of the sentence is irrelevant
                    sentenceIter = null;
                    match = true;
                    break;
                }
                
                if(!sentenceIter.hasNext()){
                    match = false;
                    break;
                }
                
                sequenceElement = sequenceIter.next();
				sentenceElement = sentenceIter.next();
				sentenceIterIndex++;
                //System.out.println("sequence: " + sequenceElement.toString(wordForm) + ",\tsentence: " + sentenceElement.toString(wordForm)); //DEBUG
				
                //the current sequence element is w or ?
				if(sequenceElement instanceof Word){
					Word w = (Word) sequenceElement;
                    match = w.matches(sentenceElement);
					if(in.lowerBoundary == null && match) in.lowerBoundary = sentenceIterIndex;
					
                //the current sequence element is *
				}else{
                    sequenceElement = sequenceIter.next(); //the w after this *
                    //System.out.println("sequence: " + sequenceElement.toString(wordForm) + ",\tsentence: " + sentenceElement.toString(wordForm)); //DEBUG
                    while(true){
                        if(sequenceElement.matches(sentenceElement)){
                            match = true;
							if(in.lowerBoundary == null) in.lowerBoundary = sentenceIterIndex;
                            break;
                        //if no match, try to match next sentence element with w
                        }else if(sentenceIter.hasNext()){
                            sentenceElement = sentenceIter.next();
							sentenceIterIndex++;
                            //System.out.println("sequence: " + sequenceElement.toString(wordForm) + ",\tsentence: " + sentenceElement.toString(wordForm)); //DEBUG
                        //if no match and no more sentence elements, then fail and quit
                        }else{
                            match = false;
                            break;
                        }
                    }
				}
            }
            
            //for sequence and sentence to match, both need to have been processed to the end (or remaining sentence elements are deemed irrelevant -> then sentenceIter is null)
            match &= !sequenceIter.hasNext() && (sentenceIter == null || !sentenceIter.hasNext());
            
        }
		in.upperBoundary = sentenceIterIndex;
        
		if(match){
			return in;
		}else{
			EmptyInterval dummy = new EmptyInterval();
            dummy.upperBoundary = sentenceIterIndex + 1;
            return dummy;
		}
    }
    
    private IntegerInterval getSequenceMatch(Sentence sentence){
        int sentenceIterIndex = 0;
        IntegerInterval in = null;
        while(sentenceIterIndex < sentence.getLength() - 1){
            in = getSequenceMatchStartingAt(sentenceIterIndex, sentence);
            
            //DEBUG
            //String s = "searching sentence " + sentence.toString(wordForm) + " from position " + sentenceIterIndex;
            //s += ", return: " + in.toString();
            //System.out.println(s); //DEBUG
            
            if(in instanceof EmptyInterval){
                sentenceIterIndex = in.upperBoundary;
            }else{
                return in;
            }
        }
        return in;
    }
	
	

	//returns indices of all target words whose context window contains (not just overlaps) the smallest sentence interval matching this sequence
	private UnionOfIntervals getTargetWordIndicesForSequenceMatch(IntegerInterval sequenceMatch, IntegerInterval contextWindow){
		IntegerInterval targetWordInterval = contextWindow.invert().shiftLowerBoundaryBy(sequenceMatch.upperBoundary).shiftUpperBoundaryBy(sequenceMatch.lowerBoundary);
		UnionOfIntervals u = new UnionOfIntervals();
		u.unifyWith(targetWordInterval);
		
		return u;
	}
		
	//returns a set of indices of occurences of this word sequence (which is only one context word)
	private SetOfIntegers getWordMatches(Sentence sentence){
		SetOfIntegers matches = new SetOfIntegers(sentence.getLength());
		
		Word sWord = (Word) elements.get(0);
		int i=0;
		for(Word word : sentence.getWords()){
			if(sWord.matches(word)){
				matches.add(i);
			}
			i++;
		}
		
		//System.out.println("word matches: " + matches.toString()); //DEBUG
		return matches;
	}
	
	private UnionOfIntervals getTargetWordIndicesForWordMatches(int[] contextWordIndices, IntegerInterval contextWindow){
		//save set of all applicable target words in here
		UnionOfIntervals unionOfNeighbourhoods = new UnionOfIntervals();
		
		//get the neighbourhood for each target word
		for(int contextWordIndex : contextWordIndices){
			IntegerInterval neighbourhood = contextWindow.invert().shiftBoundariesBy(contextWordIndex);
			unionOfNeighbourhoods.unifyWith(neighbourhood);
		}

		return unionOfNeighbourhoods;
	}
	
	
	@Override
	//returns a set of indices of (target) words whose context window contains this word sequence
	public UnionOfIntervals[] getTargetWordIndices(ParsedSentence parsedSentence, BagOfContextWindows contextWindows){
		Sentence sentence = parsedSentence.getSentence();
		if(sentence == null) return null;
		
		UnionOfIntervals[] matchesPerContextWindow = new UnionOfIntervals[contextWindows.size()];
		TreeMap<IntegerInterval, UnionOfIntervals> targetSentenceInternalWindowMatchesMap = new TreeMap<>();
		
		//if this word sequence is only one context word
		if(getSize() == 1 && elements.get(0) instanceof Word){
			//get indices of all occurences of given context word
			int[] contextWordIndices = getWordMatches(sentence).toIntArray();
			contextElementCount += contextWordIndices.length;
			
			//don't compute target word indices redundantly (only one per distinct target sentence internal window)
			for(ContextWindow contextWindow : contextWindows){
				//if context window even covers target sentence, and its target sentence internal window has not been processed before
				if(contextWindow.documentInternalSentenceWindow.contains(0) && !targetSentenceInternalWindowMatchesMap.containsKey(contextWindow.targetSentenceInternalWindow)){
					//compute target word indices and cache them
					UnionOfIntervals u = getTargetWordIndicesForWordMatches(contextWordIndices, contextWindow.targetSentenceInternalWindow);
					targetSentenceInternalWindowMatchesMap.put(contextWindow.targetSentenceInternalWindow, u);
				}
			}
			
		//if this word sequence is complex
		}else{
			IntegerInterval sequenceMatch = getSequenceMatch(sentence);
            //don't compute target word indices redundantly (only one per distinct target sentence internal window)
			for(ContextWindow contextWindow : contextWindows){
				//if context window even covers target sentence, and its target sentence internal window has not been processed before
				if(!(sequenceMatch instanceof EmptyInterval) && contextWindow.documentInternalSentenceWindow.contains(0) && !targetSentenceInternalWindowMatchesMap.containsKey(contextWindow.targetSentenceInternalWindow)){
					contextElementCount++;
					UnionOfIntervals u = getTargetWordIndicesForSequenceMatch(sequenceMatch, contextWindow.targetSentenceInternalWindow);
					targetSentenceInternalWindowMatchesMap.put(contextWindow.targetSentenceInternalWindow, u);
				}
			}
		}
		
		//go through all given context windows
		for(int i=0; i<contextWindows.size(); i++){
			//get the above-computed target sentence's target word indices. use multiply if needed by multiple context windows
			UnionOfIntervals u = targetSentenceInternalWindowMatchesMap.get(contextWindows.get(i).targetSentenceInternalWindow);
			if(u == null){
				matchesPerContextWindow[i] = new UnionOfIntervals();
			}else{
				matchesPerContextWindow[i] = u;
			}
		}
		
		return matchesPerContextWindow;
	}
	
	
    /*public static void main(String[] args){
		Vocabulary.create(Word.TOKEN);

		String[] sentenceStrings = new String[]{
            "",
            "he drinks a lot of tea",
            "he usually drinks a lot of tea",
            "he drinks a lot of tea in the evenings",
            "he usually drinks a lot of tea in the evenings",
			"he usually drinks a lot of tea in the evenings usually",
			"he usually drinks a lot of tea in the evenings usually usually"
        };
		
		WordSequence[] sequences = new WordSequence[]{
			WordSequence.create("usually"),
			WordSequence.create("(? ? drinks#VBZ * * ? tea#NN * ? * *)"),
            WordSequence.create("(? ? ?)")
		};
		
		BagOfContextWindows contextWindows = new BagOfContextWindows();
        contextWindows.add(new DoubleUnlimitedInterval());
        contextWindows.add(new LimitedInterval(-1, 1));
        contextWindows.add(new LimitedInterval(0, 1));
        contextWindows.add(new LimitedInterval(-1, 0));
        contextWindows.add(new LimitedInterval(-5, 5));

		//go through all sentences in data
		for(String sentenceString : sentenceStrings){
			String s = "sentence: " + sentenceString + "\n";
			ParsedSentence ps = new ParsedSentence();
			ps.setSentence(new Sentence(sentenceString));
			//go through each word sequence
			for(WordSequence sequence : sequences){
				s += "sequence: " + sequence.toString() + "\n";
				UnionOfIntervals[] us = sequence.getTargetWordIndices(ps, contextWindows);
				//go through all context windows
				for(int i=0; i<contextWindows.size(); i++){
					IntegerInterval contextWindow = contextWindows.get(i).sentenceInternalInterval;
					s += "context window: " + contextWindow + "\n";
					s += "target words: " + ps.getWords(us[i]).toString() + "\n";
				}
				s += "\n";
			}
			System.out.println(s);
		}
		
    }
    */

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String asString(Object o) {
        String s = "(";
        Iterator<WordSequenceElement> iter = elements.iterator();
        while(iter.hasNext()){
            WordSequenceElement e = iter.next();
            s += e.asString(o);
            s += (iter.hasNext() ? " " : ")");
        }
        
        return s;
    }

}
