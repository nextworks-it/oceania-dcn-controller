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
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowTorOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalResourceAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.opt.match.type.attributes.OptMatchTypeBuilder;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class MetadataTest {

    private MetadataMaker maker;

    private String revPad(String inString, short size) throws FlowParserException {
        StringBuilder outString = new StringBuilder("");
        outString.append(inString);
        if (inString.length() > size) {
            throw new FlowParserException("field size is greater than " + String.valueOf(size) + ", not supported.");
        }
        else if (inString.length() < size) {
            short missingZeroes = (short) (size - inString.length());
            for (short i = 0; i < missingZeroes; i++) {
                outString.append("0");
            }
        }
        return outString.toString();
    }

    private Map<String, Object> decode(Metadata metadata) {
        Map<String, Object> output = new HashMap<>();
        BigInteger bigDec = new BigInteger(
                metadata.getMetadata().toString() + metadata.getMetadataMask().toString(),
                10
        );
        System.out.println(metadata.getMetadata());
        System.out.println(metadata.getMetadataMask());
        System.out.println(bigDec.toString());

        String big_bin = bigDec.toString(2);
        System.out.println(big_bin);

        String extraBit = big_bin.substring(big_bin.length() - 1);
        big_bin = big_bin.substring(0, big_bin.length() - 1);
        System.out.println(big_bin);

        String oWave = big_bin.substring(big_bin.length() - 8);
        big_bin = big_bin.substring(0, big_bin.length() - 8);
        System.out.println(big_bin);

        String count = big_bin.substring(big_bin.length() - 8);
        big_bin = big_bin.substring(0, big_bin.length() - 8);
        System.out.println(big_bin);

        String schedule = big_bin.substring(big_bin.length() - 8);
        big_bin = big_bin.substring(0, big_bin.length() - 8);
        System.out.println(big_bin);

        String mWave = big_bin.substring(big_bin.length() - 8);
        big_bin = big_bin.substring(0, big_bin.length() - 8);
        System.out.println(big_bin);

        String timeslots = big_bin;
        output.put("extraBit", extraBit.replaceFirst("^0+(?!$)", ""));
        output.put("oWave", oWave.replaceFirst("^0+(?!$)", ""));
        output.put("count", count.replaceFirst("^0+(?!$)", ""));
        output.put("schedule", schedule.replaceFirst("^0+(?!$)", ""));
        output.put("mWave", mWave.replaceFirst("^0+(?!$)", ""));
        output.put("timeslots", timeslots.replaceFirst("^0+(?!$)", ""));
        return output;
    }
    /*
    @Before
    public void setup() {
        maker = new MetadataMakerOld();
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
    public void testRandom() throws FlowParserException{
        OpticalResourceAttributes match = makeResources(
                new BigInteger("11000111", 2),
                "11111000000000111100110011101000000000000000111011011110001111100000111011110011",
                2
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger("11000111", 2),
                "11111000000000111100110011101000000000000000111011011110001111100000111011110011",
                1
        );
        NepheleFlowAttributes nepheleResources = makeNepheleResources(
                fromBinaryString("10101001"),
                fromBinaryString("00111001")
        );
        Metadata metadata = maker.buildMetadata(match, output, nepheleResources, true);
        Assert.assertEquals(new BigInteger("100606773632124500", 10), metadata.getMetadata());
        Assert.assertEquals(new BigInteger("63620968874668943", 10), metadata.getMetadataMask());
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
    public void testWeird() throws FlowParserException{
        OpticalResourceAttributes match = makeResources(
                new BigInteger("252", 10),
                "00000100110011001100110011001100110011001100110011001100110011011110101011011010",
                4
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger("1", 10),
                "00000100110011001100110011001100110011001100110011001100110011011110101011011010",
                1
        );
        NepheleFlowAttributes nepheleResources = makeNepheleResources(
                (short) 137,
                (short) 4
        );
        Metadata metadata = maker.buildMetadata(match, output, nepheleResources, false);
        Assert.assertEquals(new BigInteger("02b3c0be3c4ce9a4", 16), metadata.getMetadata());
        Assert.assertEquals(new BigInteger("00027badbf680802", 16), metadata.getMetadataMask());
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

    @Test
    public void testAllLow() throws FlowParserException{
        for (Integer i = 1; i<30; i++) {
            OpticalResourceAttributes match = makeResources(
                    new BigInteger(i.toString(), 10),
                    revPad(Integer.toBinaryString(i*i), (short) 80),
                    2
            );
            OpticalResourceAttributes output = makeResources(
                    new BigInteger(i.toString(), 10),
                    revPad(Integer.toBinaryString(i*i), (short) 80),
                    1
            );
            NepheleFlowAttributes nepheleResources = makeNepheleResources(
                    fromBinaryString("10110010"),
                    fromBinaryString("00000001")
            );
            Metadata metadata = maker.buildMetadata(match, output, nepheleResources, false);
            Map<String, Object> decoded = decode(metadata);
            try {
                Assert.assertEquals(decoded.get("oWave"), Integer.toBinaryString(i));
                Assert.assertEquals(decoded.get("mWave"), Integer.toBinaryString(i));
                Assert.assertEquals(decoded.get("count"), Integer.toBinaryString(1));
                Assert.assertEquals(decoded.get("schedule"), Integer.toBinaryString(178));
                Assert.assertEquals(decoded.get("timeslots"), revPad(Integer.toBinaryString(i * i), (short) 80));
            } catch (ComparisonFailure e) {
                System.out.println("parameter = " + i);
                throw e;
            }
        }
    }

    @Test
    public void test15() throws FlowParserException {
        Integer i = 15;
        OpticalResourceAttributes match = makeResources(
                new BigInteger(i.toString(), 10),
                revPad(Integer.toBinaryString(i * i), (short) 80),
                2
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger(i.toString(), 10),
                revPad(Integer.toBinaryString(i * i), (short) 80),
                1
        );
        NepheleFlowAttributes nepheleResources = makeNepheleResources(
                fromBinaryString("10110010"),
                fromBinaryString("00000001")
        );
        Metadata metadata = maker.buildMetadata(match, output, nepheleResources, false);
        Map<String, Object> decoded = decode(metadata);
        try {
            Assert.assertEquals(decoded.get("oWave"), Integer.toBinaryString(i));
            Assert.assertEquals(decoded.get("count"), Integer.toBinaryString(1));
            Assert.assertEquals(decoded.get("schedule"), Integer.toBinaryString(178));
            Assert.assertEquals(decoded.get("mWave"), Integer.toBinaryString(i));
            Assert.assertEquals(decoded.get("timeslots"), revPad(Integer.toBinaryString(i * i), (short) 80));
        } catch (ComparisonFailure e) {
            System.out.println("parameter = " + i);
            throw e;
        }
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

    @Test
    public void testsmall() throws FlowParserException{
        OpticalResourceAttributes match = makeResources(
                new BigInteger("00000000", 2),
                "00000000000000000000000000000000000000000000000000000000000000000000000000000000",
                2
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger("00000001", 2),
                "00000000000000000000000000000000000000000000000000000000000000000000000000000000",
                1
        );
        NepheleFlowAttributes nepheleResources = makeNepheleResources(
                fromBinaryString("00000000"),
                fromBinaryString("00000000")
        );
        Metadata metadata = maker.buildMetadata(match, output, nepheleResources, false);
        Assert.assertEquals(new BigInteger("2", 10), metadata.getMetadata());
        Assert.assertEquals(new BigInteger("0", 10), metadata.getMetadataMask()); // Should not be 0 but absent
    }
    */
}
