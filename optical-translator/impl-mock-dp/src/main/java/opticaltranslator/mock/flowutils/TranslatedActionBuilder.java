/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.mock.flowutils;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;

import java.util.ArrayList;
import java.util.List;

class TranslatedActionBuilder {

    List<Action> makeOutAction(String outPort, VLanTagAction tagAction, VlanId tag) throws FlowParserException {
        int order = 0;
        List<Action> actionList = new ArrayList<>();
        switch (tagAction) {
            case PUSH:
                actionList.add(
                        makeAction(order,
                                new PushVlanActionCaseBuilder()
                                        .setPushVlanAction(makePushVlanAction())
                                        .build()
                        )
                );
                order = order + 1;
                actionList.add(
                        makeAction(order,
                                new SetFieldCaseBuilder()
                                        .setSetField(makeSetFieldAction(tag))
                                        .build()
                        )
                );
                order = order + 1;
                break;
            case POP:
                actionList.add(
                        makeAction(order,
                                new PopVlanActionCaseBuilder()
                                        .setPopVlanAction(
                                                new PopVlanActionBuilder()
                                                        .build()
                                        )
                                        .build()
                        )
                );
                order = order + 1;
                break;
            case NO_ACTION:
                //No-op: the default output action is enough.
                break;
            default:
                throw new FlowParserException(
                        String.format("Unsupported VLAN related action: %s", tagAction)
                );
        }
        actionList.add(
                new ActionBuilder()
                        .setOrder(order)
                        .setAction(new OutputActionCaseBuilder()
                                .setOutputAction(makeOutputAction(outPort))
                                .build())
                        .build()
        );
        return actionList;
    }

    private Action makeAction(int order,
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action
    ) {
        return new ActionBuilder()
                .setOrder(order)
                .setAction(action)
                .build();
    }

    private PushVlanAction makePushVlanAction() {
        return new PushVlanActionBuilder()
                .setEthernetType(33024)
                .build();
    }
    private SetField makeSetFieldAction(VlanId tag) {
        return new SetFieldBuilder()
                .setVlanMatch(
                        new VlanMatchBuilder()
                                .setVlanId(tag)
                                .setVlanPcp(new VlanPcp((short) 0))
                                .build()
                )
                .build();
    }

    private OutputAction makeOutputAction(String outPort) {
        return new OutputActionBuilder()
                .setMaxLength(65509)
                .setOutputNodeConnector(new Uri(outPort))
                .build();
    }
}
