package pattern.flattext;

import lingunit.flattext.Word;

public class AnyWord extends Word {

    public AnyWord() {
        super();
    }
    
    //if AnyWord does not care about POS tags
    @Override
    public boolean matches(Word w){
        return true;
    }
    
    @Override
    public String asString(Object o){
        return "*ANY*";
    }
    
}