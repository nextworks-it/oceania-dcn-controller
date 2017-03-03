/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele.mock;

import com.google.common.util.concurrent.Futures;
import opticaltranslator.nephele.mock.flowutils.FlowAssembler;
import opticaltranslator.nephele.mock.flowutils.FlowParserException;
import opticaltranslator.nephele.mock.flowutils.FutureTypeSwapper;
import opticaltranslator.nephele.mock.flowutils.MatchParser;
import opticaltranslator.nephele.mock.flowutils.OutOfTagsException;
import opticaltranslator.nephele.mock.flowutils.PortFetcher;
import opticaltranslator.nephele.mock.flowutils.UtilsFactory;
import opticaltranslator.nephele.mock.flowutils.VlanProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowPodMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowTorMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowTorOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalFlowAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalResourceAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.OpticalFlowType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.EthOptFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.OptEthFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.OptOptFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.GetTranslatorTypeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.GetTranslatorTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslateAddOpticalFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslateRemoveOpticalFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslatorApiService;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class MockTranslator implements TranslatorApiService {

    private static final Logger LOG = LoggerFactory.getLogger(MockTranslator.class);

    private static final String NAME = "Emulated NEPHELE data plane translator";

    private static final ExecutorService callForwarder = Executors.newWorkStealingPool();

    private static final ExecutorService pushExecutor = Executors.newSingleThreadExecutor();

    static TranslatorApiService newMockTranslator(SalFlowService flowService, DataBroker dataBroker) {
        VlanProvider vlanProvider = UtilsFactory.newVLanProvider(dataBroker);
        return new MockTranslator(
                flowService,
                UtilsFactory.newMatchParser(vlanProvider),
                UtilsFactory.newFlowAssembler(),
                UtilsFactory.newPortFetcher(),
                UtilsFactory.newFutureSwapper(),
                vlanProvider
        );
    }

    private final SalFlowService flowService;

    private final MatchParser parser;
    private final FlowAssembler flowAssembler;
    private final PortFetcher fetcher;
    private final FutureTypeSwapper swapper;
    private final VlanProvider vlanProvider;

    private List<AddFlowTask> scheduleQueue;
    private TreeMap<Short, List<AddFlowTask>> futureActions = new TreeMap<>();
    private volatile Short previousScheduleId = -1;
    private volatile Short currentScheduleId;

    private volatile short state;
    private static final short ACCEPTING = 1;
    private static final short DELETING = 2;

    private final Object scheduleLock = new Object();
    private final Object pushingLock = new Object();

    MockTranslator(SalFlowService flowService,
                   MatchParser parser,
                   FlowAssembler flowAssembler,
                   PortFetcher fetcher,
                   FutureTypeSwapper swapper,
                   VlanProvider vlanProvider) {
        this.flowService = flowService;
        this.parser = parser;
        this.flowAssembler = flowAssembler;
        this.fetcher = fetcher;
        this.swapper = swapper;
        this.vlanProvider = vlanProvider;
    }

    @Override
    public Future<RpcResult<GetTranslatorTypeOutput>> getTranslatorType() {
        return Futures.immediateFuture(
                RpcResultBuilder.success(
                        new GetTranslatorTypeOutputBuilder().setTranslatorType(NAME)
                ).build()
        );
    }

    @Override
    public Future<RpcResult<Void>> translateAddOpticalFlow(TranslateAddOpticalFlowInput input) {
        NepheleFlowAttributes nepheleData;
        try {
            nepheleData = fetchNepheleData(unwrapInput(input.getFlowAdded()),
                    input.getFlowAdded().getFlowId().getValue());
            if (null == nepheleData) {
                return failWithFPE(String.format("Nephele data not found in flow %s.",
                        input.getFlowAdded().getFlowId().getValue()));
            }
        } catch (NullPointerException exc) {
            return failWithFPE(String.format("Nephele data not found in flow %s.",
                    input.getFlowAdded().getFlowId().getValue()));
        } catch (FlowParserException exc) {
            return failWithFPE(exc.getMessage());
        }
        if ((null != currentScheduleId && nepheleData.getScheduleId() < currentScheduleId)
                || previousScheduleId > nepheleData.getScheduleId()) {
            LOG.warn("Ignoring request for schedule {} while parsing schedule {}.",
                    nepheleData.getScheduleId(), (null == currentScheduleId) ? previousScheduleId : currentScheduleId);
            return RpcResultBuilder.<Void>success().buildFuture();
            // Avoid enqueuing unnecessary calls to the synchronized method.
        }
        return addToSchedule(input, nepheleData.getScheduleId(), nepheleData.getFlowCounter());
    }

    private NepheleFlowAttributes fetchNepheleData(OpticalFlowType flowType, String flowId)
    throws FlowParserException {
        try {
            NepheleFlowAttributes nepheleData;
            if (flowType instanceof OptOptFlow) {
                nepheleData = ((OptOptFlow) flowType).getOptOptCase().getOptMatchType()
                        .getAugmentation(NepheleFlowPodMatch.class);
            } else if (flowType instanceof OptEthFlow) {
                nepheleData = ((OptEthFlow) flowType).getOptEthCase().getOptMatchType()
                        .getAugmentation(NepheleFlowTorMatch.class);
            } else if (flowType instanceof EthOptFlow) {
                nepheleData = ((EthOptFlow) flowType).getEthOptCase().getOptOutputType()
                        .getAugmentation(NepheleFlowTorOut.class);
            } else {
                throw new FlowParserException(
                        String.format("Unexpected flow type: %s.", flowType.getClass().getSimpleName())
                );
            }
            if (null == nepheleData) {
                throw new FlowParserException(String.format("Nephele data not found in flow %s.", flowId));
            }
            return nepheleData;
        } catch (NullPointerException exc) {
            LOG.debug("Got null pointer exception in fetchNepheleData: ", exc);
            throw new FlowParserException(String.format("Nephele data not found in flow %s.", flowId));
        }
    }

    private class AddFlowTask implements RunnableFuture<RpcResult<Void>> {

        private final TranslateAddOpticalFlowInput input;
        private final Short scheduleId;
        private Future<RpcResult<Void>> rpcResultFuture = null;
        private volatile boolean ran = false;
        private volatile boolean cancelled = false;
        private volatile boolean completing = false;
        private final String flowId;
        private Thread runner = null;

        private AddFlowTask(Short scheduleId, TranslateAddOpticalFlowInput input) {
            this.input = input;
            flowId = this.input.getFlowAdded().getFlowId().getValue();
            this.scheduleId = scheduleId;
        }

        private boolean sendVlanRequest() {
            OpticalFlowType flowType = input.getFlowAdded().getOpticalFlowType();
            try {
                if (flowType instanceof OptOptFlow) {
                    OpticalResourceAttributes res = ((OptOptFlow) flowType).getOptOptCase().getOptMatchType();
                    vlanProvider.getVLan(res);
                    LOG.trace("");
                    return true;
                } else if (flowType instanceof EthOptFlow) {
                    OpticalResourceAttributes res = ((EthOptFlow) flowType).getEthOptCase().getOptOutputType();
                    vlanProvider.getVLan(res);
                    return true;
                } else if (flowType instanceof OptEthFlow) {
                    OpticalResourceAttributes res = ((OptEthFlow) flowType).getOptEthCase().getOptMatchType();
                    vlanProvider.getVLan(res);
                    return true;
                } else {
                    throw new FlowParserException(String.format("Unexpected flow type: %s.",
                            flowType.getClass().getSimpleName()));
                }
            } catch (FlowParserException | ReadFailedException
                    | OutOfTagsException | TransactionCommitFailedException e) {
                LOG.error("Cannot parse vlan of flow {} schedule {}, reason: {}.", flowId, scheduleId, e.getMessage());
                LOG.debug("Exception details: ", e);
                return false;
            }
        }

        @Override
        public void run() {
            if (ran || cancelled) {
                return;
            }
            internalRun();
        }

        private Future<RpcResult<Void>> internalRun() {
            if (cancelled) {
                return rpcResultFuture;
            }
            if (completing) {
                return rpcResultFuture;
            }
            synchronized (this) {
                if (cancelled) {
                    return rpcResultFuture;
                }
                if (completing) {
                    return rpcResultFuture;
                }
                // else
                ran = true;
                LOG.debug("Starting request {} of schedule {}.", flowId, scheduleId);
                runner = Thread.currentThread();
                try {
                    rpcResultFuture = internalAdd(input);
                    completing = true;
                    LOG.debug("Request {} of schedule {} sent to OFplugin.", flowId, scheduleId);
                } catch (InterruptedException exc) {
                    rpcResultFuture = failInterrupted(flowId);
                    cancelled = true;
                    LOG.debug("Request {} of schedule {} cancelled.", flowId, scheduleId);
                }
                return rpcResultFuture;
            }
        }

        @Override
        public boolean cancel(boolean b) {
            if (cancelled || completing) {
                return false;
            }
            if (ran) {
                if (b) {
                    runner.interrupt();
                }
            }
            synchronized (this) {
                if (cancelled || completing) {
                    return false;
                }
                else {
                    cancelled = true;
                    rpcResultFuture = failInterrupted(flowId);
                }
            }
            return cancelled;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return (cancelled || (null != rpcResultFuture && rpcResultFuture.isDone()));
        }

        @Override
        public RpcResult<Void> get() throws InterruptedException, ExecutionException {
            return internalRun().get();
        }

        @Override
        public RpcResult<Void> get(long l, @Nonnull TimeUnit timeUnit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return internalRun().get(l, timeUnit);
        }
    }

    private AddFlowTask encapsulate(TranslateAddOpticalFlowInput input, Short scheduleId) {
        return new AddFlowTask(scheduleId, input);
    }

    private Future<RpcResult<Void>> addToSchedule(TranslateAddOpticalFlowInput input,
                                                  short flowScheduleId, short flowCount) {
        if ((null != currentScheduleId && currentScheduleId > flowScheduleId)
                || previousScheduleId > flowScheduleId) {
            LOG.warn("Ignoring request for schedule {} while parsing schedule {}.",
                    flowScheduleId, currentScheduleId);
            return RpcResultBuilder.<Void>success().buildFuture();
        }
        if (state != ACCEPTING) {
            AddFlowTask task = encapsulate(input, flowScheduleId);
            futureActions.putIfAbsent(flowScheduleId, new ArrayList<>());
            futureActions.get(flowScheduleId).add(task);
            return task;
        }
        synchronized (scheduleLock) {
            if (state != ACCEPTING) {
                AddFlowTask task = encapsulate(input, flowScheduleId);
                futureActions.putIfAbsent(flowScheduleId, new ArrayList<>()).add(task);
                return task;
            }
            if (null == currentScheduleId) {
                currentScheduleId = flowScheduleId;
                LOG.info("Started parsing of schedule {}.", currentScheduleId);
                scheduleQueue = new LinkedList<>();
                SortedMap<Short, List<AddFlowTask>> tempMap = futureActions.headMap(currentScheduleId);
                LOG.warn("Dropping {} requests relative to schedules {} while parsing schedule {}.",
                        tempMap.size(), tempMap.keySet(), currentScheduleId);
                tempMap.clear(); // Wipe stale requests.
                futureActions.get(currentScheduleId).forEach((task) -> scheduleQueue.add(task));
                // Push requests that came too soon
            }
            if (currentScheduleId.equals(flowScheduleId)) {
                AddFlowTask callable = encapsulate(input, flowScheduleId);
                LOG.debug("Adding request {} to schedule {}.", callable.flowId, currentScheduleId);
                scheduleQueue.add(callable);
                if (scheduleQueue.size() == flowCount) {
                    pushExecutor.submit(() -> pushSchedule(new LinkedList<>(scheduleQueue)));
                }
                return callable;
            }
            else if (currentScheduleId < flowScheduleId) {
                AddFlowTask task = encapsulate(input, flowScheduleId);
                futureActions.putIfAbsent(flowScheduleId, new ArrayList<>()).add(task);
                return task;
            }
            else {
                LOG.warn("Ignoring request for schedule {} while parsing schedule {}.",
                        flowScheduleId, currentScheduleId);
                return RpcResultBuilder.<Void>success().buildFuture();
            }
        }
    }

    private void stopSchedule() {
        synchronized (scheduleLock) {
            scheduleQueue.forEach((ft) -> ft.cancel(true));
            scheduleQueue = null;
            previousScheduleId = currentScheduleId;
            currentScheduleId = null;
        }
    }

    private void pushSchedule(List<AddFlowTask> schedule) {
        synchronized (pushingLock) {
            LOG.debug("Push lock acquired, starting configuration push.");
            schedule.forEach(AddFlowTask::sendVlanRequest);
            schedule.forEach(callForwarder::execute);
            schedule.removeIf(RunnableFuture::isDone);
            if (schedule.isEmpty()) {
                LOG.debug("Configuration push successful, releasing lock.");
                return;
            }
            try {
                schedule.get(0).get(500, TimeUnit.MILLISECONDS);
                schedule.removeIf(RunnableFuture::isDone);
                if (schedule.isEmpty()) {
                    LOG.debug("Configuration push successful, releasing lock.");
                    return;
                }
            } catch (InterruptedException | ExecutionException exc) {
                // Don't care about exceptions, they will be logged somewhere else.
                schedule.removeIf(RunnableFuture::isDone);
                if (schedule.isEmpty()) {
                    LOG.debug("Configuration push successful, releasing lock.");
                    return;
                }
            } catch (TimeoutException exc) {
                LOG.warn("Configuration push task did not finish in 500 ms.");
            }
            try {
                schedule.get(0).get(500, TimeUnit.MILLISECONDS);
                schedule.removeIf(RunnableFuture::isDone);
                if (schedule.isEmpty()) {
                    LOG.debug("Configuration push successful, releasing lock.");
                    return;
                }
            } catch (InterruptedException | ExecutionException exc) {
                // Don't care about exceptions, they will be logged somewhere else.
                schedule.removeIf(RunnableFuture::isDone);
                if (schedule.isEmpty()) {
                    LOG.debug("Configuration push successful, releasing lock.");
                    return;
                }
            } catch (TimeoutException exc) {
                LOG.warn("Configuration push task did not finish in 1000 ms.");
            }
            try {
                schedule.get(0).get(500, TimeUnit.MILLISECONDS);
                schedule.removeIf(RunnableFuture::isDone);
                if (schedule.isEmpty()) {
                    LOG.debug("Configuration push successful, releasing lock.");
                    return;
                }
            } catch (InterruptedException | ExecutionException exc) {
                // Don't care about exceptions, they will be logged somewhere else.
                schedule.removeIf(RunnableFuture::isDone);
                if (schedule.isEmpty()) {
                    LOG.debug("Configuration push successful, releasing lock.");
                    return;
                }
            } catch (TimeoutException exc) {
                LOG.warn("Configuration push task did not finish in 1500 ms.");
            }
            try {
                schedule.get(0).get(500, TimeUnit.MILLISECONDS);
                schedule.removeIf(RunnableFuture::isDone);
                if (schedule.isEmpty()) {
                    LOG.debug("Configuration push successful, releasing lock.");
                    return;
                }
            } catch (InterruptedException | ExecutionException exc) {
                // Don't care about exceptions, they will be logged somewhere else.
                schedule.removeIf(RunnableFuture::isDone);
                if (schedule.isEmpty()) {
                    LOG.debug("Configuration push successful, releasing lock.");
                    return;
                }
            } catch (TimeoutException exc) {
                LOG.error("Configuration push task did not finish in 2000 ms. " +
                        "Releasing lock, future configurations might fail.");
            }
            schedule.removeIf(RunnableFuture::isDone);
            LOG.debug("Releasing push lock with {} unterminated tasks.");
        }
    }

    private String printMatch(Match match) {
        StringBuilder output = new StringBuilder("[match: ");
        if (null != match
                && null != match.getVlanMatch()
                && null != match.getVlanMatch().getVlanId()
                && null != match.getVlanMatch().getVlanId().getVlanId()) {
            output.append("vlan=").append(match.getVlanMatch().getVlanId().getVlanId().getValue()).append(", ");
        }
        if (null != match && null != match.getInPort()) {
            output.append("inport=").append(match.getInPort().getValue()).append(", ");
        }
        return output.toString();
    }

    private Future<RpcResult<Void>> internalAdd(TranslateAddOpticalFlowInput input)
            throws InterruptedException {

        try {
            OpticalFlowType flowType = unwrapInput(input.getFlowAdded());
            String flowId = input.getFlowAdded().getFlowId().getValue();
            LOG.debug("Parsing flow {} add request", flowId);
            Set<Match> matches = parser.createMatch(flowType);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            if (flowType instanceof OptOptFlow) {
                List<AddFlowInput> addFlows = new LinkedList<>();
                List<String> auxFlows = new LinkedList<>();
                for (Match match : matches) {
                    String outPort = fetcher.extractOutPort(flowType);
                    AddFlowInput addFlow = flowAssembler.createAddFlow(
                            match,
                            outPort,
                            new NodeRef(input.getNodeRef())
                    );
                    addFlows.add(addFlow);
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    if (LOG.isTraceEnabled()) {
                        auxFlows.add(printMatch(match) + " outPort: " + outPort + "]");
                    }
                }
                LOG.trace("While adding flow: {}, adding auxiliary flows: {}.", flowId, auxFlows);
                return swapper.generalizeFutureList(
                        addFlows.stream().map(flowService::addFlow).collect(Collectors.toList())
                );
            } else if (flowType instanceof OptEthFlow) {
                List<AddFlowInput> addFlows = new LinkedList<>();
                List<String> auxFlows = new LinkedList<>();
                for (Match match : matches) {
                    AddFlowInput addFlow = flowAssembler.createAddFlowPopping(
                            match,
                            fetcher.extractOutPort(flowType),
                            new NodeRef(input.getNodeRef())
                    );
                    addFlows.add(addFlow);
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                }
                LOG.trace("While adding flow: {}, adding auxiliary flows: {}.", flowId, auxFlows);
                return swapper.generalizeFutureList(
                        addFlows.stream().map(flowService::addFlow).collect(Collectors.toList())
                );
            } else if (flowType instanceof EthOptFlow) {
                List<AddFlowInput> addFlows = new LinkedList<>();
                List<String> auxFlows = new LinkedList<>();
                for (Match match : matches) {
                    AddFlowInput addFlow = flowAssembler.createAddFlowPushing(
                            match,
                            fetcher.extractOutPort(flowType),
                            new NodeRef(input.getNodeRef()),
                            vlanProvider.getVLan(((EthOptFlow) flowType).getEthOptCase().getOptOutputType())
                    );
                    addFlows.add(addFlow);
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                }
                LOG.trace("While adding flow: {}, adding auxiliary flows: {}.", flowId, auxFlows);
                return swapper.generalizeFutureList(
                        addFlows.stream().map(flowService::addFlow).collect(Collectors.toList())
                );
            } else {
                return failWithFPE(
                        String.format("Unexpected flow type: %s.", flowType.getImplementedInterface().getSimpleName())
                );
            }

        } catch (ReadFailedException | TransactionCommitFailedException exc) {
            return failDataStore(exc);
        } catch (OutOfTagsException exc) {
            return failOutOfTags(exc);
        } catch (FlowParserException exc) {
            return failWithFPE(exc.getMessage());
        }
    }

    @Override
    public Future<RpcResult<Void>> translateRemoveOpticalFlow(TranslateRemoveOpticalFlowInput input) {
        LOG.debug("Got delete request for flow: {}.", input.getFlowRemoved().getFlowId().getValue());
        try {
            NepheleFlowAttributes nepheleData;
            OpticalFlowType flowType = unwrapInput(input.getFlowRemoved());
            try {
                nepheleData = fetchNepheleData(input.getFlowRemoved().getOpticalFlowType(),
                        input.getFlowRemoved().getFlowId().getValue());
                if (null == nepheleData) {
                    return failWithFPE(String.format("Nephele data not found in flow %s.",
                            input.getFlowRemoved().getFlowId().getValue()));
                }
            } catch (NullPointerException exc) {
                return failWithFPE(String.format("Nephele data not found in flow %s.",
                        input.getFlowRemoved().getFlowId().getValue()));
            } catch (FlowParserException exc) {
                return failWithFPE(exc.getMessage());
            }

            if (nepheleData.getScheduleId().equals(currentScheduleId)) {
                state = DELETING;
                stopSchedule();
            }

            Set<Match> matches = parser.createMatch(flowType);

            if (flowType instanceof OptOptFlow) {
                List<RemoveFlowInput> delFlows = new LinkedList<>();
                for (Match match : matches) {
                    RemoveFlowInput removeFlow = flowAssembler.createRemoveFlow(
                            match,
                            fetcher.extractOutPort(flowType),
                            new NodeRef(input.getNodeRef())
                    );
                    delFlows.add(removeFlow);
                }
                LOG.trace("Deleting flows: {}.", delFlows);
                return swapper.generalizeFutureList(
                        delFlows.stream().map(flowService::removeFlow).collect(Collectors.toList())
                );
            } else if (flowType instanceof OptEthFlow) {
                List<RemoveFlowInput> delFlows = new LinkedList<>();
                for (Match match : matches) {
                    RemoveFlowInput removeFlow = flowAssembler.createRemoveFlowPopping(
                            match,
                            fetcher.extractOutPort(flowType),
                            new NodeRef(input.getNodeRef())
                    );
                    delFlows.add(removeFlow);
                }
                LOG.trace("Deleting flows: {}.", delFlows);
                return swapper.generalizeFutureList(
                        delFlows.stream().map(flowService::removeFlow).collect(Collectors.toList())
                );
            } else if (flowType instanceof EthOptFlow) {
                List<RemoveFlowInput> delFlows = new LinkedList<>();
                for (Match match : matches) {
                    RemoveFlowInput removeFlow = flowAssembler.createRemoveFlowPushing(
                            match,
                            fetcher.extractOutPort(flowType),
                            new NodeRef(input.getNodeRef()),
                            vlanProvider.getVLan((EthOptFlow) flowType)
                    );
                    delFlows.add(removeFlow);
                }
                LOG.trace("Deleting flows: {}.", delFlows);
                return swapper.generalizeFutureList(
                        delFlows.stream().map(flowService::removeFlow).collect(Collectors.toList())
                );
            } else
                throw new FlowParserException(
                        String.format("Unexpected flow type: %s.", flowType.getImplementedInterface().getSimpleName())
                );

        } catch (ReadFailedException | TransactionCommitFailedException exc) {
            return failDataStore(exc);

        } catch (OutOfTagsException exc) {
            return failOutOfTags(exc);

        } catch (FlowParserException exc) {
            return failWithFPE(exc.getMessage());
        }
    }

    private OpticalFlowType unwrapInput(OpticalFlowAttributes flow) throws FlowParserException {

        if (null == flow) {
            throw new FlowParserException("Input flow is null");
        }

        OpticalFlowType flowType = flow.getOpticalFlowType();

        if (null == flowType) {
            throw new FlowParserException("Input flow type is null.");
        }
        return flowType;
    }

    private Future<RpcResult<Void>> failWithFPE(String message) {
        return RpcResultBuilder.<Void>failed().withError(
                RpcError.ErrorType.PROTOCOL,
                "Malformed message: " + message)
                .buildFuture();
    }

    private Future<RpcResult<Void>> failOutOfTags(OutOfTagsException exc) {
        LOG.error("The system is out of VLAN tags to assign. Please, emulate smaller networks.");
        return RpcResultBuilder.<Void>failed().withError(
                RpcError.ErrorType.APPLICATION,
                "The system is out of VLAN tags to assign. Please, emulate smaller networks.",
                exc
        ).buildFuture();
    }

    private Future<RpcResult<Void>> failDataStore(Exception exc) {
        LOG.error("Data store communication failed with exception: ", exc);
        return RpcResultBuilder.<Void>failed().withError(
                RpcError.ErrorType.APPLICATION,
                String.format("Data store communication failed with exception: %s: %s.",
                        exc.getClass().getSimpleName(),
                        exc.getMessage()),
                exc
        ).buildFuture();
    }

    private Future<RpcResult<Void>> failInterrupted(String flowId) {
        LOG.warn("Got interrupted while pushing flow {}. Aborting.", flowId);
        return RpcResultBuilder.<Void>failed().withError(
                RpcError.ErrorType.APPLICATION,
                String.format("Got interrupted while pushing flow %s. Aborting.", flowId)
        ).buildFuture();
    }
}
