/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele.flowutils;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;

import java.util.ArrayList;
import java.util.List;

class TranslatedActionBuilder {

    List<Action> makeOutAction(String outPort) throws FlowParserException {
        int order = 0;
        List<Action> actionList = new ArrayList<>();
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

    private OutputAction makeOutputAction(String outPort) {
        return new OutputActionBuilder()
                .setMaxLength(65509)
                .setOutputNodeConnector(new Uri(outPort))
                .build();
    }
}
