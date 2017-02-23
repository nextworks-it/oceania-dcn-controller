/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.mock.flowutils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;

import java.math.BigInteger;
import java.util.List;

public class FlowAssembler {

    private final TranslatedInstructionBuilder instructionBuilder;

    FlowAssembler(TranslatedInstructionBuilder instructionBuilder) {
        this.instructionBuilder = instructionBuilder;
    }

    public AddFlowInput createAddFlow(Match match, String outPort, NodeRef nodeRef)
            throws FlowParserException {
        return createAddFlow(match, outPort, nodeRef, VLanTagAction.NO_ACTION, null);
    }

    public AddFlowInput createAddFlowPopping(Match match, String outPort, NodeRef nodeRef)
            throws FlowParserException {
        return createAddFlow(match, outPort, nodeRef, VLanTagAction.POP, null);
    }

    public AddFlowInput createAddFlowPushing(Match match, String outPort, NodeRef nodeRef, VlanId tag)
            throws FlowParserException {
        return createAddFlow(match, outPort, nodeRef, VLanTagAction.PUSH, tag);
    }

    public RemoveFlowInput createRemoveFlow(Match match, String outPort, NodeRef nodeRef)
            throws FlowParserException {
        return createRemoveFlow(match, outPort, nodeRef, VLanTagAction.NO_ACTION, null);
    }

    public RemoveFlowInput createRemoveFlowPopping(Match match, String outPort, NodeRef nodeRef)
            throws FlowParserException {
        return createRemoveFlow(match, outPort, nodeRef, VLanTagAction.POP, null);
    }

    public RemoveFlowInput createRemoveFlowPushing(Match match, String outPort, NodeRef nodeRef, VlanId tag)
            throws FlowParserException {
        return createRemoveFlow(match, outPort, nodeRef, VLanTagAction.PUSH, tag);
    }


    private AddFlowInput createAddFlow(Match match, String outPort, NodeRef nodeRef,
                                      VLanTagAction action, VlanId tag)
            throws FlowParserException {
        //Constant fields
        AddFlowInputBuilder flowBuilder = new AddFlowInputBuilder()
                .setIdleTimeout(0)
                .setHardTimeout(0)
                .setCookie(new FlowCookie(new BigInteger("0")))
                .setTableId((short) 0)
                .setPriority(30000);

        flowBuilder.setMatch(new MatchBuilder(match).build());

        List<Instruction> instructionList = instructionBuilder.makeInstructions(outPort, action, tag);

        flowBuilder.setInstructions(new InstructionsBuilder().setInstruction(instructionList).build());

        //Set Node
        flowBuilder.setNode(nodeRef);

        return flowBuilder.build();
    }

    private RemoveFlowInput createRemoveFlow(Match match, String outPort, NodeRef nodeRef,
                                            VLanTagAction action, VlanId tag)
            throws FlowParserException {
        //Constant fields
        RemoveFlowInputBuilder flowBuilder = new RemoveFlowInputBuilder()
                .setIdleTimeout(0)
                .setHardTimeout(0)
                .setCookie(new FlowCookie(new BigInteger("0")))
                .setTableId((short) 0)
                .setPriority(30000);

        flowBuilder.setMatch(new MatchBuilder(match).build());

        List<Instruction> instructionList = instructionBuilder.makeInstructions(outPort, action, tag);

        flowBuilder.setInstructions(new InstructionsBuilder().setInstruction(instructionList).build());

        //Set Node
        flowBuilder.setNode(nodeRef);

        return flowBuilder.build();
    }
}
