package models.corpus;

import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaPrefs;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.AgigaToken;
import edu.jhu.agiga.AgigaTypedDependency;
import edu.jhu.agiga.StreamingDocumentReader;
import java.io.File;
import java.io.IOException;
import lingunit.language.Document;
import experiment.common.Description;
import experiment.common.Label;
import java.util.HashMap;
import java.util.List;
import lingunit.dependency.DepNode;
import lingunit.dependency.DepRelation;
import lingunit.dependency.DepTree;
import lingunit.flattext.Sentence;
import lingunit.flattext.Word;
import lingunit.language.ParsedSentence;
import models.AbstractModelThread;
import pipeline.signals.PipelineSignal;
import pipeline.signals.StartSignal;
import vocabulary.Vocabulary;


//does not include punctuation
public class AgigaCorpusReader extends CorpusReader{
    
    public AgigaCorpusReader(File corpusFolder) throws IOException{
        super(corpusFolder);
    }
    
    @Override
    public synchronized void signalSuperior(AbstractModelThread thread, Label threadLabel, PipelineSignal signal) throws IOException{
        if(signal instanceof Document){
            Document doc = (Document) signal;
            signalPipeline(threadLabel, doc);
        }
    }
    
    @Override
	public Description getDescription(){
		Description d = super.getDescription();
		d.setTypeAttribute("agiga corpus reader");
		
		return d;
	}

@Override
    protected AbstractModelThread createThread(Label threadLabel){ //label: corpus file
        File corpusFile = (File) threadLabel.getObject(0);
        AgigaCorpusReaderThread thread = new AgigaCorpusReaderThread(this, threadLabel, corpusFile, sentenceFormat, maxAmountOfDocuments);
        
        return thread;
    }

    @Override
    protected void startModel() throws IOException{
        report("Ready...");
        
        for(File corpusFile : corpusFolder.listFiles()){
            Label corpusFileLabel = new Label(corpusFile);
            startThread(corpusFileLabel);
            signalPipeline(corpusFileLabel, new StartSignal());
        }
        
        allowSuccessorToFinish();
    }

    @Override
    protected void finishModel() {
        report("...Finished reading corpus from " + amountOfFinishedThreads + " files in " + corpusFolder.getAbsolutePath());
    }

}



class AgigaCorpusReaderThread extends AbstractCorpusReaderThread{

    protected StreamingDocumentReader agigaDocumentReader;
	
	public AgigaCorpusReaderThread(AgigaCorpusReader superior, Label threadLabel, File corpusFile, int sentenceFormat, int maxAmountOfDocuments){
        super(superior, threadLabel, corpusFile, sentenceFormat, maxAmountOfDocuments);
		
		AgigaPrefs agigaPrefs = new AgigaPrefs();
		agigaPrefs.setAll(false);
		agigaPrefs.setWord(true);
		agigaPrefs.setLemma(true);
		agigaPrefs.setPos(true);
		agigaPrefs.setBasicDeps(true);

		agigaDocumentReader = new StreamingDocumentReader(corpusFile.getAbsolutePath(), agigaPrefs);
    }

	@Override
    public Document readDocument() throws IOException{
		Document document = null;
		
		if(agigaDocumentReader.hasNext()){
			AgigaDocument agigaDocument = agigaDocumentReader.next();
            document = new Document(agigaDocument.getDocId());
			
            //go through agiga sentences in agiga document
			int documentInternalSentenceId = 0;
			for(AgigaSentence agigaSentence : agigaDocument.getSents()){

                //get words and dep arcs for this agiga sentence
				List<AgigaToken> agigaWords = agigaSentence.getTokens();
                List<AgigaTypedDependency> agigaDepArcs = agigaSentence.getBasicDeps();
                HashMap<Integer, Word> agigaWordIndexWordMap = new HashMap<>(); //neede because not all agiga words are definitely connected to the main tree
                
                //create a sentence-dep tree pair for document
                Sentence sentence = new Sentence();
                DepTree depTree = new DepTree();
                
                //build sentence object
                for(AgigaToken agigaWord : agigaWords){
                    
                    //build word object
                    String token = agigaWord.getWord(); //TODO: should this be case-insensitive?
                    String lemma = agigaWord.getLemma().toLowerCase(); //lower case for all lemmas
                    String pos = agigaWord.getPosTag().toUpperCase();
                    Word word = Vocabulary.getWord(token, lemma, pos);
					//word.increaseTargetElementCountByOne();
					//Vocabulary.increaseTotalWordCountByOne();
                    
                    //save agiga word index for later
                    int agigaWordIndex = agigaWord.getTokIdx();
                    agigaWordIndexWordMap.put(agigaWordIndex, word);
                    //Helper.report("index " + agigaWordIndex + " " + word); //DEBUG

					//add word to sentence
                    sentence.add(word);
                    
                    //add word to dep tree (add further attributes later)
                    depTree.add(word);
                }
                
                //build deptree object
				for(AgigaTypedDependency agigaDepArc : agigaDepArcs){
                    //agiga dep node indices correspond to agiga word indices
                    int dependentIndex = agigaDepArc.getDepIdx(); 
                    String relationWithHead = agigaDepArc.getType().toUpperCase();
                    int governorIndex = agigaDepArc.getGovIdx();
                    
					//add this arc and its inverse to dep tree
                    DepNode depNode = depTree.getNode(dependentIndex);
                    DepNode headNode = depTree.getNode(governorIndex); //head node is null if dep node is root or is not connected with main tree
                    
                    //detect root node
                    if(governorIndex == -1 && relationWithHead.equals("ROOT")){
                        depTree.setRootNode(depNode);
                    
                    //add dep arcs and their inverses to tree
                    }else if(headNode != null){
                        //add arc from dep to head
                        depNode.setHeadNode(headNode);
                        depNode.setRelationWithHead(new DepRelation(relationWithHead));
                        //add inverse arc from head to dep
                        headNode.addChildNode(depNode);
                    }
				}
				
                ParsedSentence ps = new ParsedSentence(document, documentInternalSentenceId);
				if((sentenceFormat == ParsedSentence.SENTENCES || sentenceFormat == ParsedSentence.SENTENCES_AND_DEPTREES) && !sentence.isEmpty()){
					ps.setSentence(sentence);
				}
				if((sentenceFormat == ParsedSentence.DEPTREES || sentenceFormat == ParsedSentence.SENTENCES_AND_DEPTREES) && !depTree.isEmpty()){
					ps.setDepTree(depTree);
				}
                document.addParsedSentence(ps);
			}

		}
		
		return document;
    }

    @Override
    protected synchronized void readDocuments() throws IOException{
        Document doc;
        try{
            while((doc = readDocument()) != null){
                if(amountOfDocuments > 0 && amountOfDocuments % 1000 == 0) report(corpusFile.getName(), "...done reading " + amountOfDocuments + " documents...");
                superior.signalSuperior(this, threadLabel, doc);
                amountOfDocuments++;
                if(maxAmountOfDocuments > 0 && amountOfDocuments > maxAmountOfDocuments) break;
                wait();
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }
		
		report(corpusFile.getName(), "...Finished after reading " + amountOfDocuments + " documents.");
    }
    
}