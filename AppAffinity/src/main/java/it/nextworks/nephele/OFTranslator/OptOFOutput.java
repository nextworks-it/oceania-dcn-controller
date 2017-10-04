package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;

public class OptOFOutput extends OFOutput {

    private Integer lambda;
    private Bitmap timeBitmap;
    private short scheduleId;
    private short flowCounter;

    OptOFOutput(Integer inLambda, Bitmap inBitmap, String inPort) throws IllegalArgumentException {
        super(inPort);
        if ((inLambda < 0) || (inLambda > Const.W - 1)) {
            throw new IllegalArgumentException("Illegal lambda.");
        }

        timeBitmap = inBitmap;
        lambda = inLambda;

    }

    OptOFOutput(Integer inLambda, String inBitmap, String inPort) throws IllegalArgumentException {
        super(inPort);
        if ((inLambda < 0) || (inLambda > Const.W - 1)) {
            throw new IllegalArgumentException("Illegal lambda.");
        }

        timeBitmap = new Bitmap(inBitmap);
        lambda = inLambda;

    }

    OptOFOutput() {

    }

    OptOFOutput(String port) {
        super(port);
    }

    OptOFOutput(OFOutput output) {
        super(output.getOutputPort());
        if (output instanceof OptOFOutput) {
            OptOFOutput optOut = (OptOFOutput) output;
            lambda = optOut.getLambda();
            timeBitmap = new Bitmap(optOut.getTimeBitmap());
        }
    }

    public void setLambda(Integer lambda) {
        this.lambda = lambda;
    }

    public void setTimeBitmap(Bitmap timeBitmap) {
        this.timeBitmap = timeBitmap;
    }

    public Integer getLambda() {
        return lambda;
    }

    public String getTimeBitmap() {
        if (timeBitmap == null) return null;
        else return timeBitmap.getBitmap();
    }

    public short getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(short scheduleId) {
        this.scheduleId = scheduleId;
    }

    public short getFlowCounter() {
        return flowCounter;
    }

    public void setFlowCounter(short flowCounter) {
        this.flowCounter = flowCounter;
    }

    @Override
    public String toString() {
        return "OptOFOutput{" +
                "lambda=" + lambda +
                ", timeBitmap=" + timeBitmap.getBitmap() +
                ", scheduleId=" + scheduleId +
                ", flowCounter=" + flowCounter +
                ", outputPort='" + outputPort + '\'' +
                "}";
    }
}
