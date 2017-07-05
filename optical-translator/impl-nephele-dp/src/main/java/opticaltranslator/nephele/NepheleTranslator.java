/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele;

import com.google.common.util.concurrent.Futures;
import opticaltranslator.nephele.flowutils.FlowParserException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.AddNepheleFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.AddNepheleFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowPodMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowTorMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowTorOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.OpticalTranslatorNepheleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.RemoveNepheleFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.add.nephele.flow.input.NepheleFlowAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.remove.nephele.flow.input.NepheleFlowRemovedBuilder;
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

public class NepheleTranslator implements TranslatorApiService {

    private static final Logger LOG = LoggerFactory.getLogger(NepheleTranslator.class);

    static final String NAME = "Nephele data plane translator";

    static TranslatorApiService newNepheleTranslator(OpticalTranslatorNepheleService nepheleService) {
        return new NepheleTranslator(nepheleService);
    }

    private final OpticalTranslatorNepheleService nepheleService;

    NepheleTranslator(OpticalTranslatorNepheleService nepheleService) {

        this.nepheleService = nepheleService;
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

        LOG.debug("Got call to translate add optical flow (NEPHELE translator), flow: {}.",
                input.getFlowAdded().getFlowId().getValue());

        NepheleFlowAttributes nepheleResources;
        try {
            nepheleResources = extractNepheleResources(input.getFlowAdded().getOpticalFlowType());
        } catch (FlowParserException exc) {
            LOG.error("Error while parsing input for add optical flow: {}.", exc.getMessage());
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.PROTOCOL,
                    exc.getMessage(),
                    exc
            ).buildFuture();
        }
        LOG.debug("Successfully recovered nephele specific data, starting flow push.");
        AddNepheleFlowInput addFlowInput = new AddNepheleFlowInputBuilder()
                .setNodeRef(input.getNodeRef())
                .setNepheleFlowAdded(
                        new NepheleFlowAddedBuilder(nepheleResources)
                                // ^ this injects nephele resources
                                .setOpticalFlowType(input.getFlowAdded().getOpticalFlowType())
                                // ^ this injects everything else
                                .setFlowId(input.getFlowAdded().getFlowId())
                                // except the flow ID
                                .build()
                )
                .build();

        LOG.debug(
                "Flow data: optFlow = {}, schedule = {}, count = {}",
                input.getFlowAdded().getOpticalFlowType(),
                nepheleResources.getScheduleId(), nepheleResources.getFlowCounter()
        );
        return nepheleService.addNepheleFlow(
                addFlowInput
        );
    }

    @Override
    public Future<RpcResult<Void>> translateRemoveOpticalFlow(TranslateRemoveOpticalFlowInput input) {

        LOG.debug("Got call to translate remove optical flow (NEPHELE translator), flow: {}.",
                input.getFlowRemoved().getFlowId().getValue());

        NepheleFlowAttributes nepheleResources;
        try {
            nepheleResources = extractNepheleResources(input.getFlowRemoved().getOpticalFlowType());
        } catch (FlowParserException exc) {
            LOG.error("Error while parsing input for remove optical flow: {}.", exc.getMessage());
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.PROTOCOL,
                    exc.getMessage(),
                    exc
            ).buildFuture();
        }
        LOG.debug("Successfully recovered nephele specific data, starting flow removal.");
        return nepheleService.removeNepheleFlow(
                new RemoveNepheleFlowInputBuilder()
                        .setNodeRef(input.getNodeRef())
                        .setNepheleFlowRemoved(
                                new NepheleFlowRemovedBuilder(nepheleResources)
                                        // ^ this injects nephele resources
                                        .setOpticalFlowType(input.getFlowRemoved().getOpticalFlowType())
                                        // ^ this injects everything else
                                        .setFlowId(input.getFlowRemoved().getFlowId())
                                        // except the flow ID
                                        .build()
                        )
                        .build()
        );
    }

    private NepheleFlowAttributes extractNepheleResources(OpticalFlowType flow) throws FlowParserException {
        NepheleFlowAttributes output;
        try {
            if (flow instanceof OptOptFlow) {
                output = ((OptOptFlow) flow).getOptOptCase().getOptMatchType().getAugmentation(NepheleFlowPodMatch.class);
            } else if (flow instanceof EthOptFlow) {
                output = ((EthOptFlow) flow).getEthOptCase().getOptOutputType().getAugmentation(NepheleFlowTorOut.class);
            } else if (flow instanceof OptEthFlow) {
                output = ((OptEthFlow) flow).getOptEthCase().getOptMatchType().getAugmentation(NepheleFlowTorMatch.class);
            } else {
                throw new FlowParserException(
                        String.format("Unsupported optical flow type %s.", flow.getImplementedInterface().getSimpleName())
                );
            }
        } catch (NullPointerException exc) {
            throw new FlowParserException("Missing optical flow details.");
        }
        if (null == output) {
            throw new FlowParserException("Nephele resources not specified in the input.");
        }
        return output;
    }

    static OpticalFlowType unwrapInput(OpticalFlowAttributes flow) throws FlowParserException {

        if (null == flow) {
            throw new FlowParserException("Input flow is null");
        }

        OpticalFlowType flowType = flow.getOpticalFlowType();

        if (null == flowType) {
            throw new FlowParserException("Input flow type is null.");
        }
        return flowType;
    }

    static Future<RpcResult<Void>> failWithFPE(String message) {
        return RpcResultBuilder.<Void>failed().withError(
                RpcError.ErrorType.PROTOCOL,
                "Malformed message: " + message)
                .buildFuture();
    }

}
