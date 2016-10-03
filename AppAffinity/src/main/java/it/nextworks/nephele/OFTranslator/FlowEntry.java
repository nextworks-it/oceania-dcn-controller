package it.nextworks.nephele.OFTranslator;

public class FlowEntry {
	
	private OFComprehensiveMatch match;
	private OptOFOutput output;
	
	public FlowEntry(OFMatch inMatch, OFOutput inOutput){
		match = new OFComprehensiveMatch(inMatch);
		output = new OptOFOutput(inOutput);
	}
	
	public FlowEntry(OFMatch inMatch, String port){
		match = new OFComprehensiveMatch(inMatch);
		output = new OptOFOutput(port);
	}
	
    public FlowEntry(Integer[] IP, String inPort, Integer lambda, Bitmap tBmp, String outPort) {
		match= new OFComprehensiveMatch(new EthOFMatch(IP, inPort));
		output = new OptOFOutput(lambda, tBmp, outPort);
	}

	public FlowEntry(){

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
}
