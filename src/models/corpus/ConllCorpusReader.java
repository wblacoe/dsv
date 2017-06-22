package models.corpus;

import lingunit.language.Document;
import java.io.File;
import java.io.IOException;
import experiment.common.Description;
import meta.Helper;
import experiment.common.Label;
import java.io.BufferedReader;
import java.util.HashMap;
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

//for each call: input: corpus folder -> corpus files (= label), output: one document per corpus file (= signal)
//includes punctuation
public class ConllCorpusReader extends CorpusReader {
	
    public ConllCorpusReader(File corpusFolder) throws IOException {
		super(corpusFolder);
    }
    
	@Override
    public synchronized void signalSuperior(AbstractModelThread thread, Label threadLabel, PipelineSignal signal) throws IOException{ //signal from subordinate thread
        if(signal instanceof Document){
            Document doc = (Document) signal;
            signalPipeline(threadLabel, doc); //label: corpus file
        }
        
        super.signalSuperior(thread, threadLabel, signal);
    }
	
	@Override
	public Description getDescription(){
		Description d = super.getDescription();
		d.setTypeAttribute("conll corpus reader");
		
		return d;
	}
    
    @Override
    protected AbstractModelThread createThread(Label threadLabel){ //label: corpus file
        File corpusFile = (File) threadLabel.getObject(0);
        ConllCorpusReaderThread thread = new ConllCorpusReaderThread(this, threadLabel, corpusFile, sentenceFormat, maxAmountOfDocuments);
        
        return thread;
    }

    @Override
    protected void startModel() throws IOException{
        report("Reading corpus from files in " + corpusFolder.getAbsolutePath() + "...");
        
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



class ConllCorpusReaderThread extends AbstractCorpusReaderThread{

    private BufferedReader in;
    
    public ConllCorpusReaderThread(ConllCorpusReader superior, Label threadLabel, File corpusFile, int sentenceFormat, int maxAmountOfDocuments){
        super(superior, threadLabel, corpusFile, sentenceFormat, maxAmountOfDocuments);
        
        this.sentenceFormat = sentenceFormat;
        this.maxAmountOfDocuments = maxAmountOfDocuments;
        
        try{
            in = Helper.getFileReader(corpusFile);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    

    //read only sentence, not dep tree
	private Sentence readSentence() throws IOException{
		Sentence sentence = new Sentence();
		
		String line;
		String[] entries;
		while((line = in.readLine()) != null){
			if(line.equals("</s>")){
				
			}else if((entries = line.split("\t")).length == 10){
				String token = entries[1]; //TODO: should this be case-insensitive?
				String lemma = entries[2].toLowerCase();
				String pos = entries[3].toUpperCase();
				Word word = Vocabulary.getWord(token, lemma, pos);
				
				sentence.add(word);
			}
		}
		
		return sentence;
	}
	
    //read dep tree and implicitly sentence
	private DepTree readDepTree() throws IOException{
		DepTree depTree = new DepTree();
        HashMap<DepNode, Integer> depNodeHeadIndexMap = new HashMap<>();
		
        boolean isDepTreeOpen = true;
		String line;
		String[] entries;
		while(isDepTreeOpen && (line = in.readLine()) != null){
			if(line.equals("</s>")){ //found end of sentence
                isDepTreeOpen = false;
                
			}else if((entries = line.split("\t")).length == 10){
                //int wordInSentenceIndex = Integer.parseInt(entries[0]);
				String token = entries[1].toLowerCase(); //should this be case-insensitive?
				String lemma = entries[2].toLowerCase();
				String pos = entries[3].toUpperCase();
				Word word = Vocabulary.getWord(token, lemma, pos);
				int headIndex = Integer.parseInt(entries[6]) - 1; //starts with 1 in corpus
				String relationWithHead = entries[7].toUpperCase();
				
                DepNode depNode = new DepNode(word, new DepRelation(relationWithHead));
                depTree.add(depNode);
                depNodeHeadIndexMap.put(depNode, headIndex);
			}
		}

        //connect nodes in the tree
        for(DepNode depNode : depTree.getNodes()){
            Integer headIndex = depNodeHeadIndexMap.get(depNode);
            //report(depNode + " : " + headIndex); //DEBUG
            DepNode headNode = depTree.getNode(headIndex);
            
            //detect root node
            if(headNode == null && depNode.getRelationWithHead().getName().equals("NULL")){
                depTree.setRootNode(depNode);

            //add dep arcs and their inverses to tree
            }else if(headNode != null){
                //add arc from dep to head
                depNode.setHeadNode(headNode);
                //add inverse arc from head to dep
                headNode.addChildNode(depNode);
            }
        }
		
		return depTree;
	}
	
	@Override
	public Document readDocument() throws IOException{
		Document document = null;

		boolean isDocumentOpen = true;
		String line;
		int documentInternalSentenceId = 0;
		while(isDocumentOpen){
            line = in.readLine();
            if(line == null){
                isDocumentOpen = false;
            }else if(line.startsWith("<text ")){ //found new document
                document = new Document(line);
                while(isDocumentOpen && (line = in.readLine()) != null){
                    if(line.equals("</text>")){ //found end of document
                        isDocumentOpen = false;
                    }else if(line.equals("<s>")){ //found new sentence
                        ParsedSentence ps = new ParsedSentence(document, documentInternalSentenceId++);
                        if(sentenceFormat == ParsedSentence.SENTENCES){
                            Sentence sentence = readSentence();
                            if(sentence != null && !sentence.isEmpty()){
                                ps.setSentence(sentence);
                            }
                        }else if(sentenceFormat == ParsedSentence.DEPTREES){
                            DepTree depTree = readDepTree();
                            if(depTree != null && !depTree.isEmpty()){
                                ps.setDepTree(depTree);
                            }
                        }else if(sentenceFormat == ParsedSentence.SENTENCES_AND_DEPTREES){
                            DepTree depTree = readDepTree();
                            if(depTree != null && !depTree.isEmpty()){
                                ps.setDepTree(depTree);
                                Sentence sentence = depTree.toSentence();
                                ps.setSentence(sentence);
                            }
                        }
                        document.addParsedSentence(ps);
                    }
                }
            }
		}
		
		return document;
	}
    
    @Override
    public synchronized void readDocuments(){
        Document doc;
        try{
            while((doc = readDocument()) != null){
                if(amountOfDocuments > 0 && amountOfDocuments % 100 == 0) report(corpusFile.getName(), "...done reading " + amountOfDocuments + " documents...");
                superior.signalSuperior(this, threadLabel, doc);
                if(maxAmountOfDocuments > 0 && amountOfDocuments >= maxAmountOfDocuments) break;
                amountOfDocuments++;
                wait();
            }
            in.close();
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
        }
		
		report(corpusFile.getName(), "...Finished after reading " + amountOfDocuments + " documents.");
    }
  
}