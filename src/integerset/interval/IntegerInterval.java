package integerset.interval;

import integerset.AbstractSetOfIntegers;
import java.util.Objects;
import meta.Describable;
import experiment.common.Description;
import java.io.BufferedWriter;
import java.io.IOException;
import meta.Exportable;

public abstract class IntegerInterval extends AbstractSetOfIntegers implements Describable, Comparable, Exportable{

    public static final int DOUBLE_UNLIMITED_INTERVAL = 0;
    public static final int EMPTY_INTERVAL = 1;
    public static final int LIMITED_INTERVAL = 2;
    public static final int LOWER_UNLIMITED_INTERVAL = 3;
    public static final int UPPER_UNLIMITED_INTERVAL = 4;
    
    public Integer lowerBoundary, upperBoundary; //null denotes negative/positive infinity
    public int type;
    
    public abstract boolean overlapsWith(IntegerInterval in);
	public abstract boolean neighboursOrOverlapsWith(IntegerInterval in);
	public abstract boolean contains(IntegerInterval in);
	public abstract boolean contains(int i);
	public abstract boolean properlyContains(IntegerInterval in);
    public abstract IntegerInterval intersect(IntegerInterval in);
    public abstract IntegerInterval getCopy();
    public abstract IntegerInterval invert();
    public abstract IntegerInterval shiftLowerBoundaryBy(int delta);
    public abstract IntegerInterval shiftUpperBoundaryBy(int delta);
    
    public IntegerInterval shiftBoundariesBy(int delta){
		return shiftLowerBoundaryBy(delta).shiftUpperBoundaryBy(delta);
    }
    
    public abstract String getType();
    
    public static IntegerInterval importFromString(String s){
        String[] entries = s.split(" ");
        String type = entries[0];
        Integer l = (entries[1].equals("null") ? null : Integer.parseInt(entries[1]));
        Integer u = (entries[2].equals("null") ? null : Integer.parseInt(entries[2]));
        
        IntegerInterval in;
        switch(type){
            case "DoubleUnlimitedInterval":
                in = new DoubleUnlimitedInterval();
                break;
            case "EmptyInterval":
                in = new EmptyInterval();
                break;
            case "LimitedInterval":
                in = new LimitedInterval();
                break;
            case "LowerUnlimitedInterval":
                in = new LowerUnlimitedInterval(u);
                break;
            case "UpperUnlimitedInterval":
                in = new UpperUnlimitedInterval(l);
                break;
            default:
                in = null;
        }
        
        if(in != null){
            in.lowerBoundary = l;
            in.upperBoundary = u;
        }
        
        return in;
    }
    
    @Override
    public Description getDescription(){
        Description d = new Description();
		d.setTypeAttribute("integer interval");
        d.addParameter("lower boundary", "" + lowerBoundary);
        d.addParameter("upper boundary", "" + upperBoundary);
        
        return d;
    }
    
    /*@Override
    public Description getInputDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Description getOutputDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	*/

    @Override
    public int compareTo(Object o){
        IntegerInterval in = (IntegerInterval) o;
        int c;
        if((c = Integer.compare(this.type, in.type)) != 0) return c;
        if(lowerBoundary != null && in.lowerBoundary == null) return 1;
        if(lowerBoundary == null && in.lowerBoundary != null) return -1;
        if(lowerBoundary != null && in.lowerBoundary != null & (c = Integer.compare(lowerBoundary, in.lowerBoundary)) != 0) return c;
        if(upperBoundary != null && in.upperBoundary == null) return 1;
        if(upperBoundary == null && in.upperBoundary != null) return -1;
        if(upperBoundary != null && in.upperBoundary != null & (c = Integer.compare(upperBoundary, in.upperBoundary)) != 0) return c;
        return 0;
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof IntegerInterval){
            return compareTo(o) == 0;
        }else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = this.getClass().toString().hashCode();
        hash += 7 * Objects.hashCode(this.lowerBoundary);
        hash += 13 * Objects.hashCode(this.upperBoundary);
        return hash;
    }
    
    public static IntegerInterval create(Description d){
        IntegerInterval in = null;
        
        if(d.getTypeAttribute().equals("integer interval")){
            String lowerString = (String) d.getParameter("lower boundary");
            String upperString = (String) d.getParameter("upper boundary");
            String type = (String) d.getParameter("type");
            int lower = -1, upper = -1;
            if(lowerString != null && upperString != null && type != null){
                if(!lowerString.equals("null")) lower = Integer.parseInt(lowerString);
                if(!upperString.equals("null")) upper = Integer.parseInt(upperString);
                switch(type){
                    case "limited":
                        in = new LimitedInterval(lower, upper);
                        break;
                    case "empty":
                        in = new EmptyInterval();
                        break;
                    case "lower unlimited":
                        in = new LowerUnlimitedInterval(upper);
                        break;
                    case "upper unlimited":
                        in = new UpperUnlimitedInterval(lower);
                        break;
                    case "double unlimited":
                        in = new DoubleUnlimitedInterval();
                        break;
                }
            }
        }
        
        return in;
    }

    @Override
	public Object exportTo(BufferedWriter writer) throws IOException{
		writer.write(getType() + " " + lowerBoundary.toString() + " " + upperBoundary.toString());
        return true;
	}

}
