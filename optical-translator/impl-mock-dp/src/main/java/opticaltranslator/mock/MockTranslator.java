/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.mock;

import com.google.common.util.concurrent.Futures;
import opticaltranslator.mock.flowutils.FlowAssembler;
import opticaltranslator.mock.flowutils.FlowParserException;
import opticaltranslator.mock.flowutils.FutureTypeSwapper;
import opticaltranslator.mock.flowutils.MatchParser;
import opticaltranslator.mock.flowutils.OutOfTagsException;
import opticaltranslator.mock.flowutils.PortFetcher;
import opticaltranslator.mock.flowutils.UtilsFactory;
import opticaltranslator.mock.flowutils.VlanProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalFlowAttributes;
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

import java.util.concurrent.Future;

public class MockTranslator implements TranslatorApiService {

    private static final Logger LOG = LoggerFactory.getLogger(MockTranslator.class);

    private static final String NAME = "Emulated data plane translator";

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

        try {

            OpticalFlowType flowType = unwrapInput(input.getFlowAdded());

            // TODO contains getVlan call
            Match match = parser.createMatch(flowType);

            if (flowType instanceof OptOptFlow) {
                AddFlowInput addFlow = flowAssembler.createAddFlow(
                        match,
                        fetcher.extractOutPort(flowType),
                        new NodeRef(input.getNodeRef())
                );
                return swapper.generalizeFuture(flowService.addFlow(addFlow));
            }

            else if (flowType instanceof OptEthFlow) {
                AddFlowInput addFlow = flowAssembler.createAddFlowPopping(
                        match,
                        fetcher.extractOutPort(flowType),
                        new NodeRef(input.getNodeRef())
                );
                return swapper.generalizeFuture(flowService.addFlow(addFlow));
            }

            else if (flowType instanceof EthOptFlow) {
                AddFlowInput addFlow = flowAssembler.createAddFlowPushing(
                        match,
                        fetcher.extractOutPort(flowType),
                        new NodeRef(input.getNodeRef()),
                        // TODO check getVlan call
                        vlanProvider.getVLan(((EthOptFlow)flowType).getEthOptCase().getOptOutputType())
                );
                return swapper.generalizeFuture(flowService.addFlow(addFlow));
            }

            else
                throw new FlowParserException(
                        String.format("Unexpected flow type: %s.",flowType.getImplementedInterface().getSimpleName())
                );

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

        try {

            OpticalFlowType flowType = unwrapInput(input.getFlowRemoved());

            // TODO contains getVlan call
            Match match = parser.createMatch(flowType);

            if (flowType instanceof OptOptFlow) {
                RemoveFlowInput removeFlow = flowAssembler.createRemoveFlow(
                        match,
                        fetcher.extractOutPort(flowType),
                        new NodeRef(input.getNodeRef())
                );
                return swapper.generalizeFuture(flowService.removeFlow(removeFlow));
            }

            else if (flowType instanceof OptEthFlow) {
                RemoveFlowInput removeFlow = flowAssembler.createRemoveFlowPopping(
                        match,
                        fetcher.extractOutPort(flowType),
                        new NodeRef(input.getNodeRef())
                );
                return swapper.generalizeFuture(flowService.removeFlow(removeFlow));
            }

            else if (flowType instanceof EthOptFlow) {
                RemoveFlowInput removeFlow = flowAssembler.createRemoveFlowPushing(
                        match,
                        fetcher.extractOutPort(flowType),
                        new NodeRef(input.getNodeRef()),
                        // TODO check getVlan call
                        vlanProvider.getVLan((EthOptFlow)flowType)
                );
                return swapper.generalizeFuture(flowService.removeFlow(removeFlow));
            }

            else
                throw new FlowParserException(
                        String.format("Unexpected flow type: %s.",flowType.getImplementedInterface().getSimpleName())
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
}
