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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalResourceAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.attributes.optical.flow.type.EthOptFlow;

import java.util.Set;
import java.util.stream.Collectors;

public class VlanProvider {

    private final VLanDataBroker dataBroker;

    VlanProvider(VLanDataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public VlanId getVLan(OpticalResourceAttributes optMatch)
            throws ReadFailedException, OutOfTagsException, TransactionCommitFailedException {
        Integer tag = dataBroker.fetchTag(optMatch);
        return buildVLanId(tag);
    }

    public VlanId getVLan(EthOptFlow flow)
            throws ReadFailedException, OutOfTagsException, TransactionCommitFailedException {
        return getVLan(flow.getEthOptCase().getOptOutputType());
    }

    private VlanId buildVLanId(Integer tag) {
        return new VlanIdBuilder()
                .setVlanId(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId(tag))
                .setVlanIdPresent(true)
                .build();
    }

    Set<VlanId> getDependantVLans(OpticalResourceAttributes optMatch) {
        return dataBroker.dependantTags(optMatch).stream().map(this::buildVLanId).collect(Collectors.toSet());
    }
}
