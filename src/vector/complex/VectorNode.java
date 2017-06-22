package vector.complex;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import meta.Exportable;
import meta.Printable;
import vector.AbstractVector;

public class VectorNode implements Exportable, Printable{

    public AbstractVector vector;
    public VectorNode parentNode;
    public ArrayList<VectorNode> childNodes;
    public int index;
    
    public VectorNode(){
        vector = null;
        parentNode = null;
        childNodes = new ArrayList<>();
    }
    
    /*protected Description getContentDescription(){
        if(vector != null){
            return vector.getDescription();
        }else if(!childNodes.isEmpty()){
            for(VectorNode childNode : childNodes){
				Description childNodesVectorDescription = childNode.getContentDescription();
				if(childNodesVectorDescription != null) return childNodesVectorDescription;
            }
        }
        
        return null;
    }
	*/

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        writer.write(asString(null));
        return true;
    }

    @Override
    public String asString(Object o) {
        String indent = (String) o;
        String s = indent;
        if(vector == null){
            s += "null";
        }else{
            s += vector.asString(o);
        }
        s += "\n";

        for(VectorNode childNode : childNodes){
            s += childNode.asString(indent + "  ");
        }

        return s;
    }
   
}