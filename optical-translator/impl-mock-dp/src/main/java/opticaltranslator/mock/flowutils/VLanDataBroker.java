/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.mock.flowutils;

import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.mock.rev161228.VlanTagsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.mock.rev161228.vlan.optical.correspondance.OpticalResourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.mock.rev161228.vlan.tags.map.VlanTagAssignment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.mock.rev161228.vlan.tags.map.VlanTagAssignmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.mock.rev161228.vlan.tags.map.VlanTagAssignmentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalResourceAttributes;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

class VLanDataBroker {

    private static List<Integer> generateList() {
        return IntStream.range(1,4096)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /*  TODO: if it is the case, some sophisticated concurrency guards are needed
     *  TODO: in order for this to work on several threads.
     */

    private final DataBroker dataBroker;

    private final Map<ComparableResource, Integer> tagMap;

    private List<Integer> availableTags;

    VLanDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.tagMap = new HashMap<>();
    }

    Integer fetchTag(OpticalResourceAttributes optMatch)
            throws ReadFailedException, TransactionCommitFailedException, OutOfTagsException {

        ComparableResource resource = new ComparableResource(optMatch);

        //Fast check.
        Integer tag = checkCache(resource);
        if (null != tag)
            return tag;

        //Ok, we have to synchronize.
        synchronized (this) {

            //In case the cache has been updated while the thread waited
            tag = checkCache(resource);
            if (null != tag)
                return tag;

            //Update cache
            reloadMapFromDataStore();

            //If it was a recent addition
            tag = checkCache(resource);
            if (null != tag)
                return tag;

            //Ok, it really is not assigned yet.
            return pickNewTag(resource);
        }
    }

    private Integer pickNewTag(ComparableResource optMatch)
            throws TransactionCommitFailedException, OutOfTagsException {
        if (availableTags.isEmpty())
            throw new OutOfTagsException("No more tags available.");
        Integer tag = availableTags.get(0);
        putInDataStore(optMatch,tag);
        return addToCache(optMatch, tag);
    }

    private Integer checkCache(ComparableResource optMatch) {
        return tagMap.get(optMatch);
    }

    private Integer addToCache(ComparableResource optMatch, Integer tag) {
        tagMap.put(optMatch, tag);
        availableTags.remove(tag);
        return tag;
    }

    private void clearCache() {
        tagMap.clear();
        availableTags = generateList();
    }

    private void reloadMapFromDataStore() throws ReadFailedException {

        VlanTagsMap map = fetchDataStore();
        clearCache();
        if (null == map)
            return;

        parseMap(map);
    }

    private void parseMap(VlanTagsMap storeMap) throws ReadFailedException {
        for (VlanTagAssignment pair : storeMap.getVlanTagAssignment()) {
            parsePair(pair);
        }
    }

    private void parsePair(VlanTagAssignment pair) throws ReadFailedException{
        ComparableResource optRes = new ComparableResource(pair.getOpticalResource());
        Long longTag = pair.getVlanTag();
        if (longTag < 0 || longTag > 4095)
            throw new ReadFailedException("The data store contained malformed data.");
        Integer tag = longTag.intValue();
        addToCache(optRes, tag);
    }

    private VlanTagsMap fetchDataStore() throws ReadFailedException  {

        ReadOnlyTransaction rx = dataBroker.newReadOnlyTransaction();
        @SuppressWarnings("Guava")
        Optional<VlanTagsMap> optMap = rx.read(
                LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.create(VlanTagsMap.class)
        ).checkedGet();

        if (!optMap.isPresent())
            return null;
        return optMap.get();
    }

    private void putInDataStore(ComparableResource optMatch, Integer tag) throws TransactionCommitFailedException {
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(
                LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.create(VlanTagsMap.class)
                        .child(VlanTagAssignment.class, new VlanTagAssignmentKey((long) tag)),
                new VlanTagAssignmentBuilder()
                        .setVlanTag((long) tag)
                        .setOpticalResource(new OpticalResourceBuilder(optMatch.toOpticalResource()).build())
                        .build(),
                true
        );
        tx.submit().checkedGet();
        //Don't care about the response, we just want to wait for it to finish to throw exception right now.
    }
}
