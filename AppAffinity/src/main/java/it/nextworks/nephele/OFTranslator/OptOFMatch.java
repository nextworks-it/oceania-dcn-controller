package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;

public class OptOFMatch extends OFMatch {

    private Integer lambda;
    private Bitmap timeBitmap;

    public OptOFMatch(Integer inLambda, Bitmap inBitmap, String inPort) {
        super(inPort);
        if ((inLambda < 1) || (inLambda > (Const.W))) {
            throw new IllegalArgumentException("Illegal lambda.");
        }
        lambda = inLambda;
        timeBitmap = inBitmap;
    }

    public OptOFMatch(Integer inLambda, String inBitmap, String inPort) {
        super(inPort);
        if ((inLambda < 1) || (inLambda > (Const.W))) {
            throw new IllegalArgumentException("Illegal lambda.");
        }
        lambda = inLambda;
        timeBitmap = new Bitmap(inBitmap);
    }

    public OptOFMatch(Integer inLambda, String inBitmap) {
        super(null);
        if ((inLambda < 1) || (inLambda > (Const.W))) {
            throw new IllegalArgumentException("Illegal lambda.");
        }
        lambda = inLambda;
        timeBitmap = new Bitmap(inBitmap);
    }

    public OptOFMatch(Integer inLambda, Bitmap inBitmap) {
        super(null);
        if ((inLambda < 1) || (inLambda > (Const.W))) {
            throw new IllegalArgumentException("Illegal lambda.");
        }
        lambda = inLambda;
        timeBitmap = inBitmap;
    }

    public Integer getLambda() {
        return lambda;
    }

    public String getTimeBitmap() {
        return timeBitmap.getBitmap();
    }

}
