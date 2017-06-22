package vocabulary;

import experiment.common.Description;
import meta.Helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;
import lingunit.flattext.Word;
import experiment.common.Label;


//contains all words in corpus, target elements and context elements
public class Vocabulary{
	
    //public static int wordForm; //entries are only sensitive to words/tokens/pos according to this flag
	//private TreeMap<Word, Word> words; //map word to word so that an existing word with vocabulary index can be searched by an ad hoc created word without vocabulary index
    
    public static int wordForm;
    public static Word firstWordAdded, lastWordAdded;
	private static long totalWordCount = 0;
	private static double logTotalWordCount = Double.NaN;
    
	private static TreeMap<Word, Word> words = new TreeMap<>(new Comparator(){
        
        @Override
        public int compare(Object o1, Object o2) {
            Word w1 = (Word) o1;
            Word w2 = (Word) o2;

            if(wordForm == Word.TOKEN){
                if(w1.token == null && w2.token == null){
                    return 0;
                }else if(w1.token == null){
                    return 1;
                }else if(w2.token == null){
                    return -1;
                }else{
                    return w1.token.compareTo(w2.token);
                }

            }else if(wordForm == Word.LEMMA){
                if(w1.lemma == null && w2.lemma == null){
                    return 0;
                }else if(w1.lemma == null){
                    return 1;
                }else if(w2.lemma == null){
                    return -1;
                }else{
                    return w1.lemma.compareTo(w2.lemma);
                }

            }else if(wordForm == Word.TOKEN_AND_POS){
                int c;
                if(w1.token == null && w2.token == null){
                    return 0;
                }else if(w1.token == null){
                    return 1;
                }else if(w2.token == null){
                    return -1;
                }else if((c = w1.token.compareTo(w2.token)) != 0){
                    return c;
                }else if(w1.pos == null && w2.pos == null){
                    return 0;
                }else if(w1.pos == null){
                    return 1;
                }else if(w2.pos == null){
                    return -1;
                }else{
                    return w1.pos.compareTo(w2.pos);
                }

            }else if(wordForm == Word.LEMMA_AND_POS){
                int c;
                if(w1.lemma == null && w2.lemma == null){
                    return 0;
                }else if(w1.lemma == null){
                    return 1;
                }else if(w2.lemma == null){
                    return -1;
                }else if((c = w1.lemma.compareTo(w2.lemma)) != 0){
                    return c;
                }else if(w1.pos == null && w2.pos == null){
                    return 0;
                }else if(w1.pos == null){
                    return 1;
                }else if(w2.pos == null){
                    return -1;
                }else{
                    return w1.pos.compareTo(w2.pos);
                }

            }else{
                return -1;
            }

        }
    });

	
	public static void increaseTotalWordCountBy(int n){
		totalWordCount += n;
	}
	
	public static long getTotalWordCount(){
		return totalWordCount;
	}
	
	public static void setTotalWordCount(long l){
		totalWordCount = l;
	}
	
	public static double getLogTotalWordCount(){
		if(Double.isNaN(logTotalWordCount)){
			logTotalWordCount = Math.log(totalWordCount);
		}
		
		return logTotalWordCount;
	}

	
	public static Collection<Word> asList(){
		return words.values();
	}
	
	public static int getSize(){
		return words.size();
	}
    
	private static Word getIndexedWord(Word possiblyUnindexedWord){
		//if word is not yet in vocabulary, add it
		Word indexedWord = words.get(possiblyUnindexedWord);
		if(indexedWord == null){
            //this is the only way a new word is ever added to this vocabulary
			int vocabularyIndex = words.size();
			indexedWord = new Word(vocabularyIndex, possiblyUnindexedWord.token, possiblyUnindexedWord.lemma, possiblyUnindexedWord.pos);
			words.put(indexedWord, indexedWord);
            
            //create chain between words ordered by vocabulary index
            if(firstWordAdded == null){
                firstWordAdded = indexedWord;
                indexedWord.isFirstWordInVocabulary = true;
            }
            if(lastWordAdded != null){
                lastWordAdded.nextWordInVocabulary = indexedWord;
                lastWordAdded.isLastWordAddedInVocabulary = false;
            }
            lastWordAdded = indexedWord;
            indexedWord.isLastWordAddedInVocabulary = true;
		}
		
		return indexedWord;
	}
	
	public static Word getWord(String token, String lemma, String pos){
		Word unindexedWord = new Word(token, lemma, pos);
		return getIndexedWord(unindexedWord);
	}
	
	public static boolean hasWord(Word word){
		return words.containsKey(word);
	}
	
