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

public class NewMetadataTest {

    private MetadataMaker maker;

    private String rPad(String inString, short size) throws FlowParserException {
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


    private String lPad(String inString, short size) throws FlowParserException {
        StringBuilder outString = new StringBuilder("");
        if (inString.length() > size) {
            throw new FlowParserException("field size is greater than " + String.valueOf(size) + ", not supported.");
        } else if (inString.length() < size) {
            short missingZeroes = (short) (size - inString.length());
            for (short i = 0; i < missingZeroes; i++) {
                outString.append("0");
            }
        }
        outString.append(inString);
        return outString.toString();
    }

    private Map<String, Object> decode(Metadata metadata) throws FlowParserException {
        Map<String, Object> output = new HashMap<>();

        String metadataStr = metadata.getMetadata().toString(2);
        String mask = metadata.getMetadataMask().toString(2);
        metadataStr = lPad(metadataStr, (short) 64);
        mask = lPad(mask, (short) 64);

        String remainder = mask.substring(0, mask.length() - 15);
        remainder = lPad(remainder, (short) 49);

        String extraBit = remainder.substring(remainder.length() - 1);
        remainder = remainder.substring(0, remainder.length() -1);

        String oWave = remainder.substring(remainder.length() - 8);
        remainder = remainder.substring(0, remainder.length() - 8);

        String count = remainder.substring(remainder.length() - 8);
        remainder = remainder.substring(0, remainder.length() - 8);

        String schedule = remainder.substring(remainder.length() - 8);
        remainder = remainder.substring(0, remainder.length() - 8);

        String mWave = remainder.substring(remainder.length() - 8);
        remainder = remainder.substring(0, remainder.length() - 8);

        String timeslots = metadataStr + remainder;
        output.put("extraBit", extraBit.replaceFirst("^0+(?!$)", ""));
        output.put("oWave", oWave.replaceFirst("^0+(?!$)", ""));
        output.put("count", count.replaceFirst("^0+(?!$)", ""));
        output.put("schedule", schedule.replaceFirst("^0+(?!$)", ""));
        output.put("mWave", mWave.replaceFirst("^0+(?!$)", ""));
        output.put("timeslots", timeslots);
        return output;
    }

    @Before
    public void setup() {
        maker = new MetadataMakerNew();
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
        Assert.assertEquals(
                new BigInteger("1111111111111111111111111111111111111111111111111111111111111111", 2),
                metadata.getMetadata()
        );
        Assert.assertEquals(
                new BigInteger("1111111111111111111111111111111111111111111111111000000000000000", 2),
                metadata.getMetadataMask()
        );
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
        Assert.assertEquals(
                new BigInteger("1111100000000011110011001110100000000000000011101101111000111110", 2),
                metadata.getMetadata()
        );
        Assert.assertEquals(
                new BigInteger("0000111011110011110001111010100100111001110001111000000000000000", 2),
                metadata.getMetadataMask()
        );
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
        Assert.assertEquals(new BigInteger("0", 10), metadata.getMetadata());
        Assert.assertEquals(new BigInteger("1000000000000000", 2), metadata.getMetadataMask());
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
        Assert.assertEquals(new BigInteger("0", 10), metadata.getMetadata());
        Assert.assertEquals(
                new BigInteger("1111111111111111111111111111111111111111111111111000000000000000", 2),
                metadata.getMetadataMask()
        );
    }

    @Test
    public void testWeird() throws FlowParserException{
        OpticalResourceAttributes match = makeResources(
                new BigInteger("11111100", 2),
                "00000100110011001100110011001100110011001100110011001100110011011110101011011010",
                4
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger("00000001", 2),
                "00000100110011001100110011001100110011001100110011001100110011011110101011011010",
                1
        );
        NepheleFlowAttributes nepheleResources = makeNepheleResources(
                fromBinaryString("10001001"),
                fromBinaryString("00000100")
        );
        Metadata metadata = maker.buildMetadata(match, output, nepheleResources, false);
        Assert.assertEquals(
                new BigInteger("0000010011001100110011001100110011001100110011001100110011001101", 2),
                metadata.getMetadata()
        );
        Assert.assertEquals(
                new BigInteger("1110101011011010111111001000100100000100000000010000000000000000", 2),
                metadata.getMetadataMask()
        );
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
        Assert.assertEquals(
                new BigInteger("0000000000000000000000000000000000000000000000000000011011110000", 2),
                metadata.getMetadata()
        );
        Assert.assertEquals(
                new BigInteger("0101101101011001110100111011001000000000000000000000000000000000", 2),
                metadata.getMetadataMask()
        );
    }

    @Test
    public void testAllLow() throws FlowParserException {
        for (Integer i = 1; i<30; i++) {
            for (short j = 0; j<50; j++) {
                for (short k = 1; k < 30; k++) {
                    OpticalResourceAttributes match = makeResources(
                            new BigInteger(i.toString(), 10),
                            rPad(Integer.toBinaryString((int) Math.pow(i, j) % (k + 33)), (short) 80),
                            2
                    );
                    OpticalResourceAttributes output = makeResources(
                            new BigInteger(i.toString(), 10),
                            rPad(Integer.toBinaryString((int) Math.pow(i, j) % (k + 33)), (short) 80),
                            1
                    );
                    NepheleFlowAttributes nepheleResources = makeNepheleResources(
                            j,
                            k
                    );
                    Metadata metadata = maker.buildMetadata(match, output, nepheleResources, false);
                    Map<String, Object> decoded = decode(metadata);
                    try {
                        Assert.assertEquals(decoded.get("oWave"), Integer.toBinaryString(i));
                        Assert.assertEquals(decoded.get("mWave"), Integer.toBinaryString(i));
                        Assert.assertEquals(decoded.get("count"), Integer.toBinaryString(k));
                        Assert.assertEquals(decoded.get("schedule"), Integer.toBinaryString(j));
                        Assert.assertEquals(decoded.get("timeslots"), rPad(Integer.toBinaryString(
                                (int) Math.pow(i, j) % (k + 33)
                        ), (short) 80));
                    } catch (ComparisonFailure e) {
                        System.out.printf("(i, j, k) = %s, %s, %s.\n", i, j, k);
                        throw e;
                    }
                }
            }
        }
    }

    @Test
    public void test15() throws FlowParserException {
        Integer i = 15;
        OpticalResourceAttributes match = makeResources(
                new BigInteger(i.toString(), 10),
                rPad(Integer.toBinaryString(i * i), (short) 80),
                2
        );
        OpticalResourceAttributes output = makeResources(
                new BigInteger(i.toString(), 10),
                rPad(Integer.toBinaryString(i * i), (short) 80),
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
            Assert.assertEquals(decoded.get("timeslots"), rPad(Integer.toBinaryString(i * i), (short) 80));
        } catch (ComparisonFailure e) {
            System.out.println("parameter = " + i);
            throw e;
        }
    }

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
        Assert.assertEquals(
                new BigInteger("0000000000000000000000000000000000000000000000000000011011110000", 2),
                metadata.getMetadata()
        );
        Assert.assertEquals(
                new BigInteger("0101101101011001110100111011000111111111111111111000000000000000", 2),
                metadata.getMetadataMask()
        );
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
        Assert.assertEquals(new BigInteger("0", 10), metadata.getMetadata());
        Assert.assertEquals(new BigInteger("10000000000000000", 2), metadata.getMetadataMask());
    }

}
