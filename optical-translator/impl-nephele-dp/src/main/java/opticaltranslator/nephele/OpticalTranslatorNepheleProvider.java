/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele;

import opticaltranslator.nephele.flowutils.UtilsFactory;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalTranslatorService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.ReloadTranslatorImplementationOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class OpticalTranslatorNepheleProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(OpticalTranslatorNepheleProvider.class);

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private NepheleServiceImpl service;

    public OpticalTranslatorNepheleProvider() {
        // No-op
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        RpcProviderRegistry rpcRegistry = session.getSALService(RpcProviderRegistry.class);

        service = new NepheleServiceImpl(
                session.getRpcService(SalFlowService.class),
                rpcRegistry,
                UtilsFactory.newMatchParser(UtilsFactory.newMetadataMaker()),
                UtilsFactory.newFlowAssembler(),
                UtilsFactory.newPortFetcher(),
                UtilsFactory.newFutureSwapper()
        );

        OpticalTranslatorService translatorService = rpcRegistry.getRpcService(OpticalTranslatorService.class);
        if (null == translatorService) {
            LOG.error("Could not retrieve OpticalTranslatorService from RpcProviderRegistry." +
                    "Please manually load NEPEHLE translator.");
            return;
        }

        Future<RpcResult<Void>> firstStep = translatorService.deactivateRealTranslator();
        Future<RpcResult<Void>> secondStep = addCallback(
                firstStep,
                (v) -> {
                    try {
                        return service.activateNepheleTranslator().get();
                    } catch (InterruptedException | ExecutionException exc) {
                        LOG.error("Could not activate Nephele translator: got exception {}: {}.",
                                exc.getClass().getSimpleName(), exc.getMessage());
                        throw new RuntimeException(exc);
                        // Gets wrapped into a Future by the executor
                    }
                }
        );
        Future<RpcResult<ReloadTranslatorImplementationOutput>> thirdStep = addCallback(
                secondStep,
                (v) -> {
                    try {
                        return translatorService.reloadTranslatorImplementation().get();
                    } catch (InterruptedException | ExecutionException exc) {
                        LOG.error("Could not load Nephele translator: got exception {}: {}.",
                                exc.getClass().getSimpleName(), exc.getMessage());
                        throw new RuntimeException(exc);
                        // Gets wrapped into a Future by the executor
                    }
                }
        );
        RpcResult<ReloadTranslatorImplementationOutput> result;
        try {
            result = thirdStep.get();
        } catch (InterruptedException | ExecutionException exc) {
            LOG.error("Could not load Nephele translator: got exception {}: {}.",
                    exc.getClass().getSimpleName(), exc.getMessage());
            LOG.debug("Exception details: ", exc);
            LOG.error("Please, load NEPHELE translator manually.");
            return;
        }
        if (!result.isSuccessful() || !result.getResult().getTranslatorType().equals(NepheleTranslator.NAME)) {
            LOG.error("NEPHELE translator loading failed unexpectedly. Please load manually.");
        } else {
            LOG.info("NEPHELE translator successfully loaded.");
        }

        LOG.info("OpticalTranslatorNepheleProvider Session Initiated");
    }

    @Nonnull
    private <V, R> Future<R> addCallback(@Nonnull Future<V> inFuture, @Nonnull Function<V, R> callback) {
        return executor.submit(() -> syncAddCallback(inFuture, callback));
    }

    @Nullable
    private <V, R> R syncAddCallback(@Nonnull Future<V> inFuture, @Nonnull Function<V, R> callback)
            throws InterruptedException, ExecutionException {
        V intermediate = inFuture.get();
        return callback.apply(intermediate);
    }

    @Override
    public void close() throws Exception {
        if (null != service)
            service.close();
        LOG.info("OpticalTranslatorNepheleProvider Closed");
    }

}
