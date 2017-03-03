/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele.mock.flowutils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.eth.output.type.attributes.EthOutputType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.opt.output.type.attributes.OptOutputType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.OpticalFlowType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.EthOptFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.OptEthFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.OptOptFlow;

public class PortFetcher {

    PortFetcher() {}

    public String extractOutPort(OpticalFlowType flowType) throws FlowParserException {
        if (flowType instanceof OptOptFlow) {
            return extractPort(((OptOptFlow) flowType).getOptOptCase().getOptOutputType());
        }
        else if (flowType instanceof EthOptFlow) {
            return extractPort(((EthOptFlow) flowType).getEthOptCase().getOptOutputType());
        }
        else if (flowType instanceof OptEthFlow) {
            return extractPort(((OptEthFlow) flowType).getOptEthCase().getEthOutputType());
        }
        else {
            throw new FlowParserException(
                    String.format("Unexpected output type found: %s.", flowType.getImplementedInterface().getSimpleName()));
        }
    }

    private String extractPort(OptOutputType out) {
        return out.getWport().toString();
    }

    private String extractPort(EthOutputType out) {
        return out.getOutputNodeConnector().getValue();
    }
}