	//word string must contain no spaces or wild cards
	//assumes that word string is in the format denoted by wordForm
	public static Word getWord(String wordString){
        //if(this.wordForm != wordForm) return null;
        
        Word unindexedWord = new Word();

        if(wordForm == Word.TOKEN){
            unindexedWord.token = wordString;
        }else if(wordForm == Word.LEMMA){
            unindexedWord.lemma = wordString;
        }else if(wordForm == Word.TOKEN_AND_POS){
            String[] tokenAndPos = wordString.split("#");
            if(tokenAndPos.length == 2){
                unindexedWord.token = tokenAndPos[0];
                unindexedWord.pos = tokenAndPos[1];
            }else{
                //unindexedWord.token = wordString; //if split into token and pos doesn't work, treat as token only
                return null;
            }
        }else if(wordForm == Word.LEMMA_AND_POS){
            String[] lemmaAndPos = wordString.split("#");
            if(lemmaAndPos.length == 2){
                unindexedWord.lemma = lemmaAndPos[0];
                unindexedWord.pos = lemmaAndPos[1];
            }else{
                //unindexedWord.lemma = wordString; //if split into lemma and pos doesn't work, treat as lemma only
                return null;
            }
        }

        return getIndexedWord(unindexedWord);
    }
    
    /*public ArrayList<Word> getWords(ArrayList<String> wordStrings, int wordForm){
        ArrayList<Word> wordList = new ArrayList<>();
        for(String wordString : wordStrings){
            wordList.add(getWord(wordString, wordForm));
        }
        
        return wordList;
    }
    
    public ArrayList<Word> getWords(File wordStringsFile, int wordForm){
        return getWords(Helper.importWordsAsList(wordStringsFile), wordForm);
    }
    */
    
    public static boolean isEmpty(){
        return words.isEmpty();
    }
	
	private static void add(String wordString){
		getWord(wordString);
	}
    
    private static void add(ArrayList<String> wordStrings){
        for(String wordString : wordStrings){
            add(wordString);
        }
    }
    
    private static void add(File wordStringsFile) throws IOException{
        add(Helper.importWordsAsList(wordStringsFile));
    }
    
    public static void addWordWithIndex(Word w){
        if(isEmpty()){
            firstWordAdded = w;
            w.isFirstWordInVocabulary = true;
            lastWordAdded = w;
            w.isLastWordAddedInVocabulary = true;

        }else{
            lastWordAdded.nextWordInVocabulary = w;
            lastWordAdded.isLastWordAddedInVocabulary = false;
            w = lastWordAdded;
            w.isLastWordAddedInVocabulary = true;
        }
        
        words.put(w, w);
    }
    
    public static void setWordForm(int wf){
        wordForm = wf;
    }
	
	public static void importFrom(BufferedReader in) throws IOException{
		//String line = in.readLine();
        //int wf = -1;
        //String[] entries;
        //if(line == null || ((entries = line.split("\t")).length != 2) || !entries[0].equals("word form") || (wf = Integer.parseInt(entries[1])) < 0 || wf >= Word.WORD_FORMS.length){
            //return null;
        //}
        //vocabulary = Vocabulary.create(wordForm);
        
        Description d = Description.importFrom(in);
        if(d.getTypeAttribute().equals("vocabulary")){
            String wordFormAsString = d.getParameterValue("word form");
            for(int i=0; i<Word.WORD_FORMS.length; i++){
                if(Word.WORD_FORMS[i].equals(wordFormAsString)){
                    wordForm = i;
                }
            }
            
            totalWordCount = Long.parseLong(d.getParameterValue("total word count"));
            logTotalWordCount = Double.parseDouble(d.getParameterValue("log total word count"));
        }
        System.out.println("[Vocabulary] importing with header:\n" + d.asString(null)); //DEBUG
        System.out.println("[Vocabulary] word form: " + Word.WORD_FORMS[wordForm] + ", total word count: " + totalWordCount + ", log twc: " + logTotalWordCount); //DEBUG
        
        Word w;
        while((w = Word.importFrom(in)) != null){
            words.put(w, w);
            if(firstWordAdded == null){
				w.isFirstWordInVocabulary = true;
				firstWordAdded = w;
			}
            if(lastWordAdded != null){
				w.isLastWordAddedInVocabulary = true;
				lastWordAdded.nextWordInVocabulary = w;
			}
            lastWordAdded = w;
		}
	}
    
    public static void importFrom(File file) throws IOException{
		System.out.println("[Vocabulary] Importing vocabulary from " + file.getAbsolutePath() + "...");

		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			importFrom(in);
		}

