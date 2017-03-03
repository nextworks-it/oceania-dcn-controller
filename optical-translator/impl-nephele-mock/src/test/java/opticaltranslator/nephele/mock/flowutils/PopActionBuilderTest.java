/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele.mock.flowutils;

import opticaltranslator.nephele.mock.flowutils.FlowParserException;
import opticaltranslator.nephele.mock.flowutils.TranslatedActionBuilder;
import opticaltranslator.nephele.mock.flowutils.VLanTagAction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;

import java.util.List;

import static org.mockito.Mockito.mock;

public class PopActionBuilderTest {

    private TranslatedActionBuilder builder;

    @Before
    public void setUp() {
        builder = new TranslatedActionBuilder();
    }

    @Test
    public void isSize1List() throws FlowParserException {
        List<Action> actionList = builder.makeOutAction("test", VLanTagAction.POP, mock(VlanId.class));
        Assert.assertEquals(2, actionList.size());
    }

    @Test
    public void correctActionType() throws FlowParserException {
        List<Action> actionList = builder.makeOutAction("test", VLanTagAction.POP, mock(VlanId.class));
        Assert.assertTrue(actionList.get(0).getAction() instanceof PopVlanActionCase);
        Assert.assertEquals(0, (int) actionList.get(0).getOrder());
        Assert.assertTrue(actionList.get(1).getAction() instanceof OutputActionCase);
        Assert.assertEquals(1, (int) actionList.get(1).getOrder());
    }

    @Test
    public void outPortTest() throws FlowParserException {
        List<Action> actionList = builder.makeOutAction("test", VLanTagAction.POP, mock(VlanId.class));
        OutputActionCase outAction = (OutputActionCase) actionList.get(1).getAction();
        Assert.assertEquals("test", outAction.getOutputAction().getOutputNodeConnector().getValue());

        List<Action> actionList2 = builder.makeOutAction("12", VLanTagAction.POP, mock(VlanId.class));
        OutputActionCase outAction2 = (OutputActionCase) actionList2.get(1).getAction();
        Assert.assertEquals("12", outAction2.getOutputAction().getOutputNodeConnector().getValue());
    }
}
