package pattern.language;

import experiment.common.BagOfContextWindows;
import integerset.AbstractSetOfIntegers;
import lingunit.language.ParsedSentence;
import meta.Exportable;
import meta.Printable;
import pattern.dependency.DepPattern;
import pattern.flattext.WordSequence;

public abstract class LanguagePattern implements Exportable, Printable{

    public int languagePatternIndex = -1;
	
	public long contextElementCount;
	public double logContextElementCount;
	
	public LanguagePattern(){
		contextElementCount = 0;
		logContextElementCount = Double.NaN;
	}
    
	public abstract int getSize();
	public abstract AbstractSetOfIntegers[] getTargetWordIndices(ParsedSentence parsedSentence, BagOfContextWindows contextWindows);
	
	public static LanguagePattern create(String string){
		if(string.startsWith("[")){
			return DepPattern.create(string);
		}else{
			return WordSequence.create(string);
		}
	}
	
	public void increaseContextElementCountByOne(){
		contextElementCount++;
	}
	
	public long getContextElementCount(){
		return contextElementCount;
	}
	
	public void setContextElementCount(long contextElementCount){
		this.contextElementCount = contextElementCount;
	}
	
	public double getLogContextElementCount(){
		if(Double.isNaN(logContextElementCount)){
			logContextElementCount = Math.log(contextElementCount);
		}
		
		return logContextElementCount;
	}
    
}
