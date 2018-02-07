package it.nextworks.nephele.OFAAService;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;
import it.nextworks.nephele.OFTranslator.Inventory;
import it.nextworks.nephele.TrafficMatrixEngine.TrafficChanges;
import it.nextworks.nephele.TrafficMatrixEngine.TrafficMatrix;
import it.nextworks.nephele.appaffdb.DbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Value("${concurrentRequests}")
    private int concurrency;

    @Value("${useIncremental}")
    private boolean useIncremental;

    @Autowired
    private DbManager db;

    @Autowired
    private Const constants;

    private Semaphore computeSemaphore;
    private Semaphore invLock;

    private AtomicBoolean waiting = new AtomicBoolean(false);

    private ProcessingTasksTemplates templates;


    private ExecutorService executor;

    private volatile boolean computed = false;

    {
        executor = Executors.newFixedThreadPool(8);
//        executor = new ThreadPoolExecutor(8, 8, 1, TimeUnit.SECONDS, tasks);
//        ((ThreadPoolExecutor) executor).prestartAllCoreThreads();
    }

//    private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    @PostConstruct
    public void init() {
        // Initialize locks
        computeSemaphore = new Semaphore(concurrency);
        invLock = new Semaphore(1);
        log.debug("Processor concurrency level: {}.", computeSemaphore.availablePermits());
        boolean computed = false;

        // Initialize templates
        templates = new ProcessingTasksTemplates(serverPort);

        //Recover services from DB
        db.resetToStatus(ServiceStatus.REQUESTED, ServiceStatus.SCHEDULED);

        int previouslyActive = db.resetToRequested(ServiceStatus.ACTIVE);
        if (previouslyActive == -1) {
            throw new IllegalStateException("Could not recover old connections from db.");
        }

        log.debug("Found {} previously active connections in db.", previouslyActive);

        if (previouslyActive > 0) {
            log.debug("Starting instantiation.");
            startRefreshing(false);
            computed = true;
        }

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException exc) {
            log.warn("Sleep in processor initialization interrupted.");
        }

        int previouslyScheduled = db.resetToRequested(ServiceStatus.SCHEDULED);
        if (previouslyScheduled == -1) {
            throw new IllegalStateException("Could not recover old connections from db.");
        }

        int previouslyEstablishing = db.resetToRequested(ServiceStatus.ESTABLISHING);
        if (previouslyEstablishing == -1) {
            throw new IllegalStateException("Could not recover old connections from db.");
        }

        log.debug("Found {} connections previously instantiating in db.", previouslyEstablishing + previouslyScheduled);

        if (previouslyEstablishing + previouslyScheduled > 0) {
            log.debug("Starting instantiation.");
            if (computed) {
                startRefreshing(); // only the very first computation needs to be non incremental
            } else {
                startRefreshing(false);
            }
            computed = true;
        }

