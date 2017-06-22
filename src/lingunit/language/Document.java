package lingunit.language;

import java.util.ArrayList;
import pipeline.signals.PipelineSignal;

public class Document implements PipelineSignal{

	private String name;
	private ArrayList<ParsedSentence> parsedSentences;
	
	public Document(){
		name = null;
		parsedSentences = new ArrayList<>();
	}
	public Document(String name){
		this();
		this.name = name;
	}
    
    
    public ParsedSentence get(int index){
        return parsedSentences.get(index);
    }
    
    public int getSize(){
        return parsedSentences.size();
    }
	
	public boolean isEmpty(){
		return getSize() == 0;
	}
    
    public String getName(){
        return name;
    }
    
    public void addParsedSentence(ParsedSentence parsedSentence){
        parsedSentence.setContainingDocument(this);
        parsedSentences.add(parsedSentence);
    }
    
    public ArrayList<ParsedSentence> getParsedSentences(){
        return parsedSentences;
    }

    public String toString(int wordForm){
        String s = "document: " + name + "\n";
        for(ParsedSentence parsedSentence : parsedSentences){
            s += parsedSentence.toString(wordForm);
        }
        return s;
    }
    
}
