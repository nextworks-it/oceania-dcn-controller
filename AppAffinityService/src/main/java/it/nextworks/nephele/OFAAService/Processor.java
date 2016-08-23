package it.nextworks.nephele.OFAAService;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Processor {
	
	@Value("${OpendaylightURL}")
	private String ODLURL;

	@Value("${OfflineEngineURL}")
	private String OEURL;
	
	private BlockingQueue<ProcessingTask> tasks = new LinkedBlockingQueue<>();
	private Object tasksLock = new Object();
	
	private Thread executor;
	private Object execLock = new Object();
	
	public Processor(){
	}
	
	public void addTask(Service serv, Object lock) throws InterruptedException{
		synchronized(tasksLock){
			tasks.put(new ProcessingTask(serv, lock, ODLURL, OEURL));
		}
		synchronized(execLock){
			if ( (executor == null) || ( !(executor.isAlive()) ) ) executeTasks();
		}
	}
	
	public void executeTasks(){
		synchronized(tasksLock){
			while( !(tasks.isEmpty()) ){
				executor = new Thread(tasks.remove());
			}
		}
		executor.run();
		/*
		 * NOTE: this means that the thread running executeTasks() is the one
		 *
		 * doing the computations. Hence, requests are resolved one after the
		 * other. By changing this to executor.start() a new thread would be fired
		 * for each task, with better performance. Right now, though, there are no
		 * concurrency checks inside ProcessingTask (and/or the other components),
		 * hence a safety check is necessary if this patch is to be applied.
		 */
	}

}
