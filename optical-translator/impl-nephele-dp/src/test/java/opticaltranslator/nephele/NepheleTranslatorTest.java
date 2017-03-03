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
import opticaltranslator.nephele.flowutils.OutOfTagsException;
import opticaltranslator.nephele.flowutils.PortFetcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowTorMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.OpticalTranslatorNepheleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalFlowAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalTranslatorService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.add.opt.flow.input.FlowAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.eth.output.type.attributes.EthOutputType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.opt.match.type.attributes.OptMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.opt.match.type.attributes.OptMatchTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.OptEthFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.OptEthFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.opt.eth.flow.OptEthCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.opt.eth.flow.OptEthCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.remove.opt.flow.input.FlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslateAddOpticalFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslateAddOpticalFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslateRemoveOpticalFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslateRemoveOpticalFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslatorApiService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NepheleTranslatorTest {

    private TranslatorApiService translator;

    private TranslateAddOpticalFlowInput addInput;
    private TranslateRemoveOpticalFlowInput remInput;
    private OpticalFlowAttributes flow;

    @Mock
    private SalFlowService flowService;

    @Mock
    private MatchParser parser;
    @Mock
    private FlowAssembler flowAssembler;
    @Mock
    private PortFetcher fetcher;
    @Mock
    private FutureTypeSwapper swapper;
    @Mock
    private RpcProviderRegistry rpcRegistry;

    private OpticalTranslatorNepheleService nepheleService;

    @Before
    public void setUp() {
        when(rpcRegistry.getRpcService(OpticalTranslatorService.class))
                .thenReturn(mock(OpticalTranslatorService.class));
        nepheleService = new NepheleServiceImpl(flowService, rpcRegistry, parser, flowAssembler, fetcher, swapper);
        translator = new NepheleTranslator(nepheleService);

        OptMatchType optMatchType = mock(OptMatchType.class);
        when(optMatchType.getAugmentation(NepheleFlowTorMatch.class)).thenReturn(mock(NepheleFlowTorMatch.class));

        OptEthCase optEthCase = new OptEthCaseBuilder()
                .setEthOutputType(mock(EthOutputType.class))
                .setOptMatchType(optMatchType)
                .build();


        flow = new FlowAddedBuilder()
                .setFlowId(new FlowId("test"))
                .setOpticalFlowType(new OptEthFlowBuilder()
                        .setOptEthCase(optEthCase)
                        .build()
                )
                .build();

        addInput = new TranslateAddOpticalFlowInputBuilder()
                        .setNodeRef(mock(InstanceIdentifier.class))
                        .setFlowAdded(new FlowAddedBuilder(flow).build())
                        .build();

        remInput = new TranslateRemoveOpticalFlowInputBuilder()
                .setNodeRef(mock(InstanceIdentifier.class))
                .setFlowRemoved(new FlowRemovedBuilder(flow).build())
                .build();
    }

    @Test
    public void addGoodFlow()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException, ReadFailedException, OutOfTagsException, FlowParserException {

        InOrder inOrder = inOrder(parser, flowAssembler, flowService, swapper);

        when(swapper.generalizeFuture(any())).thenReturn(RpcResultBuilder.<Void>success().buildFuture());

        Future<RpcResult<Void>> response = translator.translateAddOpticalFlow(addInput);

        if (!response.get().isSuccessful()) {
            Assert.fail(response.get().getErrors().toString());
        }

        inOrder.verify(parser).createMatch(any(), any());
        inOrder.verify(flowAssembler).createAddFlow(any(), any(), any());
        inOrder.verify(flowService).addFlow(any(AddFlowInput.class));
        inOrder.verify(swapper).generalizeFuture(any());
    }

    @Test
    public void removeGoodFlow()
            throws InterruptedException, ExecutionException, TransactionCommitFailedException, ReadFailedException, OutOfTagsException, FlowParserException {

        InOrder inOrder = inOrder(parser, flowAssembler, flowService, swapper);

        when(swapper.generalizeFuture(any())).thenReturn(RpcResultBuilder.<Void>success().buildFuture());

        Future<RpcResult<Void>> response = translator.translateRemoveOpticalFlow(remInput);

        if (!response.get().isSuccessful()) {
            Assert.fail(response.get().getErrors().toString());
        }

        inOrder.verify(parser).createMatch(any(), any());
        inOrder.verify(flowAssembler).createRemoveFlow(any(), any(), any());
        inOrder.verify(flowService).removeFlow(any(RemoveFlowInput.class));
        inOrder.verify(swapper).generalizeFuture(any());
    }

    @Test
    public void getType() throws InterruptedException, ExecutionException {
        Assert.assertEquals("Nephele data plane translator",
                translator.getTranslatorType().get().getResult().getTranslatorType());
    }
}
