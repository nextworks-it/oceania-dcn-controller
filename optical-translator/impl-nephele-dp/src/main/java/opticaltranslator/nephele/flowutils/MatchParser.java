/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele.flowutils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.opt.match.type.attributes.OptMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.opt.output.type.attributes.OptOutputType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.OpticalFlowType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.EthOptFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.OptEthFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.OptOptFlow;

public class MatchParser {

    private final MetadataMaker metadataMaker;

    MatchParser(MetadataMaker metadataMaker) {
        this.metadataMaker = metadataMaker;
    }

    public Match createMatch(OpticalFlowType flowType, NepheleFlowAttributes nepheleData) throws FlowParserException {
        if (flowType instanceof OptOptFlow) {
            return createMatchOptOpt((OptOptFlow) flowType, nepheleData);
        }
        else if (flowType instanceof EthOptFlow) {
            return createMatchEthOpt((EthOptFlow) flowType, nepheleData);
        }
        else if (flowType instanceof OptEthFlow) {
            return createMatchOptEth((OptEthFlow) flowType, nepheleData);
        }
        else {
            throw new FlowParserException (
                    String.format("Unexpected match matchType found: %s.", flowType.getImplementedInterface().getSimpleName()));
        }
    }

    private Match createMatchOptOpt(OptOptFlow optOptFlow, NepheleFlowAttributes nepheleData) throws FlowParserException {

        OptMatchType optMatch = optOptFlow.getOptOptCase().getOptMatchType();
        OptOutputType optOutput = optOptFlow.getOptOptCase().getOptOutputType();

        String inPort = "openflow:" + optMatch.getWport().toString();

        Metadata metadata = metadataMaker.buildMetadata(optMatch, optOutput, nepheleData, false);

        return wrapMatch(inPort, metadata);
    }

    // TODO check match-action
    private Match createMatchOptEth(OptEthFlow optEthFlow, NepheleFlowAttributes nepheleData) throws FlowParserException {

        OptMatchType optMatch = optEthFlow.getOptEthCase().getOptMatchType();

        if (null == nepheleData)
            throw new FlowParserException("Nephele data not present, error.");

        String inPort = "openflow:" + optMatch.getWport().toString();

        Metadata metadata = metadataMaker.buildMetadata(optMatch, null, nepheleData, true);

        return wrapMatch(inPort, metadata);
    }

    // TODO check match-action
    private Match createMatchEthOpt(EthOptFlow ethOptFlow, NepheleFlowAttributes nepheleData) throws FlowParserException {

        OptOutputType optOutput = ethOptFlow.getEthOptCase().getOptOutputType();

        if (null == nepheleData)
            throw new FlowParserException("Nephele data not present, error.");

        Metadata metadata = metadataMaker.buildMetadata(null, optOutput, nepheleData, false);

        return wrapMatch(metadata, ethOptFlow.getEthOptCase().getEthMatchType());
    }

    private static Match wrapMatch(String inPort, Metadata metadata) {
        return wrapMatch(inPort, metadata, new MatchBuilder());
    }

    private static Match wrapMatch(Metadata metadata, Match base) {
        return wrapMatch(null, metadata, new MatchBuilder(base));
    }

    private static Match wrapMatch(String inPort, Metadata metadata, MatchBuilder base) {
        if (null != inPort)
            base.setInPort(new NodeConnectorId(inPort));
        if (metadata != null)
            base.setMetadata(metadata);
        return(base.build());
    }
}
