package it.nextworks.nephele.OFTranslator;

public class EthOFMatch extends OFMatch {

    private Integer[] IP;

    private short ipMask;

    private int priority;

    public EthOFMatch(Integer[] inputIP, short ipMask, String port, int priority) {
        super(port);
        if (!(IPValidator.validate(inputIP))) {
            throw new IllegalArgumentException("Illegal IP address");
        }
        IP = inputIP;
        this.ipMask = ipMask;
        this.priority = priority;
    }

    public EthOFMatch(Integer[] inputIP, short ipMask, String port) {
        super(port);
        if (!(IPValidator.validate(inputIP))) {
            throw new IllegalArgumentException("Illegal IP address");
        }
        IP = inputIP;
        this.ipMask = ipMask;
    }

    public EthOFMatch(Integer[] inputIP, short ipMask) {
        super(null);
        if (!(IPValidator.validate(inputIP))) {
            throw new IllegalArgumentException("Illegal IP address");
        }
        IP = inputIP;
        this.ipMask = ipMask;
    }

    public Integer[] getIP() {
        return IP;
    }

    public short getIpMask() {
        return ipMask;
    }

    public int getPriority() {
        return priority;
    }
}
