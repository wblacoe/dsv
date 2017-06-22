package integerset.interval;

import experiment.common.Description;

public class LowerUnlimitedInterval extends IntegerInterval{

    public LowerUnlimitedInterval(int upperBoundary){
        this.upperBoundary = upperBoundary;
        type = LOWER_UNLIMITED_INTERVAL;
    }
    
	@Override
	public boolean overlapsWith(IntegerInterval in){
		if(in instanceof EmptyInterval){
			return false;
		}else if(in instanceof DoubleUnlimitedInterval){
			return true;
		}else if(in instanceof LowerUnlimitedInterval){
			return true;
		}else if(in instanceof UpperUnlimitedInterval){
			return in.lowerBoundary <= upperBoundary;
		}else if(in instanceof LimitedInterval){
			return in.lowerBoundary <= upperBoundary;
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
			return true;
		}else if(in instanceof UpperUnlimitedInterval){
			return in.lowerBoundary <= upperBoundary + 1;
		}else if(in instanceof LimitedInterval){
			return in.lowerBoundary <= upperBoundary + 1;
		}else{
			return false;
		}
    }

    @Override
    public boolean contains(IntegerInterval in) {
        return (in.upperBoundary != null && in.upperBoundary <= upperBoundary);
    }
	
	@Override
	public boolean contains(int i){
		return i <= upperBoundary;
	}

    @Override
    public boolean properlyContains(IntegerInterval in) {
        return (in.lowerBoundary != null && in.upperBoundary != null && in.upperBoundary < upperBoundary);
    }

    @Override
	public String asString(Object o){
		return "]-inf," + upperBoundary + "]";
	}

    @Override
    public IntegerInterval intersect(IntegerInterval in) {
        if(in instanceof DoubleUnlimitedInterval){
            return getCopy();
        }else if(!overlapsWith(in)){
            return new EmptyInterval();
        }else if(in instanceof LimitedInterval){
            return new LimitedInterval(in.lowerBoundary, Math.min(upperBoundary, in.upperBoundary));
        }else if(in instanceof LowerUnlimitedInterval){
            return new LowerUnlimitedInterval(Math.min(upperBoundary, in.upperBoundary));
        }else if(in instanceof UpperUnlimitedInterval){
            return new LimitedInterval(in.lowerBoundary, upperBoundary);
        }else{
            return null;
        }
    }
    
    @Override
    public IntegerInterval getCopy() {
        return new LowerUnlimitedInterval(upperBoundary);
    }

    @Override
    public IntegerInterval invert() {
        return new UpperUnlimitedInterval(-upperBoundary);
    }

    @Override
    public IntegerInterval shiftLowerBoundaryBy(int delta) {
        return new LowerUnlimitedInterval(upperBoundary);
    }

    @Override
    public IntegerInterval shiftUpperBoundaryBy(int delta) {
        return new LowerUnlimitedInterval(upperBoundary + delta);
    }

    @Override
    public String getType() {
        return "LowerUnlimitedInterval";
    }

    @Override
    public Description getDescription(){
        Description d = super.getDescription();
        d.addParameter("type", "lower unlimited");
        
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
