package vocabulary;

import java.util.Iterator;
import lingunit.flattext.Word;
import static vocabulary.Vocabulary.firstWordAdded;

public class VocabularyIterable implements Iterable<Word>{

    @Override
    public Iterator<Word> iterator() {
        return new Iterator<Word>() {

			boolean fresh = true;
            Word w = null;
			
			@Override
			public boolean hasNext() {
				return (fresh && firstWordAdded != null) || (w != null && w.nextWordInVocabulary != null);
			}

			@Override
			public Word next() {
				if(fresh){
					w = firstWordAdded;
					fresh = false;
				}else{
					w = w.nextWordInVocabulary;
				}
                return w;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
    }
    
}
