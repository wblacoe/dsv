package experiment.common;

import integerset.interval.IntegerInterval;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import meta.Copyable;
import meta.Exportable;
import meta.Printable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import pipeline.signals.PipelineSignal;
import vector.AbstractVector;
import vector.complex.DistributionOfVectors;
import vector.complex.VectorTree;

public class Description implements Copyable, Exportable, PipelineSignal, Comparable, Printable{

    private String type;
    private HashMap<String, String> attributes;
    private ArrayList<Description> children;
    
    public Description(){
        type = "object";
        attributes = new HashMap<>();
        children = new ArrayList<>();
    }
	
	/*public Description(String type){
		this();
		setType(type);
	}*/
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return type;
	}
    
    public boolean hasAttribute(String attribute){
        return attributes.containsKey(attribute);
    }
	
	public void setAttribute(String attribute, String value){
		attributes.put(attribute, value);
	}
	
	public void setTypeAttribute(String value){
		setAttribute("type", value);
	}
	
	public String getAttribute(String attribute){
		return attributes.get(attribute);
	}
	
	public HashMap<String, String> getAttributes(){
		return attributes;
	}
    
    public String getTypeAttribute(){
        return getAttribute("type");
    }
	
	public boolean removeAttribute(String attribute){
		return attributes.remove(attribute) != null;
	}
    
    //returns the value of given parameter (e.g. if addParameterChild(...) was used to create it)
    /*public String getParameterAttribute(String param){
        for(Description child : children){
            if(child != null && child.getType().equals("param") && child.hasAttribute("name") && child.getAttribute("name").equals(param) && child.hasAttribute("value")){
                return child.getAttribute("value");
            }
        }
        
        return null;
    }
	
    //assumes that there is at most one child of type "param" with given parameter as attribute
    //returns the child of type "param" and given parameter as attribute (e.g. if addParameterGrandChild(...) was used to create it)
	public Description getParameterGrandChild(String param){
        for(Description child : children){
            if(child != null && child.getType().equals("param") && child.hasAttribute("name") && child.getAttribute("name").equals(param)){
                return child.getOnlyChild();
            }
        }
        
        return null;
    }
    
    public ArrayList<Description> getAllParameterGrandChildren(){
        ArrayList<Description> allParameterGrandChildren = new ArrayList<>();
        for(Description child : children){
            if(child != null && child.getType().equals("param") && child.hasAttribute("name") && child.getAmountOfChildren() == 1){
                //String param = child.getAttribute("name");
                Description parameterGrandChild = child.getOnlyChild();
                allParameterGrandChildren.add(parameterGrandChild);
            }
        }
        
        return allParameterGrandChildren;
    }
	*/
	
	public String getParameterValue(String param){
		String attribute;
		for(Description child : children){
			if(child.getType().equals("prim") && (attribute = child.getAttribute("param")) != null && attribute.equals(param)){
				return child.getAttribute("value");
			}
		}
		
		return null;
	}
	
	public Description getParameterObjectDescription(String param){
		String attribute;
		for(Description child : children){
			if((attribute = child.getAttribute("param")) != null && attribute.equals(param)){
				return child;
			}
		}
		
		return null;
	}
	
	//return type is ambiguous: if 
	public Object getParameter(String param){
		String parameterValue = getParameterValue(param);
		if(parameterValue != null) return parameterValue;
		return getParameterObjectDescription(param);
	}
	
	public ArrayList<Description> getChildren(){
		return children;
	}
	
	public ArrayList<Description> getAllParameterObjectDescriptions(){
        ArrayList<Description> list = new ArrayList<>();
        for(Description child : children){
            if(child.hasAttribute("param")){
                list.add(child);
            }
        }
        
        return list;
    }
    
    public Description getChild(String typeAttribute){
        for(Description child : children){
            for(Entry<String, String> attributeEntry : child.attributes.entrySet()){
                if(attributeEntry.getKey().equals("type") && attributeEntry.getValue().equals(typeAttribute)){
                    return child;
                }
            }
        }
        
        return null;
    }
    
    public Description getOnlyChild(){
        if(children.size() == 1){
            return children.get(0);
        }else{
            return null;
        }
    }
    
    public int getAmountOfChildren(){
        return children.size();
    }
    
    public void addChild(Description child){
		children.add(child);
	}
    
    public void addParameterValue(String param, String value){
		Description d = new Description();
        d.setType("prim");
		d.setAttribute("param", param);
		d.setAttribute("value", value);
        addChild(d);
    }
    
    /*public void addParameterGrandChild(String param, Description grandChild){
		Description child = new Description();
        child.setType("param");
        child.setAttribute("name", param);
		child.addChild(grandChild);
		addChild(child);
	}
	*/
	
	public void addParameterObjectDescription(String param, Description objectDescription){
		Description d = objectDescription;
		d.setAttribute("param", param);
		addChild(d);
	}
	
	public boolean addParameter(String param, Object o){
		if(o instanceof String){
			addParameterValue(param, (String) o);
		}else if(o instanceof Number){
			addParameterValue(param, ((Number) o).toString());
		}else if(o instanceof Description){
			addParameterObjectDescription(param, (Description) o);
		}else{
			return false;
		}
		
		return true;
	}
    
    @Override
    public Object exportTo(BufferedWriter writer) throws IOException{
        writer.write(asString(null));
        return true;
    }
    
    @Override
    public String asString(Object o){
        String indentation = o == null ? "" : (String) o;
        String s = indentation + "<" + type;
        for(Entry<String, String> attribute : attributes.entrySet()){
            s += " " + attribute.getKey() + "=\"" + attribute.getValue() + "\"";
        }
        if(children.isEmpty()){
            s += " />\n";
        }else{
            s += ">\n";
            for(Description child : children){
                s += child.asString(indentation + "  ");
            }
            s += indentation + "</" + type + ">\n";
        }
        
        return s;
    }
    
    private static Description fromJsoup(Element element){
        Description d = new Description();
        
        String tag = element.tagName();
        d.setType(tag);
        
        for(Attribute a : element.attributes()){
            d.setAttribute(a.getKey(), a.getValue());
        }
        
        for(Element e : element.children()){
            Description childD = fromJsoup(e);
            d.addChild(childD);
        }
        
        return d;
    }
    
    public static Description importFrom(BufferedReader in) throws IOException{
        String line, xmlString = "";
        while((line = in.readLine()) != null && line.trim().startsWith("<")){
            xmlString += line;
        } //make sure to leave an empty line after the xml block before importing further lines

        Document doc = Jsoup.parse(xmlString, "", Parser.xmlParser());
        Description d = fromJsoup(doc);
        
        return d.getOnlyChild();
    }
 
	@Override
	public Description getCopy() {
		Description d = new Description();
        d.setType(type);
		for(Entry<String, String> entry : attributes.entrySet()){
			d.setAttribute(entry.getKey(), entry.getValue());
		}
		for(Description child : children){
			d.addChild(child.getCopy());
		}
		
		return d;
	}
    
    public Object createObject(){
        Object o;
        switch(getTypeAttribute()){
            case "vector":
                o = AbstractVector.create(this);
                break;
            case "distribution of vectors":
                o = DistributionOfVectors.create(this);
                break;
            case "tree of vectors":
                o = VectorTree.create(this);
                break;
            case "label":
                o = Label.create(this);
                break;
            case "context window":
                o = ContextWindow.create(this);
                break;
            case "integer interval":
                o = IntegerInterval.create(this);
                break;
            default:
                o = null;
        }
        
        return o;
    }

    @Override
    public int compareTo(Object o) {
        Description d = (Description) o;
        int c;
        if((c = type.compareTo(d.type)) != 0) return c;
        if((c = Integer.compare(attributes.size(), d.attributes.size())) != 0) return c;
        for(Entry<String, String> attribute : attributes.entrySet()){
            String key = attribute.getKey();
            String value1 = attribute.getValue();
            String value2 = d.attributes.get(key);
            if(value2 == null) return 1;
            if((c = value1.compareTo(value2)) != 0) return c;
        }
        if((c = Integer.compare(getAmountOfChildren(), d.getAmountOfChildren())) != 0) return c;
        for(int i=0; i<getAmountOfChildren(); i++){
            Description child1 = children.get(i);
            Description child2 = d.children.get(i);
            if((c = child1.compareTo(child2)) != 0) return c;
        }
        
        return 0;
    }

}