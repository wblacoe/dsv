package lingunit.flattext;
 
import java.util.ArrayList;
import java.util.List;
import vocabulary.Vocabulary;
 
public class Sentence {
 
    private ArrayList<Word> words;
     
    public Sentence(){
        words = new ArrayList<>();
    }
    public Sentence(String spaceDelimitedSentenceString){
        add(spaceDelimitedSentenceString.split(" "));
    }
    
	
	public ArrayList<Word> getWords(){
		return words;
	}
    
    public int add(Word word){
        int newIndex = words.size();
        words.add(word);
        return newIndex;
    }
    
   
    public void add(ArrayList<String> strings){
        for(String string : strings){
            add(Vocabulary.getWord(string));
        }
    }
    
    public void add(String[] strings){
        for(String string : strings){
            add(Vocabulary.getWord(string));
        }
    }
     
    public Word get(int index){
        return words.get(index);
    }
     
    public int getLength(){
        return words.size();
    }
     
    public boolean isEmpty(){
        return words.isEmpty();
    }
    
    public List<Word> getPartialSentence(Integer firstIndex, Integer lastIndex){
        if(firstIndex < 0) firstIndex = 0;
        if(lastIndex >= getLength()) lastIndex = getLength() - 1;
        
        return words.subList(firstIndex, lastIndex);
    }
    
    @Override
    public String toString(){
        String s = "";
        for(Word word : words){
            s += word.toString() + " ";
        }
        return s;
    }
     
}