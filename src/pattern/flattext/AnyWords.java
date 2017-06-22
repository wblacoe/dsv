package pattern.flattext;

import java.io.BufferedWriter;
import java.io.IOException;
import lingunit.flattext.Word;

public class AnyWords extends WordSequenceElement {
    
    @Override
    public String asString(Object o){
        return "*";
    }
    
    @Override
    public boolean matches(Word w){
        return false;
    }

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
