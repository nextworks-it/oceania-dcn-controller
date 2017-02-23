/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.core;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.AddOpticalFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalTranslatorService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.RemoveOpticalFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.add.opt.flow.input.FlowAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.optical.flow.table.attributes.OpticalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.remove.opt.flow.input.FlowRemovedBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

class TranslatorForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(TranslatorForwarder.class);

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private OpticalTranslatorService translator;

    TranslatorForwarder(OpticalTranslatorService translator) {
        LOG.debug("Translator forwarder initialized.");
        this.translator = translator;
    }

    @Nonnull
    private <T extends DataObject> T unwrapDataAfter(@Nonnull DataObjectModification<T> optFlowMod) {
        Preconditions.checkNotNull(optFlowMod.getDataAfter(),
                "Got null getDataAfter with WRITE modifications." +
                        "Mod: %s, before: %s, after: %s",
                optFlowMod,
                optFlowMod.getDataBefore(), optFlowMod.getDataAfter());
        return optFlowMod.getDataAfter();
    }

    @Nonnull
    private <T extends DataObject> T unwrapDataBefore(@Nonnull DataObjectModification<T> optFlowMod) {
        Preconditions.checkNotNull(optFlowMod.getDataBefore(),
                "Got null getDataBefore with WRITE modifications." +
                        "Mod: %s, before: %s, after: %s",
                optFlowMod,
                optFlowMod.getDataBefore(), optFlowMod.getDataAfter());
        return optFlowMod.getDataBefore();
    }

    void parseFlowModification(DataObjectModification<OpticalFlow> optFlowMod,
                               InstanceIdentifier<Node> nodeRef) {

        switch (optFlowMod.getModificationType()) {

            case WRITE:
                OpticalFlow written = unwrapDataAfter(optFlowMod);
                if (written.equals(optFlowMod.getDataBefore())) {
                    LOG.debug("Misfired WRITE modification.");
                    return;
                }
                LOG.debug("Parsing created flow {}.", written.getFlowId().getValue());
                translator.addOpticalFlow(
                        new AddOpticalFlowInputBuilder()
                                .setFlowAdded(new FlowAddedBuilder(written).build())
                                .setNodeRef(nodeRef)
                                .build()
                );
                break;

            case DELETE:
                OpticalFlow deleted = unwrapDataBefore(optFlowMod);
                if (deleted.equals(optFlowMod.getDataAfter())) {
                    LOG.debug("Misfired DELETE modification.");
                    return;
                }
                LOG.debug("Parsing deleted flow {}.", deleted.getFlowId().getValue());
                translator.removeOpticalFlow(
                        new RemoveOpticalFlowInputBuilder()
                                .setFlowRemoved(new FlowRemovedBuilder(deleted).build())
                                .setNodeRef(nodeRef)
                                .build()
                );
                break;

            case SUBTREE_MODIFIED:
                OpticalFlow oldFlow = unwrapDataBefore(optFlowMod);
                OpticalFlow newFlow = unwrapDataAfter(optFlowMod);
                if (oldFlow.equals(newFlow)) {
                    LOG.debug("Misfired MODIFIED modification.");
                    return;
                }
                LOG.debug("Parsing modified flow {}.", oldFlow.getFlowId().getValue());
                //DELETE the old one, add the new one. It should be a rare case anyway.
                addCallback(
                        translator.removeOpticalFlow(
                                new RemoveOpticalFlowInputBuilder()
                                        .setFlowRemoved(new FlowRemovedBuilder(oldFlow).build())
                                        .setNodeRef(nodeRef)
                                        .build()
                        ),
                        (v) -> translator.addOpticalFlow(
                                new AddOpticalFlowInputBuilder()
                                        .setFlowAdded(new FlowAddedBuilder(newFlow).build())
                                        .setNodeRef(nodeRef)
                                        .build()
                        )
                );
                break;

            default: //Should not happen.
                throw new IllegalArgumentException(
                        String.format("Unexpected modification type %s.",
                                optFlowMod.getModificationType()
                        )
                );
        }
    }

    @Nonnull private <V, R> Future<R> addCallback(@Nonnull Future<V> inFuture, @Nonnull Function<V, R> callback) {
        return executor.submit(() -> syncAddCallback(inFuture, callback));
    }

    @Nullable
    private <V, R> R syncAddCallback(@Nonnull Future<V> inFuture, @Nonnull Function<V, R> callback)
            throws InterruptedException, ExecutionException {
        V intermediate = inFuture.get();
        return callback.apply(intermediate);
    }
}
