package experiment.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import lingunit.flattext.Word;
import meta.Describable;
import meta.Helper;
import vocabulary.Vocabulary;

public class BagOfTargetElements extends ArrayList<Word> implements Describable {
    
    private File inputFile;
    
    public static BagOfTargetElements importFrom(File file) throws IOException{
        System.out.println("[BagOfTargetElements] Importing target elements from file...");
        BagOfTargetElements bag = new BagOfTargetElements();
        
        ArrayList<String> targetWordStrings = Helper.importWordsAsList(file);
        for(String targetWordString : targetWordStrings){
            Word targetWord = Vocabulary.getWord(targetWordString);
			bag.add(targetWord);
            //targetWord.bagOfMeaningRepresentations = new BagOfMeaningRepresentations();
		}
        
        bag.inputFile = file;
        
        System.out.println("[BagOfTargetElements] ...Finished importing target elements (size: " + bag.size() + ") from file");
        return bag;
    }

    @Override
    public Description getDescription() {
        Description d = new Description();
        d.addParameter("target elements input file", (inputFile != null ? "" : inputFile.getAbsolutePath()));
        d.addParameter("size", this.size());
        
        return d;
    }
    
}
