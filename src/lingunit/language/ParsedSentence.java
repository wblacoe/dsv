package lingunit.language;

import integerset.AbstractSetOfIntegers;
import integerset.SetOfIntegers;
import integerset.interval.UnionOfIntervals;
import java.util.ArrayList;
import lingunit.dependency.DepNode;
import lingunit.dependency.DepTree;
import lingunit.flattext.Sentence;
import lingunit.flattext.Word;

public class ParsedSentence {
	
	public static final int SENTENCES = 0;
    public static final int DEPTREES = 1;
    public static final int SENTENCES_AND_DEPTREES = 2;
    public static final String[] SENTENCE_FORMATS = new String[]{ "sentences", "deptrees", "sentences and deptrees" };

    private Document containingDocument;
	private int documentInternalSentenceId;
    private Sentence sentence;
	private DepTree depTree;
    
    public ParsedSentence(){
        containingDocument = null;
		documentInternalSentenceId = -1;
        sentence = null;
        depTree = null;
    }
	public ParsedSentence(Document containingDocument, int documentInternalSentenceId){
		this();
		this.documentInternalSentenceId = documentInternalSentenceId;
		this.containingDocument = containingDocument;
	}
	
	
	public Document getContainingDocument(){
		return containingDocument;
	}
	
	public void setContainingDocument(Document doc){
		this.containingDocument = doc;
	}
	
	public int getDocumentInternalSentenceId(){
		return documentInternalSentenceId;
	}
	
	public Sentence getSentence(){
		return sentence;
	}
	
	public void setSentence(Sentence sentence){
		this.sentence = sentence;
	}
	
	public DepTree getDepTree(){
		return depTree;
	}
	
	public void setDepTree(DepTree depTree){
		this.depTree = depTree;
	}
    
    public String toString(int wordForm){
        String s = "";
        if(containingDocument != null) s += "containing document: " + containingDocument.getName() + "\n";
        if(sentence != null) s += "sentence: " + sentence.toString() + "\n";
        if(depTree != null) s += "depTree:\n" + depTree.toString(wordForm) + "\n";
        return s;
    }
	
	public ArrayList<Word> getWords(int[] indices){
		ArrayList<Word> words = new ArrayList<>();
		
		if(indices != null && indices.length > 0){
			if(sentence != null){
				for(int i : indices){
					words.add(sentence.get(i));
				}

			}else if(depTree != null){
				for(int i : indices){
					words.add(depTree.getNode(i).getWord());
				}

			}/*else{
				words = null;
			}*/
		}
		
		return words;
	}
	
	public ArrayList<Word> getWords(SetOfIntegers indices){
		return getWords(indices.toIntArray());
	}
	
	public ArrayList<Word> getWords(UnionOfIntervals u){
		ArrayList<Word> words = new ArrayList<>();
		
		if(u != null && !u.isEmpty()){
			if(sentence != null && !sentence.isEmpty()){
				for(int i=0; i<sentence.getLength(); i++){
					if(u.contains(i)) words.add(sentence.get(i));
				}

			}else if(depTree != null && !depTree.isEmpty()){
				for(DepNode node : depTree.getNodes()){
					if(u.contains(node.getNodeIndex())){
						words.add(node.getWord());
					}
				}

			}/*else{
				words = null;
			}*/
		}
		
		return words;
	}
    
    public ArrayList<Word> getWords(AbstractSetOfIntegers u){
        if(u instanceof SetOfIntegers){
            return getWords((SetOfIntegers) u);
        }else if(u instanceof UnionOfIntervals){
            return getWords((UnionOfIntervals) u);
        }else{
            return null;
        }
    }
	
	public Word[] getAllWords(){
		int size = getSize();
		Word[] words = new Word[size];
		
		if(sentence != null){
			for(int i=0; i<size; i++){
				words[i] = sentence.get(i);
			}
			return words;

		}else if(depTree != null){
			for(int i=0; i<size; i++){
				words[i] = depTree.getNode(i).getWord();
			}
			return words;
		}
		
		return null;
	}

	public int getSize(){
		if(sentence != null){
			return sentence.getLength();
		}else if(depTree != null){
			depTree.getAmountOfNodes();
		}
		
		return 0;
	}
    
}
