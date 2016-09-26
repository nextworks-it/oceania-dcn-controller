package it.nextworks.nephele.OFAAService;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import it.nextworks.nephele.OFAAService.Inventory.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class Processor {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${OpendaylightURL}")
	private String ODLURL;

	@Value("${OfflineEngineURL}")
	private String OEURL;

    @Value("${server.port}")
    private String serverPort;

    private ProcessingTasksTemplates templates;
	
	private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    private final Set<Service> scheduled = new HashSet<>();

    private final Set<Service> establishing = new HashSet<>();

    private final Set<Service> terminating = new HashSet<>();

    private ExecutorService executor;
    {
	    executor = new ThreadPoolExecutor(2, 2, 1, TimeUnit.SECONDS, tasks);
        ((ThreadPoolExecutor) executor).prestartAllCoreThreads();
    }

    @PostConstruct
    public void templatesInit(){
        templates = new ProcessingTasksTemplates(serverPort);
    }

    @PreDestroy
    public void cleanup(){
            executor.shutdown();
        try{
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException exc){
            try{
                executor.shutdownNow();
                executor.awaitTermination(10, TimeUnit.SECONDS);
            }
            catch (InterruptedException e){
                log.error("Couldn't shutdown executor, got interrupted by exception ", e);
            }
        }
    }

    private volatile boolean waitingForOfflineEngine = false;

    private volatile boolean isUpdateQueued = false;
	
	public Processor(){
	}

	void addTerminating(Service service){
	    terminating.add(service);
    }
	
	void startRefreshing(Service serv) {
	    tasks.add(new TrafficMatGetter());
        scheduled.add(serv);
	}

	private void callbackScheduled(){
        for (Service service : scheduled){
            service.status = ServiceStatus.ESTABLISHING;
            establishing.add(service);
        }
    }

    private void callbackEstablished(){
        for (Service service : establishing){
            service.status = ServiceStatus.ACTIVE;
        }
    }

    private void callbackTerminated(){
        for (Service service : terminating){
            service.status = ServiceStatus.DELETED;
        }
    }

	private class TrafficMatGetter extends FutureTask<int[][]>{
	    private TrafficMatGetter(){
	        super(templates.new TrafficMatGetter());
        }

        @Override
        protected void done(){
            super.done();
            try {
                int[][] matrix = this.get();
                tasks.add(new NetAllocIdGetter(matrix));
            }
            catch (InterruptedException | CancellationException intExc){
                log.error("Computation interrupted:\n", intExc);
            }
            catch (ExecutionException execExc){
                log.error("Error while GETting the traffic matrix:\n", execExc);
            }
        }
    }

    private class NetAllocIdGetter extends FutureTask<String>{
        private NetAllocIdGetter(int[][] matrix){
                super(templates.new NetAllocGetter(matrix, OEURL));
        }

        @Override
        protected void done(){
            super.done();
            try {
                String netAllocId = this.get();
                if (!waitingForOfflineEngine) {
                    waitingForOfflineEngine = true;
                    //So that there is only one GET to the offline engine on the queue
                    tasks.add(new AllocationMatrixGetter(netAllocId));
                }
                else isUpdateQueued = true;
            }
            catch (InterruptedException | CancellationException intExc){
                log.error("Computation interrupted:\n", intExc);
            }
            catch (ExecutionException execExc){
                log.error("Error while GETting the net alloc ID:\n", execExc);
            }
        }
    }

    private class InventoryGetter extends FutureTask<Inventory>{
        private InventoryGetter(int[][] matrix){
            super(templates.new InventoryGetter(matrix));
        }

        @Override
        protected void done(){
            super.done();
            try {
                Inventory inventory = this.get();
                tasks.add(new InventoryPutter(inventory, ODLURL));
            }
            catch (InterruptedException | CancellationException intExc){
                log.error("Computation interrupted:\n", intExc);
            }
            catch (ExecutionException execExc){
                log.error("Error while GETting the inventory:\n", execExc);
            }
        }
    }

    private class AllocationMatrixGetter extends FutureTask<int[][]>{

        String netAllocId;

        Instant start = Instant.now();

        private AllocationMatrixGetter(String netAllocId){
            super(templates.new NetAllocationMatrixGetter(netAllocId, OEURL));
            this.netAllocId = netAllocId;
        }

        @Override
        protected void done(){
            super.done();
            try {
                int[][] matrix = this.get();
                if (matrix != null){ //Calculation completed
                    callbackScheduled();
                    tasks.add(new InventoryGetter(matrix));
                    waitingForOfflineEngine = false;
                    if (isUpdateQueued){
                        tasks.add(new TrafficMatGetter());
                        isUpdateQueued = false;
                    }
                }
                else { //request solution again
                    long timeFromStart = Instant.now().toEpochMilli() - start.toEpochMilli();
                    if (timeFromStart < 900){
                        //guarantees at least 0.9 seconds between retries, probably 1 sec.
                        Thread.sleep(1000 - timeFromStart);
                    }
                    tasks.add(new AllocationMatrixGetter(netAllocId));
                }
            }
            catch (InterruptedException | CancellationException intExc){
                log.error("Computation interrupted:\n", intExc);
            }
            catch (ExecutionException execExc){
                log.error("Error while GETting the net alloc matrix:\n", execExc);
            }
        }
    }

    private class InventoryPutter extends FutureTask<Boolean>{

        private InventoryPutter(Inventory inventory, String ODLURL){
            super(templates.new InventoryPutter(inventory, ODLURL), true);
        }

        @Override
        protected void done(){
            super.done();
            callbackEstablished();
            callbackTerminated();
        }
    }
}
