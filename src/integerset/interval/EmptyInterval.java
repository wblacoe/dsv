package integerset.interval;

import experiment.common.Description;

public class EmptyInterval extends IntegerInterval{
    
    public EmptyInterval(){
        type = EMPTY_INTERVAL;
    }

    @Override
    public boolean overlapsWith(IntegerInterval in) {
        return false;
    }

	@Override
	public boolean neighboursOrOverlapsWith(IntegerInterval in) {
        return false;
    }
	
    @Override
    public boolean contains(IntegerInterval in) {
        return false;
    }
	
	@Override
    public boolean contains(int i) {
        return false;
    }

    @Override
    public boolean properlyContains(IntegerInterval in) {
        return false;
    }

    @Override
    public IntegerInterval intersect(IntegerInterval in) {
        return new EmptyInterval();
    }
    
    @Override
    public String asString(Object o){
        return "[]";
    }

    @Override
    public IntegerInterval getCopy() {
        return new EmptyInterval();
    }

    @Override
    public IntegerInterval invert() {
        return new EmptyInterval();
    }

    @Override
    public IntegerInterval shiftLowerBoundaryBy(int delta) {
        return new EmptyInterval();
    }

    @Override
    public IntegerInterval shiftUpperBoundaryBy(int delta) {
        return new EmptyInterval();
    }

    @Override
    public String getType() {
        return "EmptyInterval";
    }

    @Override
    public Description getDescription(){
        Description d = super.getDescription();
        d.addParameter("type", "empty");
        
        return d;
    }

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public Integer getSize() {
		return 0;
	}

}
