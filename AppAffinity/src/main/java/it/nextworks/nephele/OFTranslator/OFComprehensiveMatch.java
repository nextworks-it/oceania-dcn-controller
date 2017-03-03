package it.nextworks.nephele.OFTranslator;

public class OFComprehensiveMatch {

    private Integer lambda;
    private Bitmap timeBitmap;

    private short scheduleId;
    private short flowCounter;

    private Integer[] IP;
    private short ipMask;
    private int priority;

    private String inputPort;

    public String getInputPort() {
        return inputPort;
    }

    public void setInputPort(String inputPort) {
        this.inputPort = inputPort;
    }

    public Integer getLambda() {
        return lambda;
    }

    public String getTimeBitmap() {
        if (timeBitmap == null) return null;
        else return timeBitmap.getBitmap();
    }

    public OFComprehensiveMatch() {

    }

    public OFComprehensiveMatch(OFMatch match) {
        inputPort = match.getInputPort();
        if (match instanceof OptOFMatch) {
            OptOFMatch optMatch = (OptOFMatch) match;
            lambda = optMatch.getLambda();
            timeBitmap = new Bitmap(optMatch.getTimeBitmap());
        }
        if (match instanceof EthOFMatch) {
            IP = ((EthOFMatch) match).getIP();
            ipMask = ((EthOFMatch) match).getIpMask();
            priority = ((EthOFMatch) match).getPriority();
        }
    }

    public void setLambda(Integer lambda) {
        this.lambda = lambda;
    }

    public void setTimeBitmap(Bitmap timeBitmap) {
        this.timeBitmap = timeBitmap;
    }

    public String getAddress() {
        if (IP == null) return null;
        else return IP[0].toString() + "." +
                IP[1].toString() + "." +
                IP[2].toString() + "." +
                IP[3].toString();
    }

    public Integer[] getIP() {
        return IP;
    }

    public void setIP(Integer[] iP) {
        IP = iP;
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

    public short getIpMask() {
        return ipMask;
    }

    public void setIpMask(short ipMask) {
        this.ipMask = ipMask;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
