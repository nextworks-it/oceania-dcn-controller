/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele.mock.flowutils;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MatchParser {

    private static final Logger LOG = LoggerFactory.getLogger(MatchParser.class);

    private final VlanProvider provider;

    MatchParser(VlanProvider provider) {
        this.provider = provider;
    }

    public Set<Match> createMatch(OpticalFlowType flowType)
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

    private Set<Match> createMatch(OptMatchType optMatch)
            throws ReadFailedException, OutOfTagsException, TransactionCommitFailedException {
        String inPort;
        if (null != optMatch.getWport()) {
            inPort = "openflow:" + optMatch.getWport().toString();
        }
        else {
            inPort = null;
        }

        VlanId vLanId = provider.getVLan(optMatch);
        Set<VlanId> dependants = provider.getDependantVLans(optMatch);
        if (dependants.removeIf(Objects::isNull)) {
            LOG.warn("Got null tag as dependant to tag '{}'.", vLanId.getVlanId().getValue());
        }
        LOG.trace("Tag '{}' has dependant tags {}.", vLanId.getVlanId().getValue(),
                dependants.stream().map((t) -> t.getVlanId().getValue()).collect(Collectors.toList()));
        dependants.add(vLanId); // add the main one too
        return dependants.stream().map((tag) -> wrapMatch(inPort, tag)).collect(Collectors.toSet());
    }

    private Set<Match> createMatch(EthMatchType match) {
        return Collections.singleton(match);
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
