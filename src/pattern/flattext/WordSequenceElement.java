package pattern.flattext;

import lingunit.flattext.Word;
import meta.Exportable;
import meta.Printable;
import vocabulary.Vocabulary;

public abstract class WordSequenceElement implements Exportable, Printable{

    public static WordSequenceElement create(String string){
        switch (string) {
            case "*ANY*":
                return new AnyWord();
            case "*":
                return new AnyWords();
            default:
                return Vocabulary.getWord(string);
        }
    }
    
    public abstract boolean matches(Word w);
    
}
