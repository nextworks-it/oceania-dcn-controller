/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.core;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.AddOpticalFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalTranslatorService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.ReloadTranslatorImplementationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.ReloadTranslatorImplementationOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.RemoveOpticalFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.add.opt.flow.input.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.table.attributes.OpticalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.table.attributes.OpticalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.remove.opt.flow.input.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslateAddOpticalFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslateRemoveOpticalFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslatorApiService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class OpticalTranslatorServiceImpl implements OpticalTranslatorService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(OpticalTranslatorServiceImpl.class);

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private final RpcProviderRegistry rpcRegistry;

    private final OpticalTranslatorProvider provider;

    private final BindingAwareBroker.RpcRegistration<OpticalTranslatorService> thisRegistration;

    private TranslatorApiService translator;

    private BindingAwareBroker.RpcRegistration<TranslatorApiService> translatorRegistration;

    OpticalTranslatorServiceImpl(RpcProviderRegistry rpcRegistry, OpticalTranslatorProvider provider) {
        LOG.info("Starting Translator Service initialization.");
        this.rpcRegistry = rpcRegistry;
        this.provider = provider;
        this.translator = new RealTranslator();
        LOG.debug("Registering Optical Translator Service.");
        this.thisRegistration = rpcRegistry.addRpcImplementation(OpticalTranslatorService.class, this);
        LOG.info("Registering real data plane translator.");
        this.translatorRegistration = rpcRegistry.addRpcImplementation(TranslatorApiService.class, translator);
    }

    @Override
    public Future<RpcResult<Void>> activateRealTranslator() {
        LOG.debug("Activating real data plane translator.");
        if (null != getTranslator()) {
            String errorString = String.format("%s is currently loaded. Cannot activate another.",
                    unwrapType(getTranslator()));
            LOG.debug(errorString);
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.PROTOCOL,
                    errorString,
                    new IllegalStateException("Translator implementation already loaded."))
                    .buildFuture();
        }
        try {
            translatorRegistration = rpcRegistry.addRpcImplementation(TranslatorApiService.class,
                    new RealTranslator()
            );
            LOG.info("Real data plane translator activated.");
            return RpcResultBuilder.<Void>success().buildFuture();
        } catch (Exception exc) {
            return RpcResultBuilder.<Void>failed().withError(RpcError.ErrorType.APPLICATION,
                    "Could not activate real data plane translator.", exc).buildFuture();
        }
    }

    @Override
    public Future<RpcResult<Void>> deactivateRealTranslator() {
        LOG.debug("Deactivating real data plane translator.");
        try {
            if (null != translatorRegistration)
                translatorRegistration.close();
            LOG.info("Real data plane translator deactivated.");
            return RpcResultBuilder.<Void>success().buildFuture();
        } catch (Exception exc) {
            return RpcResultBuilder.<Void>failed().withError(RpcError.ErrorType.APPLICATION,
                    "Could not deactivate real data plane translator.", exc).buildFuture();
        }
    }

    @Override
    public Future<RpcResult<Void>> addOpticalFlow(AddOpticalFlowInput input) {
        if (null == translator)
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.PROTOCOL,
                    "No translator loaded, cannot translate flows.",
                    new NullPointerException("No translator implementation loaded.")
            )
                    .buildFuture();
        InstanceIdentifier<?> tentativeNodeRef = input.getNodeRef();
        FlowAdded flow = input.getFlowAdded();
        if (null == flow)
            return RpcResultBuilder.<Void>failed().withError(RpcError.ErrorType.PROTOCOL,
                    "invalid-value",
                    "Null flow received."
            )
                    .buildFuture();
        if (null == tentativeNodeRef)
            return RpcResultBuilder.<Void>failed().withError(RpcError.ErrorType.PROTOCOL,
                    "invalid-value",
                    "Null node reference received."
            )
                    .buildFuture();
        if (!tentativeNodeRef.getTargetType().equals(Node.class))
            return RpcResultBuilder.<Void>failed().withError(RpcError.ErrorType.PROTOCOL,
                    "invalid-value",
                    "Received node-ref did not identify an optical flow node."
            )
                    .buildFuture();

        @SuppressWarnings("unchecked")
        InstanceIdentifier<Node> nodeRef = (InstanceIdentifier<Node>) tentativeNodeRef;

        return addCallback(
                translator.translateAddOpticalFlow(new TranslateAddOpticalFlowInputBuilder(input).build()),
                DataObjectModification.ModificationType.WRITE,
                new OpticalFlowBuilder(flow).build(),
                nodeRef
        );
    }

    @Override
    public Future<RpcResult<Void>> removeOpticalFlow(RemoveOpticalFlowInput input) {
        if (null == translator)
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.PROTOCOL,
                    "No translator loaded, cannot translate flows.",
                    new NullPointerException("No translator implementation loaded.")
            )
                    .buildFuture();
        InstanceIdentifier<?> tentativeNodeRef = input.getNodeRef();
        FlowRemoved flow = input.getFlowRemoved();
        if (null == flow)
            return RpcResultBuilder.<Void>failed().withError(RpcError.ErrorType.PROTOCOL,
                    "invalid-value",
                    "Null flow received."
            )
                    .buildFuture();
        if (null == tentativeNodeRef)
            return RpcResultBuilder.<Void>failed().withError(RpcError.ErrorType.PROTOCOL,
                    "invalid-value",
                    "Null node reference received."
            )
                    .buildFuture();
        if (!tentativeNodeRef.getTargetType().equals(Node.class))
            return RpcResultBuilder.<Void>failed().withError(RpcError.ErrorType.PROTOCOL,
                    "invalid-value",
                    "Received node-ref did not identify an optical flow node."
            )
                    .buildFuture();

        @SuppressWarnings("unchecked")
        InstanceIdentifier<Node> nodeRef = (InstanceIdentifier<Node>) tentativeNodeRef;

        Future<RpcResult<Void>> future =
                translator.translateRemoveOpticalFlow(new TranslateRemoveOpticalFlowInputBuilder(input).build());
        addCallback(
                future,
                DataObjectModification.ModificationType.DELETE,
                new OpticalFlowBuilder(flow).build(),
                nodeRef
        );
        return future;
    }

    @Override
    public Future<RpcResult<ReloadTranslatorImplementationOutput>> reloadTranslatorImplementation() {
        LOG.debug("Reloading translator implementation.");
        try {
            translator = getTranslator();
            if (null == translator)
                LOG.info("No translator enabled.");
            else
                LOG.info("Active translator is {}.", unwrapType(translator));

            return RpcResultBuilder.success(
                    new ReloadTranslatorImplementationOutputBuilder()
                            .setTranslatorType(unwrapType(translator))
            ).buildFuture();
        } catch (Exception exc) {
            return RpcResultBuilder.<ReloadTranslatorImplementationOutput>failed().withError(
                    RpcError.ErrorType.APPLICATION,
                    "Couldn't load translator implementation.",
                    exc)
                    .buildFuture();
        }
    }

    private TranslatorApiService getTranslator() {
        TranslatorApiService t = rpcRegistry.getRpcService(TranslatorApiService.class);
        try {
            t.getTranslatorType().get();
            return t;
        } catch (Exception e) {
            LOG.debug("No translator service found. Exception {}: {}.", e.getClass().getSimpleName(), e.getMessage());
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

    private Future<RpcResult<Void>> addCallback(Future<RpcResult<Void>> future,
                                                DataObjectModification.ModificationType modType,
                                                OpticalFlow flow,
                                                InstanceIdentifier<Node> nodeRef) {

        ListenableFuture<RpcResult<Void>> listenable = JdkFutureAdapters.listenInPoolThread(future, executor);
        FutureCallback<RpcResult<Void>> callback = new FutureCallback<RpcResult<Void>>() {
            @Override
            public void onSuccess(@Nullable RpcResult<Void> voidRpcResult) {
                LOG.info("{} of flow {} successful, updating data tree.", modType, flow.getFlowId().getValue());
                provider.updateDataTree(flow, nodeRef, modType);
            }

            @Override
            public void onFailure(@Nonnull Throwable throwable) {
                LOG.error("{} for flow {} failed with exception ", modType, flow.getFlowId().getValue(), throwable);
            }
        };

        Futures.addCallback(listenable, callback);
        return future;
    }
}
