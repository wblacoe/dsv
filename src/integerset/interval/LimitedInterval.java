package integerset.interval;

//expresses an interval over the natural numbers (>=0) using left and right boundary indices

import experiment.common.Description;

public class LimitedInterval extends IntegerInterval{

    public LimitedInterval(){
        type = LIMITED_INTERVAL;
    }
    
    //we assume that left boundary <= right boundary
	public LimitedInterval(int lowerBoundary, int upperBoundary){
        this();
		this.lowerBoundary = lowerBoundary;
		this.upperBoundary = upperBoundary;
	}
	
    @Override
	public boolean overlapsWith(IntegerInterval in){
		if(in instanceof EmptyInterval){
			return false;
		}else if(in instanceof DoubleUnlimitedInterval){
			return true;
		}else if(in instanceof LowerUnlimitedInterval){
			return in.upperBoundary >= lowerBoundary;
		}else if(in instanceof UpperUnlimitedInterval){
			return in.lowerBoundary <= upperBoundary;
		}else if(in instanceof LimitedInterval){
			return in.upperBoundary >= lowerBoundary || in.lowerBoundary <= upperBoundary;
		}else{
			return false;
		}
	}
	
	@Override
	public boolean neighboursOrOverlapsWith(IntegerInterval in) {
        if(in instanceof EmptyInterval){
			return false;
		}else if(in instanceof DoubleUnlimitedInterval){
			return true;
		}else if(in instanceof LowerUnlimitedInterval){
			return in.upperBoundary >= lowerBoundary - 1;
		}else if(in instanceof UpperUnlimitedInterval){
			return in.lowerBoundary <= upperBoundary + 1;
		}else if(in instanceof LimitedInterval){
			return in.upperBoundary >= lowerBoundary - 1 && in.lowerBoundary <= upperBoundary + 1;
		}else{
			return false;
		}
    }
	
    @Override
	public boolean contains(IntegerInterval in){
		return in.lowerBoundary != null && in.lowerBoundary >= lowerBoundary && in.upperBoundary != null && in.upperBoundary <= upperBoundary;
	}
	
	@Override
    public boolean contains(int i) {
        return lowerBoundary <= i && i <= upperBoundary;
    }
	
    @Override
	public boolean properlyContains(IntegerInterval in){
		return in.lowerBoundary != null && in.lowerBoundary > lowerBoundary && in.upperBoundary != null && in.upperBoundary < upperBoundary;
	}

	@Override
	public String asString(Object o){
		return "[" + lowerBoundary + "," + upperBoundary + "]";
	}

    @Override
    public IntegerInterval intersect(IntegerInterval in) {
        if(in instanceof DoubleUnlimitedInterval){
            return getCopy();
        }else if(!overlapsWith(in)){
            return new EmptyInterval();
        }else if(in instanceof LimitedInterval){
            return new LimitedInterval(Math.max(lowerBoundary, in.lowerBoundary), Math.min(upperBoundary, in.upperBoundary));
        }else if(in instanceof LowerUnlimitedInterval){
            return new LimitedInterval(lowerBoundary, Math.min(upperBoundary, in.upperBoundary));
        }else if(in instanceof UpperUnlimitedInterval){
            return new LimitedInterval(Math.max(lowerBoundary, in.lowerBoundary), upperBoundary);
        }else{
            return null;
        }
    }

    @Override
    public IntegerInterval getCopy() {
        return new LimitedInterval(lowerBoundary, upperBoundary);
    }

    @Override
    public IntegerInterval invert() {
        return new LimitedInterval(-upperBoundary, -lowerBoundary);
    }

    @Override
    public IntegerInterval shiftLowerBoundaryBy(int delta) {
        return new LimitedInterval(lowerBoundary + delta, upperBoundary);
    }

    @Override
    public IntegerInterval shiftUpperBoundaryBy(int delta) {
        return new LimitedInterval(lowerBoundary, upperBoundary + delta);
    }

    @Override
    public String getType() {
        return "LimitedInterval";
    }

    @Override
    public Description getDescription(){
        Description d = super.getDescription();
        d.addParameter("type", "limited");
        
        return d;
    }

	@Override
	public boolean isEmpty() {
		return lowerBoundary > upperBoundary;
	}

	@Override
	public Integer getSize() {
		if(isEmpty()){
			return 0;
		}else{
			return upperBoundary - lowerBoundary + 1;
		}
	}
    
}
