package vector.complex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;
import lingunit.flattext.Word;
import meta.Describable;
import experiment.common.Description;
import meta.Exportable;
import experiment.common.Label;
import meta.ExportableAndComparable;
import meta.Printable;
import pipeline.signals.PipelineSignal;

public class BagOfMeaningRepresentations extends TreeMap<ExportableAndComparable, MeaningRepresentation> implements Exportable, Describable, PipelineSignal, Printable{
    
    private final Label labelWithoutTargetWord;
    private Description contentDescription;
    
    public BagOfMeaningRepresentations(Label label, Description contentDescription){
        this.labelWithoutTargetWord = label;
        this.contentDescription = contentDescription;
    }
	
	public BagOfMeaningRepresentations(Label label){
        this.labelWithoutTargetWord = label;
    }
    
    public Label getLabel(){
        return labelWithoutTargetWord;
    }
	
	public void setContentDescription(Description contentDescription){
		this.contentDescription = contentDescription;
	}
	
	public Description getContentDescription(){
		return contentDescription;
	}
    
    public boolean hasContentDescription(){
        return contentDescription != null;
    }
    
    @Override
    public Description getDescription(){
        Description d = new Description();
        d.setTypeAttribute("bag of meaning representations");
        d.addParameter("label", labelWithoutTargetWord.getDescription());
        if(contentDescription != null){
            d.addParameter("content", contentDescription);
        }
        
        return d;
    }

    /*public static BagOfMeaningRepresentations importFrom(Label labelPrefix, BufferedReader in, String typeOfMeaningRepresentation, int dimensionality) throws IOException{
        BagOfMeaningRepresentations bag = new BagOfMeaningRepresentations();
        
        String line;
        Label label;
        while(true){
            line = in.readLine();
            if(line == null) break;
            Word word = Vocabulary.getWord(line);
            (label = ((Label) labelPrefix.getCopy())).addObject(word);
            MeaningRepresentation mr = null;
            if(typeOfMeaningRepresentation.equals("sparse integer vector")){
                mr = SparseIntegerVector.importFrom(in, dimensionality);
                if(mr == null) break;
            }
            bag.put(label, mr);
        }
        
        return bag;
    }
    */
    
    public static BagOfMeaningRepresentations importFrom(BufferedReader in)throws IOException{
        
        //import header description
        Description d = Description.importFrom(in);
        Label labelWithoutTargetWord = Label.create((Description) d.getParameter("label"));
        Description contentD = (Description) d.getParameter("content");
        BagOfMeaningRepresentations bag = new BagOfMeaningRepresentations(labelWithoutTargetWord, contentD);
        
        Word word;
        MeaningRepresentation mr;
        //import all meaning representations (they have the form described in the bag's content description
        while((word = Word.importFrom(in)) != null && (mr = MeaningRepresentation.importFrom(in, contentD)) != null){
            bag.put(word, mr);
        }

        return bag;
    }

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        Description d = getDescription();

        //write this bag's description as header
        d.exportTo(writer);
        writer.write("\n");

        //for each entry in this bag
        for(Entry<ExportableAndComparable, MeaningRepresentation> entry : this.entrySet()){
            //write word and word's meaning representation
            entry.getKey().exportTo(writer);
            entry.getValue().exportTo(writer);
        }
        
        return true;
    }

    @Override
    public String asString(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
