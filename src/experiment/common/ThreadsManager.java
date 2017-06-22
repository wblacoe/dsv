package experiment.common;

import experiment.AbstractExperiment;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import meta.Helper;

public class ThreadsManager implements Runnable{
    
    private BlockingQueue<Runnable> startSignalQueue, finishSignalQueue;
    private HashSet<Runnable> threads;
    private int maxAmountOfThreads;
    private boolean finishSignalReceived;
    
    public ThreadsManager(BlockingQueue<Runnable> startSignalQueue, BlockingQueue<Runnable> finishSignalQueue, int maxAmountOfThreads){
        this.startSignalQueue = startSignalQueue;
        this.finishSignalQueue = finishSignalQueue;
        threads = new HashSet<>();
        this.maxAmountOfThreads = maxAmountOfThreads;
        finishSignalReceived = false;
    }
    
    public synchronized void notifyThreadsManager(){
        notify();
    }
    
    @Override
    public synchronized void run() {
        //Helper.report("[ThreadsManager] Starting...");
		try{
			do{
                
                while(!finishSignalQueue.isEmpty() || (!finishSignalReceived && threads.size() < maxAmountOfThreads) || (threads.size() < maxAmountOfThreads && !startSignalQueue.isEmpty())){
                //while(!finishSignalQueue.isEmpty() || (threads.size() < maxAmountOfThreads && !startSignalQueue.isEmpty())){

                    /*PipelineSignal signal;
                    //finishing threads has priority over start new threads
                    if(!finishSignalQueue.isEmpty()){
                        signal = finishSignalQueue.take();
                    }else{
                        signal = startSignalQueue.take();
                    }
                    //Helper.report("[Threads] Retrieved from queue: " + signal.toString()); //DEBUG

                    //start thread inside signal object
                    if(signal instanceof StartSignal){
                        Runnable thread = ((StartSignal) signal).thread;
                        //if(thread == null){
                        threads.add(thread);
                        (new Thread(thread)).start();
                        //Helper.report("[Threads] Starting thread \"" + thread.toString() + "\", threads: " + threads.size());

                    //close thread inside signal object
                    }else if(signal instanceof FinishSignal){
                        Runnable thread = ((FinishSignal) signal).thread;
                        if(thread == null){
                            //Helper.report("[Threads] FINISH SIGNAL RECEIVED");
                            finishSignalReceived = true;
                        }else if(threads.remove(thread)){
                            //Helper.report("[Threads] Closing thread \"" + thread.toString() + "\"");
                        }
                    }
                    */
                    
                    if(!finishSignalQueue.isEmpty()){
                        Runnable thread = finishSignalQueue.take();
                        threads.remove(thread);
                    }else if(!startSignalQueue.isEmpty()){
                        Runnable thread = startSignalQueue.take();
                        if(thread == this){ //workaround for finishing threads manager
                            finishSignalReceived = true;
                            //Helper.report("[ThreadsManager] CLOSING DOWN...");
                        }else{
                            threads.add(thread);
                            (new Thread(thread)).start();
							//Helper.report("[ThreadsManager] Running thread " + thread + "...");
                        }
                    }else if(!threads.isEmpty()){
                        wait();
                    }

                    /*String q = "{";
                    for(Runnable t : threads){
                        q += t.getClass().getSimpleName() + ", ";
                    }
                    q += "}";
                    if(!threads.isEmpty()) Helper.report("[ThreadsManager] " + threads.size() + "/" + maxAmountOfThreads + " threads are active: " + q); //DEBUG
                    */
                    
                    //Helper.report("[ThreadsManager] finish: " + finishSignalReceived + ", threads: " + threads.size() + ", startQueue: " + startSignalQueue.size() + "; finishQueue: " + finishSignalQueue.size()); //DEBUG
                    
                    
                }
                
                if(!finishSignalReceived) wait();
                
			}while(!threads.isEmpty() || !startSignalQueue.isEmpty() || !finishSignalQueue.isEmpty() || !finishSignalReceived);
            
            System.out.println("[ThreadsManager] ...Finished");
            AbstractExperiment.signalThreadsManagerFinished();

		}catch(InterruptedException | IOException e){
            e.printStackTrace();
        }
    }

}