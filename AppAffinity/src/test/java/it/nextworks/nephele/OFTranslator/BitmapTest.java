package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Created by Marco Capitani on 14/09/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
public class BitmapTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setT() {
        Const.T = 12;
    }

    @Test
    public void spliceStart() throws Exception {
        Bitmap bitmap = new Bitmap("100000000000");
        Bitmap res = bitmap.splice(1);
        Assert.assertEquals(bitmap, res);
    }

    @Test
    public void spliceEnd() throws Exception {
        Bitmap bitmap = new Bitmap("000000000001");
        Bitmap res = bitmap.splice(1);
        Assert.assertEquals(bitmap, res);
    }

    @Test
    public void spliceNotEnough() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        Bitmap bitmap = new Bitmap("110000000011");
        Bitmap res = bitmap.splice(5);
    }

    @Test
    public void spliceSeveral() throws Exception {
        Bitmap bitmap = new Bitmap("010001100010");
        Bitmap res1 = bitmap.splice(1);
        Bitmap res2 = bitmap.splice(1);
        Bitmap res3 = bitmap.splice(2);
        Assert.assertEquals(new Bitmap("010000000000"), res1);
        Assert.assertEquals(new Bitmap("000001000000"), res2);
        Assert.assertEquals(new Bitmap("000000100010"), res3);
    }

    @Test
    public void spliceRemaining() throws Exception {
        Bitmap bitmap = new Bitmap("010001100010");
        Bitmap res1 = bitmap.splice(1);
        Bitmap res2 = bitmap.splice(1);
        Bitmap res3 = bitmap.remainingSlice();
        Assert.assertEquals(new Bitmap("010000000000"), res1);
        Assert.assertEquals(new Bitmap("000001000000"), res2);
        Assert.assertEquals(new Bitmap("000000100010"), res3);
    }
}