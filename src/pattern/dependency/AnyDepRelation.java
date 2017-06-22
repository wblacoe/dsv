package pattern.dependency;

import lingunit.dependency.DepRelation;

public class AnyDepRelation extends DepRelation {

    public AnyDepRelation() {
        super("");
    }
    
    @Override
    public DepRelation invert(){
        AnyDepRelation rel = new AnyDepRelation();
        rel.isFromDependentToHead = !isFromDependentToHead;
        
        return rel;
    }
    
    @Override
    public boolean matches(DepRelation rel){
        return isFromDependentToHead == rel.isFromDependentToHead();
    }
    
    @Override
    public String asString(Object o){
        return "?";
    }
    
}