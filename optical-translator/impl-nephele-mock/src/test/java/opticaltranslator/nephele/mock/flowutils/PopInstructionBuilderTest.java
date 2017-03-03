/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele.mock.flowutils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PopInstructionBuilderTest {

    private opticaltranslator.nephele.mock.flowutils.TranslatedInstructionBuilder instructionBuilder;

    private List<Instruction> instructionList;

    private List<Action> actionList;

    @Mock
    private TranslatedActionBuilder actionBuilder;

    @Before
    public void setUp() throws FlowParserException {
        instructionBuilder = new TranslatedInstructionBuilder(actionBuilder);
        actionList = Collections.singletonList(mock(Action.class));
        when(actionBuilder.makeOutAction(eq("Port"), eq(VLanTagAction.POP), any(VlanId.class)))
                .thenReturn(actionList);
        instructionList =
                instructionBuilder.makeInstructions("Port", VLanTagAction.POP, mock(VlanId.class));
    }

    @Test
    public void testCall() throws FlowParserException {
        verify(actionBuilder).makeOutAction(eq("Port"), eq(VLanTagAction.POP), any(VlanId.class));
    }
}
