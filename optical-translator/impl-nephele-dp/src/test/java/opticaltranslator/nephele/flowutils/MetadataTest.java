/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele.flowutils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowTorOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalResourceAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.opt.match.type.attributes.OptMatchTypeBuilder;

import java.math.BigInteger;

public class MetadataTest {

    private MetadataMaker maker;

    @Before
    public void setup() {
        maker = new MetadataMaker();
    }

    private OpticalResourceAttributes makeResources(BigInteger wavelength, String timeslot, Integer port) {
        return new OptMatchTypeBuilder()
                .setWavelength(wavelength)
                .setTimeslots(timeslot)
                .setWport(port)
                .build();
    }

    private NepheleFlowAttributes makeNepheleResources(Short schedule, Short flowCounter) {
        return new NepheleFlowTorOutBuilder()
                .setScheduleId(schedule)
                .setFlowCounter(flowCounter)
                .build();
    }

    private Short fromBinaryString(String str) {
        return Short.valueOf(str, 2);
    }

    @Test
    public void testAll1() throws FlowParserException{
        OpticalResourceAttributes match = makeResources(
                new BigInteger("11111111", 2),
                "11111111111111111111111111111111111111111111111111111111111111111111111111111111",
                2
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger("11111111", 2),
                "11111111111111111111111111111111111111111111111111111111111111111111111111111111",
                1
        );
        NepheleFlowAttributes nepheleResources = makeNepheleResources(
                fromBinaryString("11111111"),
                fromBinaryString("11111111")
        );
        Metadata metadata = maker.buildMetadata(match, output, nepheleResources, true);
        Assert.assertEquals(new BigInteger("103845937170696552", 10), metadata.getMetadata());
        Assert.assertEquals(new BigInteger("57060992658440191", 10), metadata.getMetadataMask());
    }

    @Test
    public void testAll0() throws FlowParserException{
        OpticalResourceAttributes match = makeResources(
                new BigInteger("00000000", 2),
                "00000000000000000000000000000000000000000000000000000000000000000000000000000000",
                2
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger("00000000", 2),
                "00000000000000000000000000000000000000000000000000000000000000000000000000000000",
                1
        );
        NepheleFlowAttributes nepheleResources = makeNepheleResources(
                fromBinaryString("00000000"),
                fromBinaryString("00000000")
        );
        Metadata metadata = maker.buildMetadata(match, output, nepheleResources, false);
        Assert.assertEquals(new BigInteger("0", 10), metadata.getMetadata());
        Assert.assertEquals(new BigInteger("0", 10), metadata.getMetadataMask());
    }

    @Test
    public void testJustAction() throws FlowParserException{
        OpticalResourceAttributes match = makeResources(
                new BigInteger("00000000", 2),
                "00000000000000000000000000000000000000000000000000000000000000000000000000000000",
                2
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger("00000000", 2),
                "00000000000000000000000000000000000000000000000000000000000000000000000000000000",
                1
        );
        NepheleFlowAttributes nepheleResources = makeNepheleResources(
                fromBinaryString("00000000"),
                fromBinaryString("00000000")
        );
        Metadata metadata = maker.buildMetadata(match, output, nepheleResources, true);
        Assert.assertEquals(new BigInteger("1", 10), metadata.getMetadata());
        Assert.assertEquals(new BigInteger("0", 10), metadata.getMetadataMask());
    }

    @Test
    public void test0Metadata() throws FlowParserException{
        OpticalResourceAttributes match = makeResources(
                new BigInteger("11111111", 2),
                "00000000000000000000000000000000000000000000000000000000000000001111111111111111",
                2
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger("11111111", 2),
                "00000000000000000000000000000000000000000000000000000000000000001111111111111111",
                1
        );
        NepheleFlowAttributes nepheleResources = makeNepheleResources(
                fromBinaryString("11111111"),
                fromBinaryString("11111111")
        );
        Metadata metadata = maker.buildMetadata(match, output, nepheleResources, true);
        Assert.assertEquals(new BigInteger("562949953421311", 10), metadata.getMetadata());
        Assert.assertEquals(new BigInteger("0", 10), metadata.getMetadataMask());
    }

    @Test
    public void test19digits() throws FlowParserException{
        OpticalResourceAttributes match = makeResources(
                new BigInteger("11010011", 2),
                "00000000000000000000000000000000000000000000000000000110111100000101101101011001",
                2
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger("00000000", 2),
                "00000000000000000000000000000000000000000000000000000110111100000101101101011001",
                1
        );
        NepheleFlowAttributes nepheleResources = makeNepheleResources(
                fromBinaryString("10110010"),
                fromBinaryString("00000000")
        );
        Metadata metadata = maker.buildMetadata(match, output, nepheleResources, false);
        Assert.assertEquals(new BigInteger("100000000000000000", 10), metadata.getMetadata());
        Assert.assertEquals(new BigInteger("0", 10), metadata.getMetadataMask());
    }

    // TODO: should not work like this. See what they say at GWDG
    @Test
    public void test18digits() throws FlowParserException{
        OpticalResourceAttributes match = makeResources(
                new BigInteger("11010011", 2),
                "00000000000000000000000000000000000000000000000000000110111100000101101101011001",
                2
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger("11111111", 2),
                "00000000000000000000000000000000000000000000000000000110111100000101101101011001",
                1
        );
        NepheleFlowAttributes nepheleResources = makeNepheleResources(
                fromBinaryString("10110001"),
                fromBinaryString("11111111")
        );
        Metadata metadata = maker.buildMetadata(match, output, nepheleResources, true);
        Assert.assertEquals(new BigInteger("999999999999999999", 10), metadata.getMetadata());
        Assert.assertEquals(new BigInteger("0", 10), metadata.getMetadataMask()); // Should not be 0 but absent
    }
}
