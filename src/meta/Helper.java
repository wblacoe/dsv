package meta;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class Helper {
    
    //log print
	public static String getTimeString(){
		SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
		return f.format(Calendar.getInstance().getTime());
	}
	
    public static boolean developer = false;
	public synchronized static void report1(String s){
        if(developer) System.out.println(/*getTimeString() + " " +*/ s);
	}
    
    
    //I/O
    public synchronized static BufferedReader getFileReader(File file) throws IOException{
        return
            file.getName().endsWith(".gz") ?
            new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)))) :
            new BufferedReader(new FileReader(file));
    }
    public synchronized static BufferedReader getFileReader(String fileName) throws IOException{
        return getFileReader(new File(fileName));
    }
    
    public synchronized static BufferedWriter getFileWriter(File file) throws IOException{
        ensureContainingFolderExists(file);
        return
            file.getName().endsWith(".gz") ?
            new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), "UTF-8")) :
            new BufferedWriter(new FileWriter(file));
    }
    public synchronized static BufferedWriter getFileWriter(String fileName) throws IOException{
        return getFileWriter(new File(fileName));
    }
    
    public synchronized static void ensureFolderExists(File folder){
        if(!folder.exists()){
            folder.mkdirs();
        } 
    }

    public synchronized static void ensureContainingFolderExists(File file){
        if(file != null && file.getParentFile() != null && !file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        } 
    }
    
    public static ArrayList<String> importWordsAsList(File file) throws IOException{
        ArrayList<String> wordList = new ArrayList<>();
        
        BufferedReader in = getFileReader(file);

        String line;
        while((line = in.readLine()) != null){
            if(!line.isEmpty()){
                wordList.add(line.trim());
            }
        }

        in.close();
        
        return wordList;
    }
    
    public static String[] importWordsAsArray(File file) throws IOException{
        ArrayList<String> wordList = importWordsAsList(file);
        String[] wordArray = new String[wordList.size()];
        wordList.toArray(wordArray);
        
        return wordArray;
    }

    public static HashMap<String, Integer> importWordCounts(File file, String delimiter) throws IOException{
        HashMap<String, Integer> wordCounts = new HashMap<>();
        
        BufferedReader in = getFileReader(file);

        String line;
        while((line = in.readLine()) != null){
            if(!line.isEmpty()){
                String[] entries = line.split(delimiter);
                String word = entries[0];
                Integer count = Integer.parseInt(entries[1]);
                wordCounts.put(word, count);
            }
        }

        in.close();
        
        return wordCounts;
    }
    
    public static void exportWordsAsArray(File file, String[] words) throws IOException{
        BufferedWriter out = getFileWriter(file);

        for(String word : words){
            out.write(word + "\n");
        }

        out.close();
    }
    
    /*public static ArrayList<String> stopwords = new ArrayList<>();
    public static void prepareStopwords(File stopwordsFile){
        stopwords = importWordsAsList(stopwordsFile);
    }
    
    public static boolean isStopword(String word){
        return stopwords.contains(word);
    }
    */
 
    public static int getIndex(Object[] array, Object object){
        for(int i=0; i<array.length; i++){
            if(array[i] != null && array[i].equals(object)) return i;
        }
        
        return -1;
    }
    
}