package it.nextworks.nephele.OFTranslator;

public class FlowEntry {

    private OFComprehensiveMatch match;
    private OptOFOutput output;

    public FlowEntry(OFMatch inMatch, OFOutput inOutput, boolean intra) {
        match = new OFComprehensiveMatch(inMatch);
        match.setIntra(intra);
        output = new OptOFOutput(inOutput);
    }

    public FlowEntry(OFMatch inMatch, OFOutput inOutput) {
        match = new OFComprehensiveMatch(inMatch);
        match.setIntra(false);
        output = new OptOFOutput(inOutput);
    }

    public FlowEntry(OFMatch inMatch, String port) {
        match = new OFComprehensiveMatch(inMatch);
        output = new OptOFOutput(port);
    }

    public FlowEntry(Integer[] IP, short ipMask, String inPort, Integer lambda,
                     Bitmap tBmp, String outPort, int priority) {
        match = new OFComprehensiveMatch(new EthOFMatch(IP, ipMask, inPort, priority));
        output = new OptOFOutput(lambda, tBmp, outPort);
    }

    public FlowEntry() {

    }

    public OFComprehensiveMatch getMatch() {
        return match;
    }

    public OptOFOutput getOutput() {
        return output;
    }

    public void setMatch(OFComprehensiveMatch match) {
        this.match = match;
    }

    public void setOutput(OptOFOutput output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "FlowEntry{" +
                "match=" + match.toString() +
                ", output=" + output.toString() +
                '}';
    }
}
