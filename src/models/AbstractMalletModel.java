package models;

import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import vector.AbstractVector;
import vector.VectorEntry;

public abstract class AbstractMalletModel extends AbstractModel implements AddDataVectors{
    
    public Alphabet alphabet;
	public InstanceList dataset;
    
    public AbstractMalletModel(){
        alphabet = new Alphabet();
		dataset = new InstanceList(alphabet, null);
		//TODO: add all context elements into alphabet?? order needs to be preserved for indices to be the same (I think?)
        //all target words need to be in the collection of context words.
        //only words can be language patterns for a mallet experiment
    }
/*
    public InstanceList getInstanceListFromCorpusFile1(File corpusFile, String targetWord, int amountOfSentences){
        Helper.report("[Test] Collecting instances in dataset...");
        
		InstanceList dataset = new InstanceList(alphabet, null);
        
        boolean isCorpusFileOpen = true, isDocumentOpen = true, isSentenceOpen = true;
        int instanceCounter = 0, sentenceCounter = 0;
        int every = 100;
        try{
            BufferedReader in = Helper.getFileReader(corpusFile);
            
            String line;
            while(isCorpusFileOpen && (line = in.readLine()) != null){
                //discover new document
                if(line.startsWith("<text")){
                    isDocumentOpen = true;
                    FeatureSequence fs = new FeatureSequence(alphabet);
                    while(isDocumentOpen && (line = in.readLine()) != null){
                        //save document
                        if(line.equals("</text>")){
                            isDocumentOpen = false;
                            if(fs.size() > 0){
                                //add instance to dataset
                                Instance instance = new Instance(fs, null, null, null);
                                dataset.add(instance);
                                instanceCounter++;
                                if(instanceCounter % every == 0) Helper.report("[Test] " + instanceCounter + " instances have been added to the dataset...");;
                            }
                        //discover new sentence
                        }else if(line.startsWith("<s")){
                            isSentenceOpen = true;
                            ArrayList<String> wordsCache = new ArrayList<>();
                            String[] entries;
                            while(isSentenceOpen && (line = in.readLine()) != null){
                                //save sentence
                                if(line.equals("</s>")){
                                    isSentenceOpen = false;
                                    //only consider sentences containing "wear"
                                    if(wordsCache.contains(targetWord)){
                                        //add this sentence's words to instance
                                        for(String word : wordsCache){
                                            int alphabetIndex = alphabet.lookupIndex(word);
                                            fs.add(alphabetIndex);
                                        }
                                        sentenceCounter++;
                                        if(sentenceCounter % every == 0) Helper.report("[Test] " + sentenceCounter + " sentences have been added to the dataset...");;
                                        //stop after desired amount of sentences
                                        if(amountOfSentences > -1 && sentenceCounter >= amountOfSentences){
                                            isDocumentOpen = false;
                                            isCorpusFileOpen = false;
                                        }
                                    }
                                //discover new word
                                }else if(!line.isEmpty() && (entries = line.split("\t")).length == 10){
                                    //cache words in this sentence
                                    String word = entries[2]; //lemma
                                    wordsCache.add(word);
                                }
                            }
                        }
                    }
                }
            }

            in.close();
        }catch(IOException e){
            e.printStackTrace();
        }

        Helper.report("[Test] ...Finished collecting " + instanceCounter + " instances in dataset.");
        return dataset;
    }
    
    public InstanceList getInstanceListFromDocument(Document document, int wordForm){
        InstanceList instanceList = new InstanceList(alphabet, null);
        
        for(ParsedSentence parsedSentence : document.getParsedSentences()){
            Sentence sentence = parsedSentence.getSentence();
            if(sentence != null && !sentence.isEmpty()){
                FeatureSequence featureSequence = new FeatureSequence(alphabet);
                for(Word word : sentence.getWords()){
                    String wordString = word.toString(wordForm);
                    int alphabetIndex = alphabet.lookupIndex(wordString);
                    featureSequence.add(alphabetIndex);
                }
                Instance instance = new Instance(featureSequence, null, null, null);
                instanceList.add(instance);
            }
        }
        
        return instanceList;
    }
    
    public InstanceList getInstanceListFromDocument(){
        int wordForm = Word.LEMMA_AND_POS;
        Vocabulary vocabulary = new Vocabulary(wordForm);
        Document document = Tester.getDocument(vocabulary);
        System.out.println(vocabulary.toString(false)); //DEBUG
        
        return getInstanceListFromDocument(document, wordForm);
    }

	//e.g. amount of instances = amount of documents, vector dimensionality = vocabulary size, vector cardinality = amount of words per document (here a constant)
	public InstanceList getRandomSparseInstanceList(int amountOfInstances, int vectorDimensionality, int vectorCardinality){
		InstanceList instanceList = new InstanceList(alphabet, null);
		
		//fill alphabet
		for(int i=0; i<vectorDimensionality; i++){
			String word = "dim" + i;
			alphabet.lookupIndex(word);
		}
		
		for(int i=0; i<amountOfInstances; i++){
			FeatureSequence featureSequence = new FeatureSequence(alphabet);
			
			for(int j=0; j<vectorCardinality; j++){
				int randomDimension = (int) (Math.random() * vectorDimensionality);
				String word = "dim" + randomDimension;
				int alphabetIndex = alphabet.lookupIndex(word);
				featureSequence.add(alphabetIndex);
			}
			
			Instance instance = new Instance(featureSequence, null, null, null);
			instanceList.add(instance);
			
			System.out.println("Finished generating " + i + " out of " + amountOfInstances + " random sparse instances");
		}
		
		return instanceList;
	}
 */

