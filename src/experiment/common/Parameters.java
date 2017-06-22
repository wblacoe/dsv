package experiment.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Parameters {

	private static final HashMap<String, Object> parameters = new HashMap<>();
	
	public static void setParameter(String key, Object value){
		parameters.put(key, value);
	}
	
	public static Object getParameter(String key){
		return parameters.get(key);
	}
	
	public static boolean hasParameter(String key){
		return getParameter(key) != null;
	}
		
	public static Boolean getBooleanParameter(String key){
		Object value = getParameter(key);
		if(value == null){
			return null;
		}else if(value instanceof String){
			Boolean parsedValue = Boolean.parseBoolean((String) value);
			setParameter(key, parsedValue);
			return parsedValue;
		}else{
			return (Boolean) value;
		}
	}
	
	public static Integer getIntParamter(String key){
		Object value = getParameter(key);
		if(value == null){
			return null;
		}else if(value instanceof String){
			Integer parsedValue = Integer.parseInt((String) value);
			setParameter(key, parsedValue);
			return parsedValue;
		}else{
			return (Integer) value;
		}
	}
	
	public static Float getFloatParamter(String key){
		Object value = getParameter(key);
		if(value == null){
			return null;
		}else if(value instanceof String){
			Float parsedValue = Float.parseFloat((String) value);
			setParameter(key, parsedValue);
			return parsedValue;
		}else{
			return (Float) value;
		}
	}
	
	public static Double getDoubleParamter(String key){
		Object value = getParameter(key);
		if(value == null){
			return null;
		}else if(value instanceof String){
			Double parsedValue = Double.parseDouble((String) value);
			setParameter(key, parsedValue);
			return parsedValue;
		}else{
			return (Double) value;
		}
	}
	
	public static String getStringParameter(String key){
		Object value = getParameter(key);
		if(value == null){
			return null;
		}else{
			return (String) value;
		}
	}
    
    public static File getFileParameter(String key){
        Object value = getParameter(key);
        if(value == null){
            return null;
        }else if(value instanceof String){
            File file = new File((String) value);
            setParameter(key, file);
            return file;
        }else{
            return (File) value;
        }
    }
    
    public static int getConstantParameter(String key, String[] strings){
        String stringValue = getStringParameter(key);
        int intValue = -1;
        
        for(int i=0; i<strings.length; i++){
            if(strings[i].equals(stringValue)) intValue = i;
        }
        
        return intValue;
    }
	
	public static void importFrom(File file) throws IOException{
        System.out.println("[Parameters] Importing parameters from " + file.getAbsolutePath() + "...");
        
		BufferedReader in = new BufferedReader(new FileReader(file));
		
		String line;
		while((line = in.readLine()) != null){
            line = line.split("#")[0]; //remove commented part from line
            line = line.trim();
            
            String[] entries;
			if(!line.isEmpty() && (entries = line.split(" *= *")).length == 2){
				setParameter(entries[0], entries[1]);
                System.out.println("[Parameters] Setting parameter \"" + entries[0] + "\" to \"" + entries[1] + "\"");
			}
		}
		
		in.close();
        
        System.out.println("[Parameters] ...Finished importing parameters from " + file.getAbsolutePath());
	}

}
