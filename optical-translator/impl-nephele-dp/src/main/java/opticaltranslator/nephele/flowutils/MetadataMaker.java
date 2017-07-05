package opticaltranslator.nephele.flowutils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalResourceAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Marco Capitani on 03/07/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
interface MetadataMaker {

    @Nonnull
    public Metadata buildMetadata(@Nullable OpticalResourceAttributes optMatch,
                           @Nullable OpticalResourceAttributes optOutput,
                           @Nonnull NepheleFlowAttributes nepheleData,
                           boolean matchActionBit)
            throws FlowParserException;
}
