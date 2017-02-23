/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.mock.flowutils;

import opticaltranslator.mock.flowutils.FlowParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PushActionBuilderTest {

    private TranslatedActionBuilder builder;

    @Before
    public void setUp() {
        builder = new TranslatedActionBuilder();
    }

    @Test
    public void isSize3List() throws FlowParserException {
        List<Action> actionList = builder.makeOutAction("test", VLanTagAction.PUSH, mock(VlanId.class));
        Assert.assertEquals(3, actionList.size());
    }

    @Test
    public void correctActionTypes() throws FlowParserException {
        List<Action> actionList = builder.makeOutAction("test", VLanTagAction.PUSH, mock(VlanId.class));
        Assert.assertTrue(actionList.get(0).getAction() instanceof PushVlanActionCase);
        Assert.assertEquals(0, (int) actionList.get(0).getOrder());
        Assert.assertTrue(actionList.get(1).getAction() instanceof SetFieldCase);
        Assert.assertEquals(1, (int) actionList.get(1).getOrder());
        Assert.assertTrue(actionList.get(2).getAction() instanceof OutputActionCase);
        Assert.assertEquals(2, (int) actionList.get(2).getOrder());
    }

    //TODO

    @Test
    public void outPortTest() throws FlowParserException {
        List<Action> actionList = builder.makeOutAction("test", VLanTagAction.PUSH, mock(VlanId.class));
        OutputActionCase outAction = (OutputActionCase) actionList.get(2).getAction();
        Assert.assertEquals("test", outAction.getOutputAction().getOutputNodeConnector().getValue());

        List<Action> actionList2 = builder.makeOutAction("12", VLanTagAction.PUSH, mock(VlanId.class));
        OutputActionCase outAction2 = (OutputActionCase) actionList2.get(2).getAction();
        Assert.assertEquals("12", outAction2.getOutputAction().getOutputNodeConnector().getValue());
    }

    @Test
    public void tagTest() throws FlowParserException {
        VlanId tag = mock(VlanId.class);
        when(tag.getVlanId()).thenReturn(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId(12)
        );
        List<Action> actionList = builder.makeOutAction("test", VLanTagAction.PUSH, tag);
        PushVlanActionCase pushAction = (PushVlanActionCase) actionList.get(0).getAction();
        SetFieldCase fieldAction = (SetFieldCase) actionList.get(1).getAction();
        Assert.assertEquals(33024, (int) pushAction.getPushVlanAction().getEthernetType());
        Assert.assertEquals(tag,
                fieldAction.getSetField().getVlanMatch().getVlanId());
        Assert.assertEquals(new VlanPcp((short) 0), fieldAction.getSetField().getVlanMatch().getVlanPcp());
    }
}
