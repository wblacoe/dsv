package models.pointwise;

import experiment.AbstractExperiment;
import experiment.common.Description;
import java.io.IOException;
import lingunit.flattext.Word;
import experiment.common.Label;
import experiment.common.Parameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import models.AbstractModel;
import models.AbstractModelThread;
import static models.pointwise.AssociationFunction.LMI;
import static models.pointwise.AssociationFunction.NPMI;
import static models.pointwise.AssociationFunction.PMI;
import static models.pointwise.AssociationFunction.PPMI;
import static models.pointwise.AssociationFunction.PROB;
import static models.pointwise.AssociationFunction.RELATIVE_PROB;
import pipeline.signals.FinishSignal;
import pipeline.signals.NotifySignal;
import pipeline.signals.PipelineSignal;
import pipeline.signals.StartSignal;
import vector.AbstractVector;
import vector.FloatVector;
import vector.IntegerVector;
import vector.VectorEntry;
import vector.complex.BagOfMeaningRepresentations;
import vector.complex.MeaningRepresentation;
import vocabulary.Vocabulary;

public class AssociationFunction extends AbstractModel{
	
	public static final int PROB = 0;
    public static final int RELATIVE_PROB = 1;
    public static final int PMI = 2;
    public static final int PPMI = 3;
    public static final int LMI = 4;
	public static final int NPMI = 5;
	public static final String[] ASSOCIATION_FUNCTIONS = new String[]{ "probility", "relative probability", "pmi", "ppmi", "lmi", "npmi" };

	private final int associationFunction;
	
	private final TreeMap<Label, AssociationFunctionThread> threads; //one thread per context window
	protected final TreeMap<Label, BlockingQueue<PipelineSignal>> signalQueues;
	
	public AssociationFunction(int associationFunction) throws IOException{
		this.associationFunction = associationFunction;
        threads = new TreeMap<>();
		signalQueues = new TreeMap<>();
	}
    
    public static AssociationFunction create(int associationFunction) throws IOException{
        return new AssociationFunction(associationFunction);
    }
    
    public static AssociationFunction create() throws IOException{
        return create(Parameters.getConstantParameter("association function", ASSOCIATION_FUNCTIONS));
    }
    
    public static AssociationFunction create(String associationFunction) throws IOException{
        return create(getIndex(ASSOCIATION_FUNCTIONS, associationFunction));
    }
	
	@Override
    public synchronized void signalSuperior(AbstractModelThread thread, Label label, PipelineSignal signal) throws IOException{
        
        //flush every signal (after assotiationation) through to successor
        signalPipeline(label, signal); //label: context window, signal: bag of MRs or target word or count vector
        
        super.signalSuperior(thread, label, signal);
    }

	@Override
	public void signalModel(Label label, PipelineSignal signal) throws IOException{
        if(signal instanceof StartSignal){
            signalPipeline(label, signal); //flush start signal through to successor, all other signals get associationated before passed on to successor
        }
        
        if(signal instanceof BagOfMeaningRepresentations || signal instanceof Word || signal instanceof MeaningRepresentation){ //label: context window, signal: content description or target word or count vector
            signalThread(label, signal);
        }
        
        super.signalModel(label, signal);
	}
	
	@Override
	public String protocol() {
		return "INPUT:\n" +
            "label: master label, signal: start signal\n" +
			"for each context window:\n" +
			"  label: context window, signal: start signal\n" +
			"  label: context window, signal: empty bag of MRs with header\n" +
			"  for each target word:\n" +
            "    label: context window, signal: target word" +
			"    label: context window, signal: count vector\n" +
			"  label: context window, signal: finish signal\n\n" +


            "OUTPUT:\n" +
            "label: master label, signal: start signal\n" +
			"for each context window:\n" +
			"  label: context window, signal: start signal\n" +
			"  label: context window, signal: empty bag of MRs with header\n" +
			"  for each target word:\n" +
            "    label: context window, signal: target word\n" +
			"    label: context window, signal: associationated vector\n" +
			"  label: context window, signal: finish signal";
	}

    @Override
    protected AbstractModelThread createThread(Label threadLabel) {
        BlockingQueue<PipelineSignal> signalQueue = new LinkedBlockingDeque<>();
        return new AssociationFunctionThread(this, threadLabel, signalQueue, associationFunction);
    }

    @Override
    protected void startModel() {
        report("Ready...");
    }

    @Override
    protected void finishModel() {
        report("...Finished associationating meaning representations under " + amountOfFinishedThreads + " labels");
    }
   
}



class AssociationFunctionThread extends AbstractModelThread{
	
	private final int associationFunction;
	//private MeaningRepresentation contentPrototype;
	
    //label: context window
	public AssociationFunctionThread(AssociationFunction superior, Label threadLabel, BlockingQueue<PipelineSignal> signalQueue, int associationFunction){
        super(superior, threadLabel, signalQueue);
        
		this.associationFunction = associationFunction;
	}

