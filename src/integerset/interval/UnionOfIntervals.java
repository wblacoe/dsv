package integerset.interval;

import integerset.AbstractSetOfIntegers;
import integerset.SetOfIntegers;
import java.util.ArrayList;
import java.util.Iterator;

public final class UnionOfIntervals extends AbstractSetOfIntegers{

	private DoubleUnlimitedInterval doubleUnlimited; //if not null, a double unlimited interval (-> lower and upper are null and limitedList is empty)
	private LowerUnlimitedInterval lower; //if not null, a lower unlimited interval not overlapping with upper or any intervals in limitedList (-> doubleUnlimited is null)
	private UpperUnlimitedInterval upper;//if not null, an upper unlimited interval not overlapping with lower or any intervals in limitedList (-> doubleUnlimited is null)
	private ArrayList<LimitedInterval> limitedList; //if not empty, a list of non-overlapping limited intervals (-> doubleUnlimited is null)
	
	public UnionOfIntervals(){
		limitedList = new ArrayList<>();
	}
	
	public UnionOfIntervals(IntegerInterval in){
		this();
		unifyWith(in);
	}
	
	public UnionOfIntervals(SetOfIntegers set){
		this();
		for(int i : set.toIntArray()){
			unifyWith(new LimitedInterval(i, i));
		}
	}
	
	private void resetEverything(){
		doubleUnlimited = null;
		lower = null;
		upper = null;
		limitedList.clear();
	}
	
	@Override
	public boolean isEmpty(){
		return doubleUnlimited == null && lower == null && upper == null && limitedList.isEmpty();
	}
	
	@Override
	public Integer getSize(){
		if(doubleUnlimited != null || lower != null || upper != null){
			return null;
		}else{
			int size = 0;
			for(LimitedInterval limited : limitedList){
				size += limited.getSize();
			}
			return size;
		}
	}
	
	public ArrayList<IntegerInterval> getIntervals(){
		ArrayList<IntegerInterval> intervals = new ArrayList<>();
		if(doubleUnlimited != null) intervals.add(doubleUnlimited);
		if(lower != null) intervals.add(lower);
		if(upper != null) intervals.add(upper);
		intervals.addAll(limitedList);
		
		return intervals;
	}
	
	public void unifyWith(IntegerInterval in){
		if(doubleUnlimited != null){
			//do nothing
		}else if(in instanceof EmptyInterval){
			//do nothing
		}else if(in instanceof DoubleUnlimitedInterval){
			resetEverything();
			doubleUnlimited = (DoubleUnlimitedInterval) in;
		}else if(in instanceof LowerUnlimitedInterval){
			LowerUnlimitedInterval l = (LowerUnlimitedInterval) in;
			if(upper != null && upper.neighboursOrOverlapsWith(l)){
				resetEverything();
				doubleUnlimited = new DoubleUnlimitedInterval();
			}else{
				if(l.properlyContains(lower)) lower = l;
				Iterator<LimitedInterval> it = limitedList.iterator();
				while(it.hasNext()){
					LimitedInterval limited = it.next();
					if(lower.neighboursOrOverlapsWith(limited)){
						if(limited.upperBoundary > lower.upperBoundary) lower.upperBoundary = limited.upperBoundary;
						it.remove();
					}
				}
			}
		}else if(in instanceof UpperUnlimitedInterval){
			UpperUnlimitedInterval u = (UpperUnlimitedInterval) in;
			if(lower != null && lower.neighboursOrOverlapsWith(u)){
				resetEverything();
				doubleUnlimited = new DoubleUnlimitedInterval();
			}else{
				if(u.properlyContains(upper)) upper = u;
				Iterator<LimitedInterval> it = limitedList.iterator();
				while(it.hasNext()){
					LimitedInterval limited = it.next();
					if(upper.neighboursOrOverlapsWith(limited)){
						if(limited.lowerBoundary < upper.lowerBoundary) upper.lowerBoundary = limited.lowerBoundary;
						it.remove();
					}
				}
			}
		}else if(in instanceof LimitedInterval){
			LimitedInterval m = (LimitedInterval) in;
			Iterator<LimitedInterval> it = limitedList.iterator();
			while(it.hasNext()){
				LimitedInterval limited = it.next();
				if(m.neighboursOrOverlapsWith(limited)){
					m.lowerBoundary = Math.min(m.lowerBoundary, limited.lowerBoundary);
					m.upperBoundary = Math.max(m.upperBoundary, limited.upperBoundary);
					it.remove();
				}
			}
			if(lower != null && lower.neighboursOrOverlapsWith(m)) lower.upperBoundary = m.upperBoundary;
			if(upper != null && upper.neighboursOrOverlapsWith(m)) upper.lowerBoundary = m.lowerBoundary;
			if(lower != null && upper != null && lower.neighboursOrOverlapsWith(upper)){
				resetEverything();
				doubleUnlimited = new DoubleUnlimitedInterval();
			}else{
				limitedList.add(m);
			}
		}
		
	}
	
