/*
 * Nextworks s.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.impl.rev161228;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.impl.nephele.rev161228.OpticalTranslatorNepheleModuleFactory;

public class OpticalTranslatorNepheleModuleFactoryTest {
    @Test
    public void testFactoryConstructor() {
        // ensure no exceptions on construction
        new OpticalTranslatorNepheleModuleFactory();
    }
}
