/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.mock.flowutils;

import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.eth.match.type.attributes.EthMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.opt.match.type.attributes.OptMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.OpticalFlowType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.EthOptFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.OptEthFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.OptOptFlow;

import javax.annotation.Nullable;

public class MatchParser {

    private final VlanProvider provider;

    MatchParser(VlanProvider provider) {
        this.provider = provider;
    }

    public Match createMatch(OpticalFlowType flowType)
            throws FlowParserException, ReadFailedException, OutOfTagsException, TransactionCommitFailedException {
        if (flowType instanceof OptOptFlow)
            return createMatch(((OptOptFlow) flowType).getOptOptCase().getOptMatchType());
        else if (flowType instanceof EthOptFlow)
            return createMatch(((EthOptFlow) flowType).getEthOptCase().getEthMatchType());
        else if (flowType instanceof OptEthFlow)
            return createMatch(((OptEthFlow) flowType).getOptEthCase().getOptMatchType());
        else {
            throw new FlowParserException (
                    String.format("Unexpected match matchType found: %s.", flowType.getImplementedInterface().getSimpleName()));
        }
    }

    private Match createMatch(OptMatchType optMatch)
            throws ReadFailedException, OutOfTagsException, TransactionCommitFailedException {
        String inPort;
        if (null != optMatch.getWport()) {
            inPort = "openflow:" + optMatch.getWport().toString();
        }
        else {
            inPort = null;
        }

        // TODO check getVlan call
        VlanId vLanId = provider.getVLan(optMatch);
        return wrapMatch(inPort, vLanId);
    }

    private Match createMatch(EthMatchType match) {
        return match;
    }

    private static Match wrapMatch(@Nullable String inPort, VlanId vLanId) {
        MatchBuilder match = new MatchBuilder();
        if (null != inPort) {
            match.setInPort(new NodeConnectorId(inPort));
        }
        if (vLanId != null) {
            match.setVlanMatch(new VlanMatchBuilder().setVlanId(vLanId).build());
        }
        return(match.build());
    }
}
