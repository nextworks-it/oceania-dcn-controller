/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele;

import opticaltranslator.nephele.flowutils.FlowAssembler;
import opticaltranslator.nephele.flowutils.FlowParserException;
import opticaltranslator.nephele.flowutils.FutureTypeSwapper;
import opticaltranslator.nephele.flowutils.MatchParser;
import opticaltranslator.nephele.flowutils.PortFetcher;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.AddNepheleFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.OpticalTranslatorNepheleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.RemoveNepheleFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.OpticalFlowType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslatorApiService;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class NepheleServiceImpl implements OpticalTranslatorNepheleService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(NepheleServiceImpl.class);
    private final SalFlowService flowService;

    private final MatchParser parser;
    private final FlowAssembler flowAssembler;
    private final PortFetcher fetcher;
    private final FutureTypeSwapper swapper;

    private final RpcProviderRegistry rpcRegistry;

    private final BindingAwareBroker.RpcRegistration<OpticalTranslatorNepheleService> thisRegistration;

    private BindingAwareBroker.RpcRegistration<TranslatorApiService> translatorRegistration;

    NepheleServiceImpl(SalFlowService flowService,
                       RpcProviderRegistry rpcRegistry,
                       MatchParser parser,
                       FlowAssembler flowAssembler,
                       PortFetcher fetcher,
                       FutureTypeSwapper swapper) {
        LOG.debug("Initializing nephele translator service.");
        this.flowService = flowService;
        this.parser = parser;
        this.flowAssembler = flowAssembler;
        this.fetcher = fetcher;
        this.swapper = swapper;
        this.rpcRegistry = rpcRegistry;
        thisRegistration = rpcRegistry.addRpcImplementation(OpticalTranslatorNepheleService.class, this);
        LOG.info("Nephele translator service initialized.");
    }

    private TranslatorApiService buildTranslator() {
        return NepheleTranslator.newNepheleTranslator(this);
    }

    @Override
    public Future<RpcResult<Void>> addNepheleFlow(AddNepheleFlowInput input) {
        String flowId;
        try {
             flowId = input.getNepheleFlowAdded().getFlowId().getValue();
        } catch (NullPointerException exc) {
            LOG.error("Input did not specify a flow Id.");
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.PROTOCOL,
                    "Input did not specify a flow Id.",
                    new FlowParserException("Input did not specify a flow Id.")
            ).buildFuture();
        }
        LOG.debug("Received call to add flow with id '{}'.", flowId);
        try {

            OpticalFlowType flowType = NepheleTranslator.unwrapInput(input.getNepheleFlowAdded());

            NepheleFlowAttributes nepheleData = input.getNepheleFlowAdded();
            if (null == nepheleData || null == nepheleData.getFlowCounter() || null == nepheleData.getScheduleId()) {
                throw new FlowParserException(
                        String.format("Nephele specific data absent from flow request '%s'.",
                                input.getNepheleFlowAdded().getFlowId().getValue()
                        )
                );
            }

            LOG.debug("Extracted flow data, translating flow '{}'.", flowId);

            Match match = parser.createMatch(flowType, nepheleData);

            LOG.debug("Match built, building add flow input for flow '{}'.", flowId);

            AddFlowInput addFlow = flowAssembler.createAddFlow(
                    match,
                    fetcher.extractOutPort(flowType),
                    new NodeRef(input.getNodeRef())
            );
            LOG.debug("Sending request for flow '{}'.", flowId);

            return swapper.generalizeFuture(flowService.addFlow(addFlow));

        } catch (FlowParserException exc) {
            return NepheleTranslator.failWithFPE(exc.getMessage());
        }
    }

    @Override
    public Future<RpcResult<Void>> removeNepheleFlow(RemoveNepheleFlowInput input) {
        String flowId;
        try {
             flowId = input.getNepheleFlowRemoved().getFlowId().getValue();
        } catch (NullPointerException exc) {
            LOG.error("Input did not specify a flow Id.");
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.PROTOCOL,
                    "Input did not specify a flow Id.",
                    new FlowParserException("Input did not specify a flow Id.")
            ).buildFuture();
        }
        LOG.debug("Received call to remove flow, flow id: '{}'.", flowId);
        try {

            OpticalFlowType flowType = NepheleTranslator.unwrapInput(input.getNepheleFlowRemoved());

            NepheleFlowAttributes nepheleData = input.getNepheleFlowRemoved();
            if (null == nepheleData || null == nepheleData.getFlowCounter() || null == nepheleData.getScheduleId()) {
                throw new FlowParserException(
                        String.format("Nephele specific data absent from flow request '%s'.",
                                input.getNepheleFlowRemoved().getFlowId().getValue()
                        )
                );
            }

            LOG.debug("Extracted flow data, translating flow '{}'.", flowId);

            Match match = parser.createMatch(flowType, nepheleData);

            LOG.debug("Match built, building remove flow input for flow '{}'.", flowId);

            RemoveFlowInput removeFlow = flowAssembler.createRemoveFlow(
                    match,
                    fetcher.extractOutPort(flowType),
                    new NodeRef(input.getNodeRef())
            );
            LOG.debug("Sending request for flow '{}'.", flowId);
            return swapper.generalizeFuture(flowService.removeFlow(removeFlow));

        } catch (FlowParserException exc) {
            return NepheleTranslator.failWithFPE(exc.getMessage());
        }
    }

    @Override
    public Future<RpcResult<Void>> activateNepheleTranslator() {
        LOG.debug("Activating nephele translator");
        if (null != getTranslator()) {
            String errorString = String.format("%s is currently loaded. Cannot activate another.",
                    unwrapType(getTranslator()));
            LOG.debug(errorString);
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.PROTOCOL,
                    errorString,
                    new IllegalStateException("Another translator implementation already loaded."))
                    .buildFuture();
        }
        try {
            translatorRegistration =
                    rpcRegistry.addRpcImplementation(TranslatorApiService.class, buildTranslator());
            LOG.info("Nephele translator activated.");
            return RpcResultBuilder.<Void>success().buildFuture();
        } catch (Exception exc) {
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.APPLICATION,
                    "Could not activated nephele translator.",
                    exc
            )
                    .buildFuture();
        }
    }

    @Override
    public Future<RpcResult<Void>> deactivateNepheleTranslator() {
        LOG.debug("Deactivating nephele translator");
        try {
            if (null != translatorRegistration)
                translatorRegistration.close();
            LOG.info("Emulation translator deactivated.");
            return RpcResultBuilder.<Void>success().buildFuture();
        } catch (Exception exc) {
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.APPLICATION,
                    "Could not turn off nephele translator.",
                    exc
            )
                    .buildFuture();
        }
    }

    private TranslatorApiService getTranslator() {
        TranslatorApiService t = rpcRegistry.getRpcService(TranslatorApiService.class);
        try {
            t.getTranslatorType().get();
            return t;
        } catch (Exception e) {
            LOG.info("No translator service found. Exception {}: {}.", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private String unwrapType(TranslatorApiService t) {
        try {
            return t.getTranslatorType().get().getResult().getTranslatorType();
        } catch (InterruptedException | ExecutionException exc) {
            LOG.error("Really, really, really shouldn't have happened.");
            //hence, we rethrow
            throw new RuntimeException(exc);
        }
    }

    @Override
    public void close() throws Exception {
        if (null != translatorRegistration)
            translatorRegistration.close();
        if (null != thisRegistration)
            thisRegistration.close();
    }
}
