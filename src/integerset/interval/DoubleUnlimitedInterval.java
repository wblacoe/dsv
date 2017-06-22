package integerset.interval;

import experiment.common.Description;

public class DoubleUnlimitedInterval extends IntegerInterval{

    public DoubleUnlimitedInterval(){
        type = DOUBLE_UNLIMITED_INTERVAL;
    }

    @Override
    public boolean overlapsWith(IntegerInterval in) {
        return true;
    }
	
	@Override
	public boolean neighboursOrOverlapsWith(IntegerInterval in) {
        return true;
    }

    @Override
    public boolean contains(IntegerInterval in) {
        return true;
    }

    @Override
    public boolean contains(int i) {
        return true;
    }

    @Override
    public boolean properlyContains(IntegerInterval in) {
        return (in instanceof LimitedInterval) || (in instanceof EmptyInterval);
    }

    @Override
    public String asString(Object o){
        return "]-inf,+inf[";
    }

    @Override
    public IntegerInterval intersect(IntegerInterval in) {
        return in.getCopy();
    }

    @Override
    public IntegerInterval getCopy() {
        return new DoubleUnlimitedInterval();
    }

    @Override
    public IntegerInterval invert() {
        return new DoubleUnlimitedInterval();
    }

    @Override
    public IntegerInterval shiftLowerBoundaryBy(int delta) {
        return new DoubleUnlimitedInterval();
    }

    @Override
    public IntegerInterval shiftUpperBoundaryBy(int delta) {
        return new DoubleUnlimitedInterval();
    }

    @Override
    public String getType() {
        return "DoubleUnlimitedInterval";
    }

    @Override
    public Description getDescription(){
        Description d = super.getDescription();
        d.addParameter("type", "double unlimited");
        
        return d;
    }

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Integer getSize() {
		return null;
	}
    
}