	public void unifyWith(UnionOfIntervals uin){
		if(uin.doubleUnlimited != null) unifyWith(uin.doubleUnlimited);
		if(uin.lower != null) unifyWith(uin.lower);
		if(uin.upper != null) unifyWith(uin.upper);
		for(LimitedInterval limited : uin.limitedList) unifyWith(limited);
	}
	
	public void intersect(IntegerInterval in){
		if(doubleUnlimited != null){
			resetEverything();
			unifyWith(in);
		}else if(isEmpty()){
			//do nothing
		}else if(in instanceof DoubleUnlimitedInterval){
			//do nothing
		}else if(in instanceof EmptyInterval){
			resetEverything();
		}else if(in instanceof LowerUnlimitedInterval){
			LowerUnlimitedInterval l = (LowerUnlimitedInterval) in;
			if(lower != null && lower.properlyContains(l)) lower = l;
			Iterator<LimitedInterval> it = limitedList.iterator();
			while(it.hasNext()){
				LimitedInterval limited = it.next();
				if(limited.overlapsWith(l)){
					limited.upperBoundary = Math.min(limited.upperBoundary, l.upperBoundary);
				}else{
					it.remove();
				}
			}
			if(upper != null){
				if(upper.overlapsWith(l)) limitedList.add(new LimitedInterval(upper.lowerBoundary, l.upperBoundary));
				upper = null;
			}
		}else if(in instanceof UpperUnlimitedInterval){
			UpperUnlimitedInterval u = (UpperUnlimitedInterval) in;
			if(upper != null && upper.properlyContains(u)) upper = u;
			Iterator<LimitedInterval> it = limitedList.iterator();
			while(it.hasNext()){
				LimitedInterval limited = it.next();
				if(limited.overlapsWith(u)){
					limited.lowerBoundary = Math.max(limited.lowerBoundary, u.lowerBoundary);
				}else{
					it.remove();
				}
			}
			if(lower != null){
				if(lower.overlapsWith(u)) limitedList.add(new LimitedInterval(lower.upperBoundary, u.lowerBoundary));
				lower = null;
			}
		}else if(in instanceof LimitedInterval){
			LimitedInterval m = (LimitedInterval) in;
			Iterator<LimitedInterval> it = limitedList.iterator();
			while(it.hasNext()){
				LimitedInterval limited = it.next();
				if(limited.overlapsWith(m)){
					limited.lowerBoundary = Math.max(limited.lowerBoundary, m.lowerBoundary);
					limited.upperBoundary = Math.min(limited.upperBoundary, m.upperBoundary);
				}else{
					it.remove();
				}
			}
			if(lower != null){
				if(lower.overlapsWith(m)) limitedList.add(new LimitedInterval(m.lowerBoundary, Math.min(lower.upperBoundary, m.upperBoundary)));
				lower = null;
			}
			if(upper != null){
				if(upper.overlapsWith(m)) limitedList.add(new LimitedInterval(Math.max(upper.lowerBoundary, m.lowerBoundary), m.upperBoundary));
				upper = null;
			}
		}
		
	}
	
	public boolean contains(IntegerInterval in){
		if((doubleUnlimited != null && doubleUnlimited.contains(in)) ||
			(lower != null && lower.contains(in)) ||
			(upper != null && upper.contains(in)))
			return true;
		for(LimitedInterval limited : limitedList){
			if(limited.contains(in)) return true;
		}
		return false;
	}
	
	public boolean contains(int i){
		if((doubleUnlimited != null && doubleUnlimited.contains(i)) ||
			(lower != null && lower.contains(i)) ||
			(upper != null && upper.contains(i)))
			return true;
		for(LimitedInterval limited : limitedList){
			if(limited.contains(i)) return true;
		}
		return false;
	}
	
	@Override
	public String toString(){
		String s = "{";
		if(doubleUnlimited != null) s += doubleUnlimited.toString();
		if(lower != null) s += lower.toString();
		if(upper != null) s += upper.toString();
		for(LimitedInterval in : limitedList){
			s += in.toString() + ",";
		}
		s += "}";
				
		return s;
	}

    @Override
    public String asString(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	
}
