/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.core;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalFlowNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.node.attributes.OpticalFlowTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.table.attributes.OpticalFlow;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class OpticalTranslatorProvider implements BindingAwareProvider, AutoCloseable,
        DataTreeChangeListener<OpticalFlow> {

    private static final Logger LOG = LoggerFactory.getLogger(OpticalTranslatorProvider.class);

    private DataBroker dataBroker;

    private TranslatorForwarder translatorForwarder;

    private OpticalTranslatorServiceImpl service;

    public OpticalTranslatorProvider() {

    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.debug("Initializing translator provider.");
        dataBroker = session.getSALService(DataBroker.class);
        dataBroker.registerDataTreeChangeListener(
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION,
                        InstanceIdentifier.create(Nodes.class)
                                .child(Node.class)
                                .augmentation(OpticalFlowNode.class)
                                .child(OpticalFlowTable.class)
                                .child(OpticalFlow.class)
                ),
                this
        );

        LOG.debug("Initializing translator service.");
        try {

            service = new OpticalTranslatorServiceImpl(session.getSALService(RpcProviderRegistry.class), this);
            translatorForwarder = new TranslatorForwarder(service);
        } catch (Exception e) {
            LOG.warn("Exception:", e);
        }

        LOG.info("OpticalTranslatorProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        if (null != service)
            service.close();
        LOG.info("OpticalTranslatorProvider Closed");
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<OpticalFlow>> collection) {
        collection.forEach(this::parseModification);
    }

    private void parseModification(DataTreeModification<OpticalFlow> modification) {
        InstanceIdentifier<Node> nodeRef = modification.getRootPath().getRootIdentifier().firstIdentifierOf(Node.class);
        DataObjectModification<OpticalFlow> objModification = modification.getRootNode();
        translatorForwarder.parseFlowModification(objModification, nodeRef);
    }

    void updateDataTree(OpticalFlow flow, InstanceIdentifier<Node> nodeRef, DataObjectModification.ModificationType modType) {
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        switch (modType) {
            case WRITE:
                tx.put(
                        LogicalDatastoreType.OPERATIONAL,
                        nodeRef
                                .augmentation(OpticalFlowNode.class)
                                .child(OpticalFlowTable.class)
                                .child(OpticalFlow.class, flow.getKey()),
                        flow,
                        true
                );
                break;
            case DELETE:
                tx.delete(
                        LogicalDatastoreType.OPERATIONAL,
                        nodeRef
                                .augmentation(OpticalFlowNode.class)
                                .child(OpticalFlowTable.class)
                                .child(OpticalFlow.class, flow.getKey())
                );
                break;
            case SUBTREE_MODIFIED:
            default:
                LOG.error("Unexpected modification type in provider.updateDataTree: {}.", modType);
        }

        FutureCallback<Void> callback = new FutureCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void aVoid) {
                LOG.debug("Successfully committed transaction.");
                tx.cancel();
            }

            @Override
            public void onFailure(@Nonnull Throwable throwable) {
                LOG.error("Transaction failed with exception ", throwable);
                tx.cancel();
            }
        };

        Futures.addCallback(tx.submit(), callback);
    }

}

