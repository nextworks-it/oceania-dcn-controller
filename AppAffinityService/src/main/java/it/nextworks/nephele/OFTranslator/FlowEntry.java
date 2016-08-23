package it.nextworks.nephele.OFTranslator;

public class FlowEntry {
	
	public OFMatch match;
	public OFOutput output;
	
	public FlowEntry(OFMatch inMatch, OFOutput inOutput){
		match= inMatch;
		output=inOutput;
	}
	
	public FlowEntry(OFMatch inMatch, String port){
		match= inMatch;
		output=new OFOutput(port);
	}
	
	public FlowEntry(Integer[] IP, String inPort, String outPort) {
		match= new EthOFMatch(IP, inPort);
		output = new OFOutput(outPort);
	}
	
	public FlowEntry(Integer[] IP, String inPort, OFOutput out) {
		match= new EthOFMatch(IP, inPort);
		output = out;
	}
	
	public FlowEntry(Integer[] IP, String inPort, Integer lambda, Bitmap tBmp, String outPort) {
		match= new EthOFMatch(IP, inPort);
		output = new OptOFOutput(lambda, tBmp, outPort);
	}

	public OFMatch getMatch() {
		return match;
	}

	public OFOutput getOutput() {
		return output;
	}
}
