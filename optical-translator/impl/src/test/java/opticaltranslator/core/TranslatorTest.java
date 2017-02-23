/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslatorApiService;

import java.util.concurrent.ExecutionException;

public class TranslatorTest {

    private TranslatorApiService translator;

    @Before
    public void setUp() {
        translator = new RealTranslator();
    }

    @Test
    public void addGoodFlow() {
        //TODO
    }

    @Test
    public void removeGoodFlow() {
        //TODO
    }

    @Test
    public void getType() throws InterruptedException, ExecutionException {
        Assert.assertEquals("Real data plane translator",
                translator.getTranslatorType().get().getResult().getTranslatorType());
    }
}
