/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.mock.flowutils;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;

public class UtilsFactory {

    private UtilsFactory() {
        throw new UnsupportedOperationException("Not meant to be instantiated.");
    }

    public static FlowAssembler newFlowAssembler() {
        return new FlowAssembler(new TranslatedInstructionBuilder(new TranslatedActionBuilder()));
    }

    public static FutureTypeSwapper newFutureSwapper() {
        return new FutureTypeSwapper();
    }

    public static MatchParser newMatchParser(VlanProvider vlanProvider) {
        return new MatchParser(vlanProvider);
    }

    public static PortFetcher newPortFetcher() {
        return new PortFetcher();
    }

    public static VlanProvider newVLanProvider(DataBroker dataBroker) {
        return new VlanProvider(newVLanDataBroker(dataBroker));
    }

    private static VLanDataBroker newVLanDataBroker(DataBroker dataBroker) {
        return new VLanDataBroker(dataBroker);
    }

}
