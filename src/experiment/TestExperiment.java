package experiment;

import experiment.common.Parameters;
import java.io.File;
import java.io.IOException;
import models.AbstractModel;
import models.withDataset.SimilarityFunction;
import models.withDataset.WordSim353;
import models.io.Importer;
import models.pointwise.AssociationFunction;

public class TestExperiment extends AbstractExperiment{

    private File parametersFile;
            
    public TestExperiment(){
        parametersFile = new File(".../parameters.txt");;
    }
    public TestExperiment(File parametersFile){
        this.parametersFile = parametersFile;
    }
    
    @Override
	public void prepareParameters(File parametersFile) throws IOException{
		Parameters.importFrom(parametersFile);
	}
    
    @Override
    public AbstractModel[] prepareModels() throws IOException{
        return new AbstractModel[]{
            //CorpusReader.create("conll corpus reader")
            //,Counter.create("clustering counter")
            //,SequentialClusteringModel.create("sequential agglomerative clustering")
            //,Counter.create("simple counter")
            //,Exporter.create(Parameters.getFileParameter("count vectors output folder"))
            //,Printer.create("pretty print")
            //,AssociationFunction.create("ppmi")
            //,Exporter.create(Parameters.getFileParameter("association function output folder"))
            //,Printer.create()
            Importer.create(Parameters.getFileParameter("count vectors output folder"))
            ,AssociationFunction.create(AssociationFunction.RELATIVE_PROB)
            //,Printer.create("pretty print")
            //Importer.create(Parameters.getFileParameter("association function output folder"))
            ,WordSim353.create(
                Parameters.getFileParameter("dataset file"),
                SimilarityFunction.create("cosine"))
        };
    }
    
    public void run(){
        try{
			prepareParameters(parametersFile);
            AbstractModel[] models = prepareModels();
			prepareExperiment();
            startExperiment(models);
            
		}catch(IOException e){
			e.printStackTrace();
		}
    }
		
	public static void main(String[] args){
		TestExperiment exp;
        if(args.length == 0){
            exp = new TestExperiment();
        }else{
            exp = new TestExperiment(new File(args[0]));
        }
        
        exp.run();
	}

}
