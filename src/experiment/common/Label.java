package experiment.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import meta.Copyable;
import meta.Describable;
import meta.ExportableAndComparable;
import meta.Printable;

public class Label implements ExportableAndComparable, Describable, Copyable, Printable{
    
    public static final Label MASTER_LABEL = new Label("*MASTER*");

	private ArrayList<Comparable> objects;
	
	public Label(){
		objects = new ArrayList<>();
	}
	public Label(ArrayList<Comparable> objects){
		this.objects = objects;
	}
	public Label(Comparable[] objects){
        this();
        this.objects.addAll(Arrays.asList(objects));
	}
    public Label(Comparable o){
        this();
        objects.add(o);
    }
	
	public void addObject(Comparable o){
		objects.add(o);
	}
	
	public int getSize(){
		return objects.size();
	}
	
	public Comparable getObject(int index){
		return objects.get(index);
	}

	@Override
	public int compareTo(Object o) {
		Label label = (Label) o;
        int c;
		if((c = Integer.compare(label.getSize(), getSize())) != 0) return c;
        for(int i=0; i<getSize(); i++){
            if(!getObject(i).getClass().equals(label.getObject(i).getClass())) return -1;
            if((c = getObject(i).compareTo(label.getObject(i))) != 0) return c;
        }
		
		return 0;
	}
    
    @Override
    public Description getDescription() {
        Description d = new Description();
        d.setTypeAttribute("label");
        for(Comparable o : objects){
            d.addParameter("object", ((Describable) o).getDescription());
        }
        
        return d;
    }
    
    public static Label create(Description d){
        Label label = new Label();
        for(Description parameterChild : d.getAllParameterObjectDescriptions()){
            label.addObject((Comparable) parameterChild.createObject());
        }
        
        return label;
    }

    @Override
    public Object getCopy() {
        Label copy = new Label();
        for(Comparable o : objects){
            copy.addObject(o);
        }
        
        return copy;
    }
    
    public boolean isEmpty(){
        return objects.isEmpty();
    }
    
    public Object remove(int index){
        return objects.remove(index);
    }
    
    public Object removeFinalObject(){
        if(isEmpty()){
            return null;
        }else{
            return remove(objects.size() - 1);
        }
    }

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException{
        writer.write(asString(null));
        return true;
    }

    @Override
    public String asString(Object o) {
        String s = "";
        for(int i=0; i<objects.size() - 1; i++){
            Comparable object = objects.get(i);
            if(object instanceof File){
                File file = (File) object;
                String fileName = file.getName();
                s += fileName + ", ";
            }else{
                s += ((Printable) object).asString(o) + ", ";
            }
        }
        
        return s + objects.get(objects.size() - 1);
    }
    
    @Override
    public String toString(){
        return asString(null);
    }
		
}
