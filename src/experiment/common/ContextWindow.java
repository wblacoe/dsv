package experiment.common;

import integerset.interval.DoubleUnlimitedInterval;
import integerset.interval.IntegerInterval;
import integerset.interval.LimitedInterval;
import java.io.BufferedWriter;
import java.io.IOException;
import meta.Describable;
import meta.Exportable;
import meta.Printable;

public class ContextWindow implements Comparable, Describable, Exportable, Printable{
    
    public static final int DOCUMENT_INTERNAL_SENTENCE_WINDOW = 0;
    public static final int TARGET_SENTENCE_INTERNAL_WINDOW = 1;
    public static final String[] CONTEXT_WINDOWS = new String[]{ "document internal sentence window", "target sentence internal window" };
    
    public IntegerInterval documentInternalSentenceWindow, targetSentenceInternalWindow;
    public int contextWindowIndex;
    
    public ContextWindow(){
        contextWindowIndex = -1;
    }
    
    //cover context words in target sentence relative to target word according to sentence internal interval
    //and cover all context words in other sentences than target sentences according to sentences interval
    public ContextWindow(IntegerInterval documentInternalSentenceWindow, IntegerInterval targetSentenceInternalWindow){
        this.documentInternalSentenceWindow = documentInternalSentenceWindow;
        this.targetSentenceInternalWindow = targetSentenceInternalWindow;
    }
    
    //0: cover all context words in all sentences referenced by given interval
    //1: cover all context words relative to target word according to given interval. do this only in target sentence.
    public ContextWindow(IntegerInterval interval, int contextWindowIntervalType){
        switch(contextWindowIntervalType){
            case DOCUMENT_INTERNAL_SENTENCE_WINDOW:
                documentInternalSentenceWindow = interval;
                targetSentenceInternalWindow = new DoubleUnlimitedInterval();
                break;
            case TARGET_SENTENCE_INTERNAL_WINDOW:
                documentInternalSentenceWindow = new LimitedInterval(0, 0); //only this sentence
                targetSentenceInternalWindow = interval;
                break;
        }
    }

    
    public static ContextWindow create(){
        ContextWindow cw = new ContextWindow();
        if(Parameters.hasParameter("sentences interval")){
            cw.documentInternalSentenceWindow = IntegerInterval.importFromString(Parameters.getStringParameter("sentences interval"));
        }
        if(Parameters.hasParameter("sentence internal interval")){
            cw.targetSentenceInternalWindow = IntegerInterval.importFromString(Parameters.getStringParameter("sentence internal interval"));
        }
        
        return cw;
    }
    
    public static ContextWindow importFromString(String s){
        String[] entries = s.split("\t");
        IntegerInterval in1 = IntegerInterval.importFromString(entries[0]); //document internal sentence window
        IntegerInterval in2 = IntegerInterval.importFromString(entries[1]); //target sentence internal window
        
        return new ContextWindow(in1, in2);
    }
    
    @Override
    public int compareTo(Object o) {
        ContextWindow cw = (ContextWindow) o;
        int c;
        if(documentInternalSentenceWindow != null && cw.documentInternalSentenceWindow == null) return -1;
        if(documentInternalSentenceWindow == null && cw.documentInternalSentenceWindow != null) return 1;
        if(documentInternalSentenceWindow != null && cw.documentInternalSentenceWindow != null && (c = documentInternalSentenceWindow.compareTo(cw.documentInternalSentenceWindow)) != 0) return c;
        if(targetSentenceInternalWindow != null && cw.targetSentenceInternalWindow == null) return -1;
        if(targetSentenceInternalWindow == null && cw.targetSentenceInternalWindow != null) return 1;
        if(targetSentenceInternalWindow != null && cw.targetSentenceInternalWindow != null && (c = targetSentenceInternalWindow.compareTo(cw.targetSentenceInternalWindow)) != 0) return c;
        
        return 0;
    }

    @Override
    public Description getDescription(){
        Description d = new Description();
        d.setTypeAttribute("context window");
        d.addParameter("context window index", "" + contextWindowIndex);
        d.addParameter("document internal sentence window", documentInternalSentenceWindow.getDescription());
        d.addParameter("target sentence internal window", targetSentenceInternalWindow.getDescription());
        
        return d;
    }
    
    public static ContextWindow create(Description d){
        ContextWindow cw = null;
        
        if(d.getTypeAttribute().equals("context window")){
            String indexString = d.getParameterValue("context window index");
            Description in1D = d.getParameterObjectDescription("document internal sentence window");
            Description in2D = d.getParameterObjectDescription("target sentence internal window");
            if(indexString != null && in1D != null && in2D != null){
                int index = Integer.parseInt(indexString);
                IntegerInterval in1 = IntegerInterval.create(in1D);
                IntegerInterval in2 = IntegerInterval.create(in2D);
                cw = new ContextWindow(in1, in2);
                cw.contextWindowIndex = index;
            }
        }
        
        return cw;
    }

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException{
        documentInternalSentenceWindow.exportTo(writer);
        writer.write("\t");
        targetSentenceInternalWindow.exportTo(writer);
        return true;
    }
    
    @Override
    public String asString(Object o){
        return "sentences: " + documentInternalSentenceWindow.asString(o) + ", words: " + targetSentenceInternalWindow.asString(o);
    }
    
    @Override
    public String toString(){
        return asString(null);
    }
    
}