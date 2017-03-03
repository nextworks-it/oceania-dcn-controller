/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele.mock;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.mock.rev161228.OpticalTranslatorNepheleMockService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslatorApiService;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MockServiceImpl implements OpticalTranslatorNepheleMockService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MockServiceImpl.class);

    private final SalFlowService flowService;

    private final RpcProviderRegistry rpcRegistry;

    private final DataBroker dataBroker;

    private final BindingAwareBroker.RpcRegistration<OpticalTranslatorNepheleMockService> thisRegistration;

    private BindingAwareBroker.RpcRegistration<TranslatorApiService> translatorRegistration;

    MockServiceImpl(SalFlowService flowService, RpcProviderRegistry rpcRegistry, DataBroker dataBroker) {
        LOG.debug("Initializing nephele-mock translator service.");
        this.flowService = flowService;
        this.rpcRegistry = rpcRegistry;
        this.dataBroker = dataBroker;
        thisRegistration = rpcRegistry.addRpcImplementation(OpticalTranslatorNepheleMockService.class, this);
        LOG.info("Nephele-mock translator service initialized.");
    }

    private TranslatorApiService buildTranslator(DataBroker dataBroker) {
        return MockTranslator.newMockTranslator(flowService, dataBroker);
    }

    @Override
    public Future<RpcResult<Void>> activateNepheleMockTranslator() {
        LOG.debug("Activating nephele-mock translator");
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
            translatorRegistration =
                    rpcRegistry.addRpcImplementation(TranslatorApiService.class, buildTranslator(dataBroker));
            LOG.info("Nephele-mock translator activated.");
            return RpcResultBuilder.<Void>success().buildFuture();
        } catch (Exception exc) {
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.APPLICATION,
                    "Could not activated nephele-mock translator.",
                    exc
            )
                    .buildFuture();
        }
    }

    @Override
    public Future<RpcResult<Void>> deactivateNepheleMockTranslator() {
        LOG.debug("Deactivating nephele-mock translator");
        try {
            if (null != translatorRegistration)
                translatorRegistration.close();
            LOG.info("Nephele-mock translator deactivated.");
            return RpcResultBuilder.<Void>success().buildFuture();
        } catch (Exception exc) {
            return RpcResultBuilder.<Void>failed().withError(
                    RpcError.ErrorType.APPLICATION,
                    "Could not turn off nephele-mock translator.",
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
