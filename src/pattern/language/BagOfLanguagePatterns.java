package pattern.language;

import meta.Helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import meta.Describable;
import experiment.common.Description;
import meta.Exportable;
import meta.Printable;

public class BagOfLanguagePatterns extends ArrayList<LanguagePattern> implements Describable, Exportable, Printable{

    //private ArrayList<LanguagePattern> languagePatterns;
	private File inputFile;
    
    //public BagOfLanguagePatterns(){
        //languagePatterns = new ArrayList<>();
    //}
    
    //public LanguagePattern get(int languagePatternIndex){
        //return languagePatterns.get(languagePatternIndex);
    //}
    
    public void addLanguagePattern(LanguagePattern languagePattern){
        if(languagePattern != null){
			//lp.languagePatternIndex = languagePatterns.size();
			languagePattern.languagePatternIndex = size();
			//languagePatterns.add(lp);
			add(languagePattern);
		}
    }
	
	//public int getSize(){
		//return languagePatterns.size();
	//}
    
    //public ArrayList<LanguagePattern> getLanguagePatterns(){
        //return languagePatterns;
    //}
	
    @Override
	public String asString(Object o){
		String s = "language patterns:\n";
		
		for(int i=0; i<size(); i++){
			s += "" + i + ": " + get(i).asString(o) + " (" + get(i).getClass() + ")\n";
		}
		
		return s;
	}
    
    
    public static BagOfLanguagePatterns importFrom(BufferedReader in) throws IOException{
        System.out.println("[BagOfLanguagePatterns] Importing language patterns from file...");
        BagOfLanguagePatterns bag = new BagOfLanguagePatterns();
        
        String line;
        LanguagePattern lp;
        while((line = in.readLine()) != null){
            
            String[] entries = line.split("\t");
            if(entries.length == 1){
                lp = LanguagePattern.create(line);
				lp.languagePatternIndex = bag.size();
                
            }else if(entries.length == 4){
                lp = LanguagePattern.create(entries[1]);
                lp.languagePatternIndex = Integer.parseInt(entries[0]);
                lp.contextElementCount = Long.parseLong(entries[2]);
                lp.logContextElementCount = Double.parseDouble(entries[3]);
                
            }else{
                break;
            }
            
            //System.out.println("Creating new Word, word string: " + lpString + ", word form: " + Word.WORD_FORMS[wordForm] + ", created word: " + lp.toString()); //DEBUG
            bag.add(lp);
        }
        
        System.out.println("[BagOfLanguagePatterns] ...Finished importing language patterns (size: " + bag.size() + ") from file");
        return bag;
    }
    
    public static BagOfLanguagePatterns importFrom(File file) throws IOException{
        BagOfLanguagePatterns bag;
        BufferedReader in = Helper.getFileReader(file);
        bag = importFrom(in);
        in.close();
        
        bag.inputFile = file;
        
        return bag;
    }
    
	@Override
	public Description getDescription(){
		Description d = new Description();
		d.setTypeAttribute("bag of language patterns");
		d.addParameter("context elements input file", (inputFile != null ? "" : inputFile.getAbsolutePath()));
		d.addParameter("size", "" + size());
		
		return d;
	}

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        Object parameter = null;
        for(int i=0; i<size(); i++){
            LanguagePattern lp = get(i);
            writer.write("" + lp.languagePatternIndex + "\t" + lp.asString(parameter) + "\t" + lp.contextElementCount + "\t" + lp.logContextElementCount + "\n");
        }
        writer.close();
        return true;
    }

}