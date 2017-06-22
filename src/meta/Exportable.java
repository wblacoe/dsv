package meta;

import java.io.BufferedWriter;
import java.io.IOException;

public interface Exportable {
    
	public Object exportTo(BufferedWriter writer) throws IOException;
    
}
