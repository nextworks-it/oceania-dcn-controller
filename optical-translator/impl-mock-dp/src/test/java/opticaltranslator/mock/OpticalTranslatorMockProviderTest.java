/*
 * Nextworks s.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.mock;

import opticaltranslator.core.OpticalTranslatorProvider;
import org.junit.Test;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpticalTranslatorMockProviderTest {

    @Test
    public void testOnSessionInitiated() {
        OpticalTranslatorMockProvider provider = new OpticalTranslatorMockProvider();

        //mocks
        BindingAwareBroker.ProviderContext mockProviderContext = mock(BindingAwareBroker.ProviderContext.class);
        RpcProviderRegistry mockRpcRegistry = mock(RpcProviderRegistry.class);

        //method calls
        when(mockProviderContext.getSALService(RpcProviderRegistry.class)).thenReturn(mockRpcRegistry);

        provider.onSessionInitiated(mockProviderContext);

        //check correct calls have been made on the services
        verify(mockProviderContext).getSALService(RpcProviderRegistry.class);
    }

    @Test
    public void testClose() throws Exception {
        OpticalTranslatorProvider provider = new OpticalTranslatorProvider();

        provider.close();
    }
}