//        if (!computed) {
//            constants.initialize();
//            executor.submit(new NetAllocIdGetter(new TrafficMatrix(Const.matrix)));
//        }

        this.computed = computed;

    }

    @PreDestroy
    public void cleanup() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException exc) {
            try {
                executor.shutdownNow();
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Couldn't shutdown executor, got interrupted by exception ", e);
            }
        }
    }

    private volatile boolean waitingForOfflineEngine = false;

    private volatile boolean isUpdateQueued = false;

    public Processor() {
    }

    synchronized void addTerminating(Service service) {
        db.updateStatus(service, ServiceStatus.TERMINATION_REQUESTED);
    }

    synchronized void startRefreshing() {
        startRefreshing(useIncremental);
    }

    private synchronized void startRefreshing(boolean incremental) {
        boolean current_computed = computed;
        computed = true;
        log.trace("This is {}the first scheduled computation.", current_computed ? "not " : "");
        log.trace("Incremental is {}.", String.valueOf(incremental));
        if (!computeSemaphore.tryAcquire()) {
            log.trace("Hit concurrency cap. Delaying execution.");
            scheduleRefresh(incremental && current_computed);
        } else {
            if (incremental && current_computed) {
                TrafficMatChangesGetter task = new TrafficMatChangesGetter();
                executor.submit(task);
                log.debug("Starting Traffic changes computation: OpId {}.", task.id);
            } else {
                TrafficMatGetter task = new TrafficMatGetter();
                executor.submit(task);
                log.debug("Starting Traffic matrix computation: OpId {}.", task.id);
            }
        }
    }

    private void scheduleRefresh(boolean incremental) {
        if (waiting.compareAndSet(false, true)) {
            if (incremental) {
                executor.submit(new WaitForChanges());
            } else {
                executor.submit(new WaitForSchedule());
            }
        }
        // else, there is already someone waiting, it will get our path installed.
    }

    private void callbackScheduling(Collection<String> requested) {
        for (String service : requested) {
            log.debug("Scheduling service: {}.", service);
            db.updateStatus(service, ServiceStatus.SCHEDULED);
        }
    }

    private void callbackSchedulingDone(Collection<String> scheduled) {
        for (String service : scheduled) {
            log.debug("Establishing service: {}.", service);
            db.updateStatus(service, ServiceStatus.ESTABLISHING);
        }
    }

    private void callbackEstablished(Collection<String> establishing) {
        for (String service : establishing) {
            log.debug("Established service: {}.", service);
            db.updateStatus(service, ServiceStatus.ACTIVE);
        }
    }

    private void callbackTerminated(Collection<String> terminating) {
        for (String service : terminating) {
            log.debug("Terminated service: {}.", service);
            db.updateStatus(service, ServiceStatus.DELETED);
        }
    }

    private void callbackTerminating(Collection<String> terminating) {
        for (String service : terminating) {
            log.debug("Terminating service: {}.", service);
            db.updateStatus(service, ServiceStatus.TERMINATING);
        }
    }

    private void fail(Collection<String> failed) {
        for (String service : failed) {
            log.warn("Service {} failed.", service);
            db.updateStatus(service, ServiceStatus.FAILED);
        }
    }

    private void terminateFail(Collection<String> failed) {
        for (String service : failed) {
            log.warn("Service {} termination failed.", service);
            db.updateStatus(service, ServiceStatus.TERMINATION_REQUESTED);
        }
    }

    private class TrafficMatGetter extends FutureTask<TrafficMatrix> {
        protected UUID id;
        private List<String> terminating;
        private List<String> requested;

        private TrafficMatGetter() {
            super(templates.new TrafficMatGetter());
            id = UUID.randomUUID(); 
        }

        @Override
        public void run() {
            requested = db.queryWithStatus(ServiceStatus.REQUESTED);
            terminating = db.queryWithStatus(ServiceStatus.TERMINATION_REQUESTED);
            callbackScheduling(requested);
            callbackTerminating(terminating);
            super.run();
        }

        @Override
        protected void done() {
            super.done();
            log.debug("Got traffic matrix. OpId: {}.", this.id);
            try {
                TrafficMatrix matrix = this.get();
                NetAllocIdGetter task = new NetAllocIdGetter(matrix, requested, terminating);
                executor.submit(task);
                log.debug("Posting traffic matrix. OpId: {}.", task.id);
            } catch (Exception exc) {
                fail(requested);
                terminateFail(terminating);
                computeSemaphore.release();
                log.error("Error while GETting the traffic matrix:\n", exc);
            }
        }
    }

    private class TrafficMatChangesGetter extends FutureTask<TrafficChanges> {
        protected UUID id;
        private List<String> toBeTerminated;
        private List<String> requested;

        private TrafficMatChangesGetter() {
            super(templates.new TrafficMatChangesGetter());
            id = UUID.randomUUID();
        }

        @Override
        public void run() {
            requested = db.queryWithStatus(ServiceStatus.REQUESTED);
            toBeTerminated = db.queryWithStatus(ServiceStatus.TERMINATION_REQUESTED);
            callbackScheduling(requested);
            callbackTerminating(toBeTerminated);
            super.run();
        }

        @Override
        protected void done() {
            super.done();
            log.debug("Got traffic matrix changes. OpId: {}.", this.id);
            try {
                TrafficChanges matrix = this.get();
                NetAllocChangesIdGetter task = new NetAllocChangesIdGetter(matrix, requested, toBeTerminated);
                executor.submit(task);
                log.debug("Posting traffic changes. OpId: {}.", task.id);
            } catch (Exception exc) {
                fail(requested);
                terminateFail(toBeTerminated);
                computeSemaphore.release();
                log.error("Error while GETting the traffic matrix:\n", exc);
            }
        }
    }

    private class NetAllocIdGetter extends FutureTask<String> {
        private UUID id;
        private List<String> requested;
        private List<String> terminating;

        private NetAllocIdGetter(TrafficMatrix matrix, List<String> requested, List<String> terminating) {
            super(templates.new NetAllocIdGetter(matrix, OEURL));
            id = UUID.randomUUID();
            this.requested = requested;
            this.terminating = terminating;
        }

        @Override
        protected void done() {
            super.done();
            try {
                String netAllocId = this.get();
                if (!waitingForOfflineEngine) {
                    waitingForOfflineEngine = true;
                    AllocationGetter task = new AllocationGetter(netAllocId, this.id, requested, terminating);
                    //So that there is only one GET to the offline engine on the queue
                    log.debug("Getting network allocation with ID : " + netAllocId);
                    executor.submit(task);
                } else isUpdateQueued = true;
            } catch (Exception exc) {
                fail(requested);
                terminateFail(terminating);
                computeSemaphore.release();
                waitingForOfflineEngine = false;
                log.error("Error while GETting the net alloc ID:\n", exc);
            }
        }
    }

    private class NetAllocChangesIdGetter extends FutureTask<String> {
        private UUID id;
        private List<String> requested;
        private List<String> terminating;

        private NetAllocChangesIdGetter(TrafficChanges matrix, List<String> requested, List<String> terminating) {
            super(templates.new NetAllocChangesIdGetter(matrix, OEURL));
            id = UUID.randomUUID();
            this.requested = requested;
            this.terminating = terminating;
        }

        @Override
        protected void done() {
            try {
                String netAllocId = this.get();
                if (!waitingForOfflineEngine) {
                    waitingForOfflineEngine = true;
                    AllocationGetter task = new AllocationGetter(netAllocId, this.id, requested, terminating);
                    //So that there is only one GET to the offline engine on the queue
                    log.debug("Getting network allocation with ID : " + netAllocId);
                    executor.submit(task);
                } else isUpdateQueued = true;
            } catch (Exception exc) {
                fail(requested);
                terminateFail(terminating);
                waitingForOfflineEngine = false;
                computeSemaphore.release();
                log.error("Error while GETting the net changes ID:\n", exc);
            }
        }
    }

    private class InventoryGetter extends FutureTask<Inventory> {
        private UUID id;
        private List<String> requested;
        private List<String> terminating;

        private InventoryGetter(NetSolBase netSol, List<String> requested, List<String> terminating) {
            super(templates.new InventoryGetter(netSol));
            id = UUID.randomUUID();
            this.requested = requested;
            this.terminating = terminating;
        }

        @Override
        protected void done() {
            super.done();
            log.debug("Got inventory. OpId: {}.", this.id);
            try {
                Inventory inventory = this.get();
                InventoryPutter task = new InventoryPutter(inventory, ODLURL, requested, terminating);
                executor.submit(task);
                log.debug("Sending inventory. OpId: {}.", task.id);
            } catch (Exception execExc) {
                fail(requested);
                terminateFail(terminating);
                invLock.release();
                log.error("Error while translating the inventory:\n", execExc);
            }
        }
    }

    private class AllocationGetter extends FutureTask<NetSolBase> {
        private UUID id;
        private List<String> requested;
        private List<String> terminating;

        String netAllocId;

        Instant start = Instant.now();

        private AllocationGetter(String netAllocId, UUID opId, List<String> requested, List<String> terminating) {
            super(templates.new NetAllocationMatrixGetter(netAllocId, OEURL));
            this.netAllocId = netAllocId;
            id = opId;
            this.requested = requested;
            this.terminating = terminating;
        }

        @Override
        protected void done() {
            super.done();
            try {
                NetSolBase netSol = this.get();
                switch (netSol.status) {
                    case COMPUTED: //Calculation completed
                        log.debug("Got network allocation. OpId: {}.", this.id);
                        callbackSchedulingDone(requested);
                        invLock.acquire();
                        log.debug("Releasing computation permit.");
                        computeSemaphore.release();
                        InventoryGetter task = new InventoryGetter(netSol, requested, terminating);
                        executor.submit(task);
                        log.debug("Translating inventory. OpId: {}.", task.id);
                        waitingForOfflineEngine = false;
                        if (isUpdateQueued) {
                            executor.submit(new TrafficMatGetter());
                            isUpdateQueued = false;
                        }
                        break;

                    case COMPUTING: //request solution again
                        long timeFromStart = Instant.now().toEpochMilli() - start.toEpochMilli();
                        if (timeFromStart < 900) {
                            //guarantees at least 0.9 seconds between retries, probably 1 sec.
                            Thread.sleep(1000 - timeFromStart);
                        }
                        executor.submit(new AllocationGetter(netAllocId, this.id, requested, terminating));
                        break;

                    case FAILED:
                        log.error("Computation failed, not enough bandwidth. OpId: {}.", this.id);
                        throw new IllegalStateException("Computation failed.");
                    default:
                        String message = String.format("Unexpected status %s in response from offline", netSol.status);
                        log.error(message);
                        throw new IllegalStateException(message);
                }
            } catch (Exception execExc) {
                fail(requested);
                terminateFail(terminating);
                log.debug("Releasing computation permit.");
                waitingForOfflineEngine = false;
                computeSemaphore.release();
                log.error("Error while GETting the net alloc matrix:\n", execExc);
            }
        }
    }

    private class InventoryPutter extends FutureTask<Boolean> {

        private UUID id;
        private List<String> requested;
        private List<String> terminating;

        private InventoryPutter(Inventory inventory, String ODLURL, List<String> requested, List<String> terminating) {
            super(templates.new InventoryPutter(inventory, ODLURL), true);
            id = UUID.randomUUID();
            this.requested = requested;
            this.terminating = terminating;
        }

        @Override
        protected void done() {
            super.done();
            invLock.release();
            if (this.isDone()) {
                try {
                    log.debug("Inventory sent, status " + this.get().toString());
                    log.trace("Releasing inventory lock.");
                    log.debug("Inventory pushed. OpId: {}.", this.id);
                    callbackEstablished(requested);
                    callbackTerminated(terminating);
                } catch (Exception exc) {
                    log.error("Sending inventory got exception: ", exc);
                    fail(requested);
                    terminateFail(terminating);
                    // Do not fail terminating, it will be terminated by the next successful pass-through
                }
            }
        }
    }

    private class WaitForChanges extends TrafficMatChangesGetter {

        @Override
        public void run() {
            log.debug("Waiting for a concurrency slot to free up. OpId: {}.", this.id);
            try {
                computeSemaphore.acquire();
                waiting.set(false);
                log.trace("A slot freed up, resuming.");
                log.debug("Starting Traffic changes computation: OpId {}.", this.id);
                super.run();
            } catch (InterruptedException exc) {
                log.warn("Wait for incremental schedule interrupted: ", exc);
            }
        }
    }

    private class WaitForSchedule extends TrafficMatGetter {

        @Override
        public void run() {
            log.debug("Waiting for a concurrency slot to free up. OpId: {}.", this.id);
            try {
                computeSemaphore.acquire();
                waiting.set(false);
                log.trace("A slot freed up, resuming.");
                log.debug("Starting Traffic matrix computation: OpId {}.", this.id);
                super.run();
            } catch (InterruptedException exc) {
                log.warn("Wait for schedule interrupted: ", exc);
            }
        }
    }
}