	private double pmi(double logJointCount, double logTargetElementCount, double logContextElementCount, double logTotalWordCount){
		double pmi = logJointCount + logTotalWordCount - logTargetElementCount - logContextElementCount;
		//Helper.report("j: " + logJointCount + ", w: " + logTargetElementCount + ", c: " + logContextElementCount + ", t: " + logTotalWordCount + ", pmi: " + pmi);
		return pmi;
	}
    
	private void applyAssociationFunctionReplaceVectorEntries(Word targetWord, AbstractVector inputVector){
		
		//apply association function
		switch(associationFunction){
		case PROB: //assumes that vector is non-zero and that all entries are positive
			float manhattanNorm = inputVector.norm(1);
			for(VectorEntry ve : inputVector){
				inputVector.set(ve.getDimension(), ve.getFloatValue() / manhattanNorm);
			}
			break;
			
		case RELATIVE_PROB:
			for(VectorEntry ve : inputVector){
				double relativeProb = (1.0 * ve.getDoubleValue() / targetWord.getTargetElementCount()) * (1.0 * Vocabulary.getTotalWordCount() / AbstractExperiment.getContextElement(ve.getDimension()).getContextElementCount());
				inputVector.set(ve.getDimension(), (float) relativeProb);
			}
			break;
			
        case PMI:
            HashMap<Integer, Float> changes = new HashMap<>();
			for(VectorEntry ve : inputVector){
				double assocValue = pmi(Math.log(ve.getDoubleValue()), targetWord.getLogTargetElementCount(), AbstractExperiment.getContextElement(ve.getDimension()).getLogContextElementCount(), Vocabulary.getLogTotalWordCount());
				//inputVector.set(ve.getDimension(), (float) assocValue);
                changes.put(ve.getDimension(), (float) assocValue);
			}
            for(Entry<Integer, Float> entry : changes.entrySet()){
                inputVector.set(entry.getKey(), entry.getValue());
            }
			break;
			
		case PPMI:
			ArrayList<Integer> entriesToBeRemoved = new ArrayList<>();
			for(VectorEntry ve : inputVector){
				Float ppmiShift = Parameters.getFloatParamter("ppmi shift");
				if(ppmiShift == null) ppmiShift = 0f;
				float assocValue = ppmiShift + (float) pmi(Math.log(ve.getDoubleValue()), targetWord.getLogTargetElementCount(), AbstractExperiment.getContextElement(ve.getDimension()).getLogContextElementCount(), Vocabulary.getLogTotalWordCount());
				if(assocValue > 0){
					inputVector.set(ve.getDimension(), assocValue);
				}else{
					entriesToBeRemoved.add(ve.getDimension());
				}
			}
			for(int dimension : entriesToBeRemoved){
				inputVector.set(dimension, 0);
			}
			break;
			
		case LMI:
			for(VectorEntry ve : inputVector){
				double jointProb = 1.0 * ve.getDoubleValue() / Vocabulary.getTotalWordCount();
				double relativeProb = (1.0 * ve.getDoubleValue() / targetWord.getTargetElementCount()) * (1.0 * Vocabulary.getTotalWordCount() / AbstractExperiment.getContextElement(ve.getDimension()).getContextElementCount());
				double assocValue = jointProb * Math.log(relativeProb);
				inputVector.set(ve.getDimension(), (float) assocValue);
			}
			break;
			
		case NPMI:
			for(VectorEntry ve : inputVector){
				double jointProb = 1.0 * ve.getDoubleValue() / Vocabulary.getTotalWordCount();
				double relativeProb = (1.0 * ve.getDoubleValue() / targetWord.getTargetElementCount()) * (1.0 * Vocabulary.getTotalWordCount() / AbstractExperiment.getContextElement(ve.getDimension()).getContextElementCount());
				double assocValue = -Math.log(relativeProb) / Math.log(jointProb);
				inputVector.set(ve.getDimension(), (float) assocValue);
			}
			break;
		}

    }
	
