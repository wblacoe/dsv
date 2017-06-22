package pattern.dependency;

public class DepTreePatternTokeniser {

	private String s;
	private int i;
	
	public DepTreePatternTokeniser(String s){
		this.s = s;
		i = 0;
	}
	
	public boolean hasNext(){
		return i < s.length();
	}
	
	public String next() {
		char c;
		if(!hasNext()) return null;
		c = s.charAt(i);
        switch (c) {
            case '[':
                i++;
                return "[";
            case ']':
                i++;
                return "]";
            case ' ':
                i++;
                return next();
            default:
                StringBuilder tokenBuffer = new StringBuilder();
                while(true){
                    if(!hasNext()) return tokenBuffer.toString();
                    c = s.charAt(i);
                    if(c == '[' || c == ']' || c == ' '){
                        return tokenBuffer.toString();
                    }else{
                        i++;
                        tokenBuffer.append(c);
                    }
                }
        }
	}
	
}
