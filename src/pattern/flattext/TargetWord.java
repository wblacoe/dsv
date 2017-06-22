package pattern.flattext;

import lingunit.flattext.Word;
import vocabulary.Vocabulary;

public class TargetWord extends Word{
	
	public TargetWord() {
        super();
    }
    
    @Override
    public boolean matches(Word w){
		
		//POS doesn't matter
		if(Vocabulary.wordForm == Word.TOKEN || Vocabulary.wordForm == Word.LEMMA){
			return true;
		}
		
		//compare POS tags
		if(pos == null){
			return w.pos == null;
		}else{
			return pos.equals(w.pos) || pos.equals("?") || w.pos.equals("?");
		}
    }

    @Override
    public String toString() {
        if(Vocabulary.wordForm == Word.LEMMA_AND_POS || Vocabulary.wordForm == Word.TOKEN_AND_POS){
            return "*TARGET*#" + pos;
        }else{
            return "*TARGET*";
        }
    }

}