    private AbstractVector applyAssociationFunctionCreateNewVector(Word targetWord, AbstractVector inputVector){
		
        AbstractVector resultVector = FloatVector.create(inputVector.isDense(), AbstractExperiment.getDimensionality());
        
		//apply association function
		switch(associationFunction){
		case PROB:
			resultVector = inputVector.normalise(1);
			break;
			
		case RELATIVE_PROB:
			for(VectorEntry ve : inputVector){
				double relativeProb = (1.0 * ve.getDoubleValue() / targetWord.getTargetElementCount()) * (1.0 * Vocabulary.getTotalWordCount() / AbstractExperiment.getContextElement(ve.getDimension()).getContextElementCount());
				resultVector.set(ve.getDimension(), (float) relativeProb);
			}
			break;
			
        case PMI:
			for(VectorEntry ve : inputVector){
				double assocValue = pmi(Math.log(ve.getDoubleValue()), targetWord.getLogTargetElementCount(), AbstractExperiment.getContextElement(ve.getDimension()).getLogContextElementCount(), Vocabulary.getLogTotalWordCount());
				resultVector.set(ve.getDimension(), (float) assocValue);
			}
			break;
			
		case PPMI:
			for(VectorEntry ve : inputVector){
				Float ppmiShift = Parameters.getFloatParamter("ppmi shift");
				if(ppmiShift == null) ppmiShift = 0f;
				float assocValue = ppmiShift + (float) pmi(Math.log(ve.getDoubleValue()), targetWord.getLogTargetElementCount(), AbstractExperiment.getContextElement(ve.getDimension()).getLogContextElementCount(), Vocabulary.getLogTotalWordCount());
				if(assocValue > 0) resultVector.set(ve.getDimension(), assocValue);
			}
			break;
			
		case LMI:
			for(VectorEntry ve : inputVector){
				double jointProb = 1.0 * ve.getDoubleValue() / Vocabulary.getTotalWordCount();
				double relativeProb = (1.0 * ve.getDoubleValue() / targetWord.getTargetElementCount()) * (1.0 * Vocabulary.getTotalWordCount() / AbstractExperiment.getContextElement(ve.getDimension()).getContextElementCount());
				double assocValue = jointProb * Math.log(relativeProb);
				resultVector.set(ve.getDimension(), (float) assocValue);
			}
			break;
			
		case NPMI:
			for(VectorEntry ve : inputVector){
				double jointProb = 1.0 * ve.getDoubleValue() / Vocabulary.getTotalWordCount();
				double relativeProb = (1.0 * ve.getDoubleValue() / targetWord.getTargetElementCount()) * (1.0 * Vocabulary.getTotalWordCount() / AbstractExperiment.getContextElement(ve.getDimension()).getContextElementCount());
				double assocValue = -Math.log(relativeProb) / Math.log(jointProb);
				resultVector.set(ve.getDimension(), (float) assocValue);
			}
			break;
			
		default:
			resultVector = null;
		}

		return resultVector;
    }
	
	private AbstractVector applyAssociationFunction(Word targetWord, AbstractVector inputVector, boolean replaceVectorEntries){
		if(replaceVectorEntries){
			applyAssociationFunctionReplaceVectorEntries(targetWord, inputVector);
			return null;
		}else{
			return applyAssociationFunctionCreateNewVector(targetWord, inputVector);
		}
	}
    
    private MeaningRepresentation applyAssociationFunction(Word targetWord, MeaningRepresentation inputMR){
		//Helper.report("[AssociationFunction] Applying association function " + ASSOCIATION_FUNCTIONS[associationFunction] + " to target words...");
		
        //copy the input MR. this retains the MR's interal structure among all contained vectors
        MeaningRepresentation copyMR;
        if(inputMR instanceof IntegerVector){
            copyMR = ((IntegerVector) inputMR).times(1f);
        }else{
            copyMR = (MeaningRepresentation) inputMR.getCopy();
        }

        //go through all vectors contained in copy MR and replace them by their associationated versions
        for(AbstractVector copyVector : copyMR.getContainedVectors()){
            //if(copyVector instanceof IntegerVector) copyVector = copyVector.times(1f);
            applyAssociationFunction(targetWord, copyVector, true);
        }
		
		//Helper.report("[AssociationFunction] ...Finished applying association function to target words");
        return copyMR;
    }

	@Override
	public void run() {
		report(AssociationFunction.ASSOCIATION_FUNCTIONS[associationFunction], "Associationating meaning representations...");
		int amountOfAssociationations = 0;
		Word targetWord = null;
		try{
			while(true){ //every signal goes through, some in original form, some in modified form
				PipelineSignal signal = signalQueue.take();
				
				if(signal instanceof BagOfMeaningRepresentations){
					//update content field to "float"
					BagOfMeaningRepresentations bag = (BagOfMeaningRepresentations) signal;
					Label bagLabel = bag.getLabel();
					Description contentDescription = bag.getContentDescription().getCopy();
					Description fieldParameter = contentDescription.getParameterObjectDescription("field");
					fieldParameter.setAttribute("value", "float");
					BagOfMeaningRepresentations bagCopy = new BagOfMeaningRepresentations(bagLabel, contentDescription);
					signalSuperior(threadLabel, bagCopy);
					
				}else if(signal instanceof Word){ //target word and meaning repr must be sent separately
					targetWord = (Word) signal; //therefore, cache the target word for now
                    superior.signalSuperior(this, threadLabel, signal);
					
				}else if(signal instanceof MeaningRepresentation){
					MeaningRepresentation mr = (MeaningRepresentation) signal;
					MeaningRepresentation assocMr = applyAssociationFunction(targetWord, mr);
                    amountOfAssociationations++;
                    superior.signalSuperior(this, threadLabel, assocMr);
					superior.signalSuperior(this, threadLabel, new NotifySignal());
					
				}else if(signal instanceof FinishSignal){
					break;
				}//else{
                    //superior.signalSuperior(this, threadLabel, signal); //e.g. content description
                //}
			}
            
            report(AssociationFunction.ASSOCIATION_FUNCTIONS[associationFunction], "...Finished associationating " + amountOfAssociationations + " meaning representations");
            finishThread();
		}catch(InterruptedException | IOException e){
			e.printStackTrace();
		}
	}

}
