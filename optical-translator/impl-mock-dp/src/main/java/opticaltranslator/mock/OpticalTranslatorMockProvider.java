/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.mock;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpticalTranslatorMockProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(OpticalTranslatorMockProvider.class);

    private MockServiceImpl service;

    public OpticalTranslatorMockProvider() {
        // No-op
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        service = new MockServiceImpl(
                session.getRpcService(SalFlowService.class),
                session.getSALService(RpcProviderRegistry.class),
                session.getSALService(DataBroker.class)
        );
        LOG.info("OpticalTranslatorMockProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        if (null != service)
            service.close();
        LOG.info("OpticalTranslatorMockProvider Closed");
    }

}
