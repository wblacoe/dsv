package integerset.interval;

import experiment.common.Description;

public class UpperUnlimitedInterval extends IntegerInterval{

    public UpperUnlimitedInterval(int lowerBoundary){
        this.lowerBoundary = lowerBoundary;
        type = UPPER_UNLIMITED_INTERVAL;
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
			return true;
		}else if(in instanceof LimitedInterval){
			return in.upperBoundary >= lowerBoundary;
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
			return true;
		}else if(in instanceof LimitedInterval){
			return in.upperBoundary >= lowerBoundary - 1;
		}else{
			return false;
		}
    }
	
    @Override
    public boolean contains(IntegerInterval in) {
        return (in.lowerBoundary != null && in.lowerBoundary >= lowerBoundary);
    }
	
	@Override
	public boolean contains(int i){
		return i >= lowerBoundary;
	}

    @Override
    public boolean properlyContains(IntegerInterval in) {
        return (in.upperBoundary != null && in.lowerBoundary != null && in.lowerBoundary > lowerBoundary);
    }

    @Override
	public String asString(Object o){
		return "[" + lowerBoundary + ",+inf[";
	}

    @Override
    public IntegerInterval intersect(IntegerInterval in) {
        if(in instanceof DoubleUnlimitedInterval){
            return getCopy();
        }else if(!overlapsWith(in)){
            return new EmptyInterval();
        }else if(in instanceof LimitedInterval){
            return new LimitedInterval(Math.max(lowerBoundary, in.lowerBoundary), in.upperBoundary);
        }else if(in instanceof LowerUnlimitedInterval){
            return new LimitedInterval(lowerBoundary, in.upperBoundary);
        }else if(in instanceof UpperUnlimitedInterval){
            return new UpperUnlimitedInterval(Math.max(lowerBoundary, in.lowerBoundary));
        }else{
            return null;
        }
    }

    @Override
    public IntegerInterval getCopy() {
        return new UpperUnlimitedInterval(lowerBoundary);
    }

    @Override
    public IntegerInterval invert() {
        return new LowerUnlimitedInterval(-lowerBoundary);
    }

    @Override
    public IntegerInterval shiftLowerBoundaryBy(int delta) {
        return new UpperUnlimitedInterval(lowerBoundary + delta);
    }

    @Override
    public IntegerInterval shiftUpperBoundaryBy(int delta) {
        return new UpperUnlimitedInterval(lowerBoundary);
    }

    @Override
    public String getType() {
        return "UpperUnlimitedInterval";
    }

    @Override
    public Description getDescription(){
        Description d = super.getDescription();
        d.addParameter("type", "upper unlimited");
        
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
