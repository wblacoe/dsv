package models.corpus;

import java.io.File;
import java.io.IOException;
import lingunit.language.ParsedSentence;
import experiment.common.Parameters;
import experiment.common.Description;
import experiment.common.Label;
import lingunit.language.Document;
import models.AbstractModel;
import models.AbstractModelThread;
import pipeline.PipelineNode;

public abstract class CorpusReader extends AbstractModel{

    public static final int CONLL_CORPUS_READER = 0;
    public static final int AGIGA_CORPUS_READER = 1;
    public static final String[] CORPUS_READERS = new String[]{ "conll corpus reader", "agiga corpus reader" };
    
    public int sentenceFormat;
    public File corpusFolder;
    protected int maxAmountOfDocuments, maxAmountOfIOThreads;
    protected PipelineNode pipelineNode;
    
    public CorpusReader(File corpusFolder) throws IOException{
        sentenceFormat = Parameters.getConstantParameter("sentence format", ParsedSentence.SENTENCE_FORMATS);
        this.corpusFolder = corpusFolder;
        maxAmountOfDocuments = Parameters.hasParameter("amount of documents") ? Parameters.getIntParamter("amount of documents") : -1;
        maxAmountOfIOThreads = Parameters.getIntParamter("amount of threads");
    }
    
    @Override
    public Description getDescription(){
        Description d = new Description();
		d.setTypeAttribute("corpus reader");
        d.addParameter("sentence format", ParsedSentence.SENTENCE_FORMATS[sentenceFormat]);
        d.addParameter("corpus folder", corpusFolder.getAbsolutePath());
        
        return d;
    }
    
    public static CorpusReader create(int corpusReader, File corpusFolder) throws IOException{
        CorpusReader cr;
        
        switch(corpusReader){
            case CONLL_CORPUS_READER:
                cr = new ConllCorpusReader(corpusFolder);
                break;
            case AGIGA_CORPUS_READER:
                cr = new AgigaCorpusReader(corpusFolder);
                break;
            default:
                cr = null;
        }
        
        return cr;
    }
    
    public static CorpusReader create(String modelName) throws IOException{
        return create(
            getIndex(CORPUS_READERS, modelName),
            Parameters.getFileParameter("corpus folder")
        );
    }
    
    public static CorpusReader create(File courpusFolder) throws IOException{
        return create(
            Parameters.getConstantParameter("corpus reader", CORPUS_READERS),
            courpusFolder
        );
    }
    
    public static CorpusReader create() throws IOException{
        return create(
            Parameters.getConstantParameter("corpus reader", CORPUS_READERS),
            Parameters.getFileParameter("corpus folder")
        );
    }
    
    @Override
    public String protocol() {
        return "INPUT:\n" +
            "label: master label, signal: start signal\n" +
            "label: master label, signal: finish signal\n\n" +
				
            "OUTPUT:\n" +
            "label: master label, signal: start signal\n" +
			"for each corpus file in corpus folder:\n" +
			"  label: corpus file, signal: start signal\n" +
			"  for each document in corpus file:\n" +
			"    label: corpus file, signal: document";
    }
    
}


abstract class AbstractCorpusReaderThread extends AbstractModelThread{
    
    protected File corpusFile;
    protected int sentenceFormat;
    protected int amountOfDocuments, maxAmountOfDocuments;
    
    public AbstractCorpusReaderThread(AbstractModel superior, Label threadLabel, File corpusFile, int sentenceFormat, int maxAmountOfDocuments){
        super(superior, threadLabel);
        
        this.corpusFile = corpusFile;
        this.sentenceFormat = sentenceFormat;
        this.maxAmountOfDocuments = maxAmountOfDocuments;
        amountOfDocuments = 0;
    }
    
    protected abstract Document readDocument() throws IOException;
    protected abstract void readDocuments() throws IOException;

    @Override
    public void run(){
        try{
            readDocuments();
            finishThread();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
 
}