		System.out.println("[Vocabulary] ...Finished importing vocabulary from " + file.getAbsolutePath());
    }
    
    private static boolean exportWord(BufferedWriter writer, Word word) throws IOException{
        writer.write("" + word.vocabularyIndex);
        if(wordForm == Word.TOKEN || wordForm == Word.TOKEN_AND_POS) writer.write("\t" + word.token);
        if(wordForm == Word.LEMMA || wordForm == Word.LEMMA_AND_POS) writer.write("\t" + word.lemma);
        if(wordForm == Word.TOKEN_AND_POS || wordForm == Word.LEMMA_AND_POS) writer.write("\t" + word.pos);
        writer.write("\t" + word.targetElementCount + "\t" + word.logTargetElementCount);
        
        return true;
    }
	
	public static void exportTo(BufferedWriter writer, boolean inAlphabeticOrder) throws IOException{
        System.out.println("[Vocabulary] Exporting vocabulary (size: " + getSize() + ") to file...");
        
        writer.write(getDescription().asString(null) + "\n");
        
		if(inAlphabeticOrder){
            for(Word word : asList()){
                exportWord(writer, word);
                writer.write("\n");
            }
        }else{
			for(Word word : asIterable()){
				exportWord(writer, word);
				writer.write("\n");
			}
        }
        
        System.out.println("[Vocabulary] ...Finished exporting vocabulary (size: " + getSize() + ") to file");
	}
    
    
    public static /*ExportToFileJob*/ void exportTo(File file, boolean inAlphabeticOrder) throws IOException{
        BufferedWriter out = Helper.getFileWriter(file);
        exportTo(out, inAlphabeticOrder);
        out.close();
    }
	
    //@Override
    public static String toString(boolean inAlphabeticOrder){
        String s = "vocabulary (size: " + getSize() + "):\n";

        if(inAlphabeticOrder){
            for(Word w : words.values()) s += w.asString(null) + "\n";
        }else{
			for(Word word : asIterable()){
                s += word.asString(null) + "\n";
            }
        }
        
        return s;
    }
    
    public static VocabularyIterable asIterable(){
        return new VocabularyIterable();
    }

	//@Override
	/*public static Iterator<Word> getIterator() {
		return new Iterator<Word>() {

			boolean fresh = true;
            Word w = null;
			
			@Override
			public boolean hasNext() {
				return (fresh && firstWordAdded != null) || (w != null && w.nextWordInVocabulary != null);
			}

			@Override
			public Word next() {
				if(fresh){
					w = firstWordAdded;
					fresh = false;
				}else{
					w = w.nextWordInVocabulary;
				}
                return w;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
	}
	*/
    
    //export all vectors under given model for words in given word list
    /*private static void exportWordMeaningRepresentations(BufferedWriter out, Label label, Collection<Word> wordList) throws IOException{
		Helper.report("[Vocabulary] Exporting word meaning representations under label " + label.toString() + "...");
		
        if(wordList == null) wordList = words.values();
        
        //header is model description
        //label.exportTo(out);
        
        //wrote one meaning representation per word in wordlist
        for(Word word : wordList){
            out.write(word.toString() + "\n");
            word.bagOfMeaningRepresentations.get(label).exportTo(out);
        }
        
        Helper.report("[Vocabulary] ...Finished exporting word meaning representations (size: " + wordList.size() + ") under label " + label.toString());
    }
    
    public static void exportWordMeaningRepresentations(File file, Label label, ArrayList<Word> wordList) throws IOException{
        BufferedWriter out = Helper.getFileWriter(file);
        exportWordMeaningRepresentations(out, label, wordList);
        out.close();
    }
    
    public static void exportWordMeaningRepresentations(File file, Label label) throws IOException{
        exportWordMeaningRepresentations(file, label, null);
    }
    */

    //@Override
    public static Description getDescription(){
        Description d = new Description();
		d.setTypeAttribute("vocabulary");
        d.addParameter("size", "" + getSize());
        d.addParameter("word form", Word.WORD_FORMS[wordForm]);
        d.addParameter("total word count", "" + totalWordCount);
        d.addParameter("log total word count", "" + logTotalWordCount);
        
        return d;
    }
    
    public static void importWordMeaningRepresentations(BufferedReader in, Label label, Collection<Word> wordList) throws IOException{
        System.out.println("[Vocabulary] (" + label.asString(null) + ") Importing word meaning representations...");
		
        //if(wordList == null) wordList = words.values();
        
        
        
        
        System.out.println("[Vocabulary] (" + label.asString(null) + ") ...Finished importing word meaning representations");
    }
    
    public static void importWordMeaningRepresentations(File file, Label label, ArrayList<Word> wordList) throws IOException{
        BufferedReader in = new BufferedReader(new FileReader(file));
        importWordMeaningRepresentations(in, label, wordList);
        in.close();
    }
    
    public static void importWordMeaningRepresentations(File file, Label label) throws IOException{
        importWordMeaningRepresentations(file, label, null);
    }
    
}