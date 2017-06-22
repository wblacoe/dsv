package experiment.common;

import meta.Helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import meta.Describable;
import meta.Exportable;
import meta.Printable;

public class BagOfContextWindows extends ArrayList<ContextWindow> implements Describable, Exportable, Printable{
	
	private File inputFile;

    public BagOfContextWindows(){
        super();
    }
    
    public void addContextWindow(ContextWindow cw){
        cw.contextWindowIndex = size();
        add(cw);
    }
    
    public static BagOfContextWindows importFrom(BufferedReader in) throws IOException{
        BagOfContextWindows bag = new BagOfContextWindows();
        
        String line;
        while((line = in.readLine()) != null){
            if(!line.startsWith("#")){
                ContextWindow cw = ContextWindow.importFromString(line);
                bag.addContextWindow(cw);
            }
        }
        
        return bag;
    }
    
    public static BagOfContextWindows importFrom(File file) throws IOException{
        System.out.println("[BagOfContextWindows] Importing context windows from file...");
        
        BagOfContextWindows bag;
        BufferedReader in = Helper.getFileReader(file);
        bag = importFrom(in);
        in.close();
        
        bag.inputFile = file;
        
        System.out.println("[BagOfContextWindows] ...Finished importing context windows (size: " + bag.size() + ") from file");
        return bag;
    }
    
    
    
    @Override
    public Description getDescription(){
        Description d = new Description();
		d.setTypeAttribute("bag of context windows");
        d.addParameter("size", this.size());
		d.addParameter("context windows input file", (inputFile != null ? "" : inputFile.getAbsolutePath()));
        
        return d;
    }

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException{
        for(ContextWindow cw : this){
            cw.exportTo(writer);
            writer.write("\n");
        }
        return true;
    }
    
    @Override
    public String asString(Object o){
        String s = "intervals:\n";
        int i=0;
        for(ContextWindow cw : this){
            s += "" + i + ": " + cw.asString(o) + "\n";
            i++;
        }
        
        return s;
    }

}
