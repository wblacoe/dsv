package vector.complex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vector.AbstractVector;
import experiment.common.Description;

public class VectorTree extends MeaningRepresentation {
    
    protected static final Pattern vectorTreePattern = Pattern.compile("<vectortree bracketstring=\"([^\"]*)\">");
    
    public VectorNode root;
    private Description contentDescription;
    
    public VectorTree(VectorNode root){
        this.root = root;
		contentDescription = new Description();
    }
    
    
    //is vector node index necessary?
    private static VectorNode createFromBracketString(String bracketString, int position, ArrayList<VectorNode> vectorNodes){
        StringBuilder sb = new StringBuilder();
        VectorNode node = new VectorNode();
        
        while(position < bracketString.length()){
            char c = bracketString.charAt(position);
            if(c == '['){
                VectorNode childNode = createFromBracketString(bracketString, position, vectorNodes);
                node.childNodes.add(childNode);
                childNode.parentNode = node;
            }else if(c == ']'){
                node.index = Integer.parseInt(sb.toString());
                break;
            }else{
                sb.append(c);
                position++;
            }
        }
        
        return node;
    }

    public static VectorTree importFrom(BufferedReader in) throws IOException{
        VectorTree tree = null;
        
        String line = in.readLine(); //line starting with "<vectortree"
        Matcher matcher = vectorTreePattern.matcher(line);
        if(matcher.find()){
            String bracketString = matcher.group(1);
            if(bracketString.startsWith("[")){
                ArrayList<VectorNode> vectorNodes = new ArrayList<>();
                while((line = in.readLine()) != null){
                    if(line.equals("</vectortree>")){
                        break;
                    }else{
                        //AbstractVector vector = AbstractVector.importFrom(in);
                        AbstractVector vector = AbstractVector.create(null); //TODO
                        VectorNode vectorNode = new VectorNode();
                        vectorNode.vector = vector;
                        vectorNodes.add(vectorNode);
                    }
                }

                VectorNode vectorRootNode = createFromBracketString(bracketString, 1, vectorNodes);
                tree = new VectorTree(vectorRootNode);
            }
        }
        
        return tree;
    }

    //is vector node index necessary?
    public String toBracketString(VectorNode node, ArrayList<AbstractVector> vectors, String bracketString){
        int nodeIndex = vectors.size();
        String s = "[" + nodeIndex;
        vectors.add(node.vector);
        for(VectorNode childNode : node.childNodes){
            s += toBracketString(childNode, vectors, bracketString);
        }
        
        return s + "]";
    }
    
    
    //0: amount of nodes, 1: amount of nodes with non-null vector
    private int[] getStats(){
        int amountOfNodes = 0;
        int amountOfNonEmptyNodes = 0;
        
        Stack<VectorNode> stack = new Stack<>();
        stack.push(root);
        
        while(!stack.isEmpty()){
            VectorNode node = stack.pop();
            
            amountOfNodes++;
            if(node.vector != null) amountOfNonEmptyNodes++;
            
            for(VectorNode childNode : node.childNodes){
                stack.push(childNode);
            }
        }
        
        return new int[]{ amountOfNodes, amountOfNonEmptyNodes };
    }
	
	public Description getContentDescription(){
		return contentDescription;
	}
	
	//once the tree has been filled with vector nodes, the content description should be updated (from a very generic description) to the description of the contained vectors
	/*public Description updateDescription(){
		contentDescription = root.getContentDescription();
		return contentDescription;
	}
    */
    
    @Override
    public Description getDescription() {
        Description d = new Description();
        d.setAttribute("type", "tree of vectors");
        
        int[] stats = getStats();
        d.addParameter("size", "" + stats[0]);
        d.addParameter("amount of vectors", "" + stats[1]);
		d.addParameter("content", contentDescription);
        
        return d;
    }

    public static DistributionOfVectors create(Description d){
        //TODO
        return null;
    }
    
	@Override
	public ArrayList<AbstractVector> getContainedVectors() {
		ArrayList<AbstractVector> containedVectors = new ArrayList<>();
		
		Stack<VectorNode> stack = new Stack<>();
		if(root != null) stack.push(root);
		while(!stack.isEmpty()){
			VectorNode node = stack.pop();
			if(node.vector != null) containedVectors.add(node.vector);
			for(VectorNode childNode : node.childNodes){
				stack.push(childNode);
			}
		}
		
		return containedVectors;
	}
	
	public ArrayList<VectorNode> getNodes(){
		ArrayList<VectorNode> nodes = new ArrayList<>();
		
		Stack<VectorNode> stack = new Stack<>();
		if(root != null) stack.push(root);
		while(!stack.isEmpty()){
			VectorNode node = stack.pop();
			nodes.add(node);
			for(VectorNode childNode : node.childNodes){
				stack.push(childNode);
			}
		}
		
		return nodes;
	}

	@Override
	public VectorTree getCopy() {
		VectorTree vt;
		if(root == null){
			vt = new VectorTree(null);
		}else{
			HashMap<VectorNode, VectorNode> originalCopyMap = new HashMap<>();
			ArrayList<VectorNode> originalNodes = getNodes();
			for(VectorNode originalNode : originalNodes){
				VectorNode copy = new VectorNode();
				copy.vector = originalNode.vector.getCopy();
				copy.index = originalNode.index;
				originalCopyMap.put(originalNode, copy);
			}
			for(VectorNode originalNode : originalNodes){
				VectorNode copy = originalCopyMap.get(originalNode);
				copy.parentNode = originalCopyMap.get(originalNode.parentNode);
				for(VectorNode originalChildNode : originalNode.childNodes){
					copy.childNodes.add(originalCopyMap.get(originalChildNode));
				}
			}
			vt = new VectorTree(originalCopyMap.get(root));
		}
		vt.contentDescription = contentDescription.getCopy();
		
		return vt;
	}

	@Override
	public Float similarity(MeaningRepresentation mr) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

    @Override
    public Object exportTo(BufferedWriter writer) throws IOException {
        ArrayList<AbstractVector> vectors = new ArrayList<>();
        String bracketString = "";
        bracketString = toBracketString(root, vectors, bracketString);
        System.out.println(bracketString);

        //out.write("<vectortree bracketstring=\"" + bracketString + "\">\n");
        writer.write(bracketString + "\n");
        for(AbstractVector vector : vectors){
            if(vector == null){
                writer.write("\n");
            }else{
                vector.exportTo(writer);
            }
        }
        writer.write("\n");

        return true;
    }

    @Override
    public String asString(Object o) {
        return root.asString(o);
    }

}
