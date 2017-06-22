package models;

public class AbstractFunction {

    public static int getIndex(Object[] array, Object object){
        for(int i=0; i<array.length; i++){
            if(array[i] != null && array[i].equals(object)) return i;
        }
        
        return -1;
    }

}
