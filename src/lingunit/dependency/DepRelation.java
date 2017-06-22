package lingunit.dependency;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Objects;
import meta.Exportable;
import meta.Printable;
import pattern.dependency.AnyDepRelation;

public class DepRelation implements Comparable, Exportable, Printable{

    protected String name;
    protected boolean isFromDependentToHead;
    
    public DepRelation(String name){
		if(name.endsWith("-1")){
			this.name = name.substring(0, name.length() - 2);
			isFromDependentToHead = false;
		}else{
			this.name = name;
			isFromDependentToHead = true;
		}
	}
    public DepRelation(){
        this("");
    }

    
    public String getName(){
        return name;
    }
    public void setName(String name){
		if(name.endsWith("-1")){
			this.name = name.substring(0, name.length() - 2);
			isFromDependentToHead = false;
		}else{
			this.name = name;
			isFromDependentToHead = true;
		}
    }
    
    public DepRelation invert(){
        DepRelation inverseRelation = new DepRelation(name);
        inverseRelation.setIsFromDependentToHead(!isFromDependentToHead);

        return inverseRelation;
    }
    
    public boolean isFromDependentToHead(){
        return isFromDependentToHead;
    }
	public void setIsFromDependentToHead(boolean isFromDependentToHead){
		this.isFromDependentToHead = isFromDependentToHead;
	}

    public boolean matches(DepRelation rel){
        return name.equals(rel.getName()) && isFromDependentToHead() == rel.isFromDependentToHead();
    }
    
    //given "" or "?", this returns an AnyDepRelation from dependent to head
    //given "-1" or "?-1" this returns an AnyDepRelation from head to dependent
    //otherwise this returns a concrete DepRelation from dependent to head iff given name does not end with "-1"
    public static DepRelation create(String relationString){
        DepRelation rel;
        
		if(relationString.isEmpty() || relationString.equals("?")){
			rel = new AnyDepRelation();
			rel.setIsFromDependentToHead(true);
		}else if(relationString.equals("-1") || relationString.equals("?-1")){
			rel = new AnyDepRelation();
			rel.setIsFromDependentToHead(false);
		}else{
			rel = new DepRelation(relationString);
		}
        
        return rel;
    }
    
    @Override
    public int compareTo(Object o) {
        DepRelation rel = (DepRelation) o;
        int c = name.compareTo(rel.name);
        if(c != 0) return c;
        c = Boolean.compare(isFromDependentToHead, rel.isFromDependentToHead);
        return c;
    }
	
	@Override
	public int hashCode(){
		return (isFromDependentToHead() ? 1 : -1) * name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DepRelation other = (DepRelation) obj;
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		if (this.isFromDependentToHead != other.isFromDependentToHead) {
			return false;
		}
		return true;
	}

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        return asString(null);
    }
    
    @Override
    public String asString(Object o) {
        return name + (isFromDependentToHead ? "" : "-1");
    }

    
    
}