	/*@Override
	//assumes data to be <Integer, Integer>
	public void addDataVector(AbstractVector dataVector) {
		Iterator it = dataVector.iterator();
		FeatureSequence fs = new FeatureSequence(alphabet);
		while(it.hasNext()){
			Entry entry = (Entry) it.next();
			Integer dimension = (Integer) entry.getKey(); //dimension = context element index
			Integer amount = (Integer) entry.getValue(); //how often the context element index appears in the "document"
			for(int i=0; i<amount; i++) fs.add(dimension); //add this context element into the feature sequence as often as it appears in "document" (I hope the feature sequence order is irrelevant?)
		}
		Instance instance = new Instance(fs, null, null, null);
		dataset.add(instance);
	}
	
	@Override
	public void randomlyPopulateDataset(float relativeCardinality){
		Integer amountOfDataPoints, dataDimensionality;
		if((amountOfDataPoints = Parameters.getIntParamter("amount of data points")) != null && (dataDimensionality = Parameters.getIntParamter("data dimensionality")) != null){
			for(int i=0; i<amountOfDataPoints; i++){
				AbstractVector dataVector = SparseIntegerVector.createRandom(dataDimensionality, relativeCardinality);
				addDataVector(dataVector);
			}
		}
	}
	*/

	/*@Override
	public ArrayList<String> requiredParameters() {
		ArrayList<String> rp = super.requiredParameters();
		return rp;
	}
    */
	
	/*@Override
	public void importDataVectors(Description inputVectorsSource){
		for(Word targetWord : exp.targetWords){
			AbstractVector dataVector = (AbstractVector) targetWord.bagOfMeaningRepresentations.get(inputVectorsSource);
			Iterator it = dataVector.iterator();
			FeatureSequence fs = new FeatureSequence(alphabet);
			while(it.hasNext()){
				Entry entry = (Entry) it.next();
				Integer dimension = (Integer) entry.getKey(); //dimension = context element index
				Integer amount = (Integer) entry.getValue(); //how often the context element index appears in the "document"
				for(int i=0; i<amount; i++) fs.add(dimension); //add this context element into the feature sequence as often as it appears in "document" (I hope the feature sequence order is irrelevant?)
			}
			Instance instance = new Instance(fs, null, null, null);
			dataset.add(instance);
		}
	}
    
    /*@Override
    public Description getDescription(){
        Description d = super.getDescription();
        
        
        return d;
    }
	*/
    
    @Override
	public void addDataVector(AbstractVector countVector) { //assumes that this is an integer vector
		FeatureSequence fs = new FeatureSequence(alphabet);
		for(VectorEntry ve : countVector){
			for(int i=0; i<ve.getIntValue(); i++) fs.add(ve.getDimension()); //add this context element into the feature sequence as often as it appears in "document" (I hope the feature sequence order is irrelevant?)
		}
		Instance instance = new Instance(fs, null, null, null);
		dataset.add(instance);
	}
	
}