package opticaltranslator.nephele.mock.flowutils;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.mock.rev161228.vlan.optical.correspondance.OpticalResource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.mock.rev161228.vlan.optical.correspondance.OpticalResourceBuilder;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class ComparableResourceTest {

    @Test
    public void includesPositive() throws Exception {
        ComparableResource small = new ComparableResource(
                new OpticalResourceBuilder()
                        .setWavelength(new BigInteger("2"))
                        .setTimeslots("110011001100")
                        .build());
        ComparableResource small2 = new ComparableResource(
                new OpticalResourceBuilder()
                        .setWavelength(new BigInteger("2"))
                        .setTimeslots("000000000000")
                        .build());
        ComparableResource big1 = new ComparableResource(
                new OpticalResourceBuilder()
                        .setWavelength(new BigInteger("2"))
                        .setTimeslots("111111001100")
                        .build());
        ComparableResource big2 = new ComparableResource(
                new OpticalResourceBuilder()
                        .setWavelength(new BigInteger("2"))
                        .setTimeslots("111111111111")
                        .build());
        ComparableResource big3 = new ComparableResource(
                new OpticalResourceBuilder()
                        .setWavelength(new BigInteger("2"))
                        .setTimeslots("110011101101")
                        .build());
        Assert.assertTrue(big1.includes(small));
        Assert.assertTrue(big2.includes(small));
        Assert.assertTrue(big3.includes(small));
        Assert.assertTrue(!small.includes(big1));
        Assert.assertTrue(!small.includes(big2));
        Assert.assertTrue(!small.includes(big3));
        Assert.assertTrue(big1.includes(small2));
        Assert.assertTrue(big2.includes(small2));
        Assert.assertTrue(big3.includes(small2));
        Assert.assertTrue(!small2.includes(big1));
        Assert.assertTrue(!small2.includes(big2));
        Assert.assertTrue(!small2.includes(big3));
    }

    @Test
    public void includesNotComparable() {
        ComparableResource a = new ComparableResource(
                new OpticalResourceBuilder()
                        .setWavelength(new BigInteger("2"))
                        .setTimeslots("110011001100")
                        .build());
        ComparableResource b = new ComparableResource(
                new OpticalResourceBuilder()
                        .setWavelength(new BigInteger("1"))
                        .setTimeslots("110011001100")
                        .build());
        ComparableResource c = new ComparableResource(
                new OpticalResourceBuilder()
                        .setWavelength(new BigInteger("2"))
                        .setTimeslots("1100110011001")
                        .build());
        Assert.assertTrue(!a.includes(b));
        Assert.assertTrue(!a.includes(c));
        Assert.assertTrue(!b.includes(a));
        Assert.assertTrue(!c.includes(a));
        Assert.assertTrue(!b.includes(c));
        Assert.assertTrue(!c.includes(b));
    }
}