package lingunit.flattext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import meta.ExportableAndComparable;
import pattern.flattext.WordSequenceElement;
import pipeline.signals.PipelineSignal;
import vocabulary.Vocabulary;

//only Vocabulary is allowed to instantiate new words, otherwise unindexed words will be created and cause chaos
//word objects must only come out of a vocabulary object, not created (unindexed) on the fly
public class Word extends WordSequenceElement implements ExportableAndComparable, PipelineSignal{
	
	public static final int TOKEN = 0;
	public static final int LEMMA = 1;
	public static final int TOKEN_AND_POS = 2;
	public static final int LEMMA_AND_POS = 3;
    public static final String[] WORD_FORMS = new String[]{ "token", "lemma", "token and pos", "lemma and pos" };

    public int vocabularyIndex;
	public String token, lemma, pos;
    
    public Word nextWordInVocabulary;
    public boolean isFirstWordInVocabulary, isLastWordAddedInVocabulary;
	
	public long targetElementCount;
	public double logTargetElementCount;

    
    public Word(){
		vocabularyIndex = -1;
		targetElementCount = 0;
		logTargetElementCount = Double.NaN;
    }
	public Word(String token, String lemma, String pos){
		this();
        if(Vocabulary.wordForm == TOKEN || Vocabulary.wordForm == TOKEN_AND_POS) this.token = token;
		if(Vocabulary.wordForm == LEMMA || Vocabulary.wordForm == LEMMA_AND_POS) this.lemma = lemma;
		if(Vocabulary.wordForm == TOKEN_AND_POS || Vocabulary.wordForm == LEMMA_AND_POS) this.pos = pos;
	}
	public Word(int vocabularyIndex, String token, String lemma, String pos){
		this(token, lemma, pos);
		this.vocabularyIndex = vocabularyIndex;
	}
    
	
	public void increaseTargetElementCountByOne(){
		targetElementCount++;
	}
	
	public long getTargetElementCount(){
		return targetElementCount;
	}
	
	public void setTargetElementCount(long targetElementCount){
		this.targetElementCount = targetElementCount;
	}
	
	public double getLogTargetElementCount(){
		if(Double.isNaN(logTargetElementCount)){
			logTargetElementCount = Math.log(targetElementCount);
		}
		
		return logTargetElementCount;
	}
    
    private boolean matchesToken(Word w){
        return (w.token == null && token == null) || w.token.equals(token) || w.token.equals("*ANY*") || token.equals("*ANY*");
    }
    
    private boolean matchesLemma(Word w){
        return (w.lemma == null && lemma == null) || w.lemma.equals(lemma) || w.lemma.equals("*ANY*") || lemma.equals("*ANY*");
    }
    
    private boolean matchesPos(Word w){
        return (w.pos == null && pos == null) || w.pos.equals(pos) || pos.equals("?") || pos.equals("*ANY*") || w.pos.equals("?") || w.pos.equals("*ANY*");
    }
    
    @Override
    public boolean matches(Word w){
		switch(Vocabulary.wordForm){
			case TOKEN:
                return matchesToken(w);
            case LEMMA:
                return matchesLemma(w);
            case TOKEN_AND_POS:
                return matchesToken(w) && matchesPos(w);
            case LEMMA_AND_POS:
                return matchesLemma(w) && matchesPos(w);
            default:
                return false;
		}
    }
	
    public String toXmlString(){
        return "<word token=\"" + (token == null ? "" : token) + "\" lemma=\"" + (lemma == null ? "" : lemma) + "\" pos=\"" + (pos == null ? "" : pos) + " vocabularyIndex=\"" + vocabularyIndex + "\">";
    }
    
    
    public static Word importFrom(BufferedReader in) throws IOException{
        Word w = null;
        
        String line = in.readLine();
        //Helper.report("[Word] processing line \"" + line + "\"..."); //DEBUG
        if(line != null){
            String[] entries = line.split("\t");
            
            if(entries.length == 1){
                w = Vocabulary.getWord(entries[0]);
                
            }else if(entries.length == 4){
                w = new Word();
                w.vocabularyIndex = Integer.parseInt(entries[0]);
                //if(!entries[1].equals("*NULL*")) w.token = entries[1];
                //if(!entries[2].equals("*NULL*")) w.lemma = entries[2];
                //if(!entries[3].equals("*NULL*")) w.pos = entries[3];
                
                String[] pair;
                switch(Vocabulary.wordForm){
                    case Word.LEMMA:
                        w.lemma = entries[1];
                        break;
                    case Word.TOKEN:
                        w.token = entries[1];
                        break;
                    case Word.LEMMA_AND_POS:
                        pair = entries[1].split("#");
                        w.lemma = pair[0];
                        w.pos = pair[1];
                        break;
                    case Word.TOKEN_AND_POS:
                        pair = entries[1].split("#");
                        w.token = pair[0];
                        w.pos = pair[1];
                        break;
                }
                
                w.targetElementCount = Long.parseLong(entries[2]);
                w.logTargetElementCount = Double.parseDouble(entries[3]);
                
                //Helper.report("[Word] adding indexed word to vocabulary: " + w.toXmlString()); //DEBUG
                Vocabulary.addWordWithIndex(w);
            }
            
            /*if(entries.length > 4){
                for(int i=4; i<entries.length; i++){
                    if(entries[i].equals("*FIRST_WORD_IN_VOCABULARY*")) w.isFirstWordInVocabulary = true;
                    else if(entries[i].equals("*LAST_WORD_ADDED_IN_VOCABULARY*")) w.isLastWordAddedInVocabulary = true;
                }
            }
			*/
        }
        
        return w;
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(vocabularyIndex, ((Word) o).vocabularyIndex);
    }

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        writer.write(asString(Vocabulary.wordForm) + "\n");
        return true;
    }

    @Override
    public String asString(Object o) {
        int wordForm;
        if(o == null){
            wordForm = Vocabulary.wordForm;
        }else{
            wordForm = (int) o;
        }
        
        switch(wordForm){
			case TOKEN:
				return token;
			case LEMMA:
				return lemma;
			case TOKEN_AND_POS:
				return token + "#" + pos;
			case LEMMA_AND_POS:
				return lemma + "#" + pos;
			default:
				return null;
		}
    }
    
}
