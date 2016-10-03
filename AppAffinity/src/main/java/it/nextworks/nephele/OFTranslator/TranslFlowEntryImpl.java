package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.Inventory.FlowEntry;
import it.nextworks.nephele.OFAAService.Inventory.OFComprehensiveMatch;

public class TranslFlowEntryImpl implements FlowEntry {
	
	public OFMatch match;
	public OFOutput output;
	
	public TranslFlowEntryImpl(OFMatch inMatch, OFOutput inOutput){
		match= inMatch;
		output=inOutput;
	}
	
	public TranslFlowEntryImpl(OFMatch inMatch, String port){
		match= inMatch;
		output=new OFOutput(port);
	}
	
	public TranslFlowEntryImpl(Integer[] IP, String inPort, String outPort) {
		match= new EthOFMatch(IP, inPort);
		output = new OFOutput(outPort);
	}
	
	public TranslFlowEntryImpl(Integer[] IP, String inPort, OFOutput out) {
		match= new EthOFMatch(IP, inPort);
		output = out;
	}
	
	public TranslFlowEntryImpl(Integer[] IP, String inPort, Integer lambda, Bitmap tBmp, String outPort) {
		match= new EthOFMatch(IP, inPort);
		output = new OptOFOutput(lambda, tBmp, outPort);
	}

	public OFMatch getOFMatch() {
		return match;
	}

	public OFOutput getOFOutput() {
		return output;
	}

	@Override
	public OFComprehensiveMatch getMatch() {
		OFComprehensiveMatch output = new OFComprehensiveMatch();
        output.setInputPort(match.inputPort);
        if (match instanceof OptOFMatch) {
            OptOFMatch optMatch = (OptOFMatch) match;
            output.setLambda(optMatch.getLambda());
            output.setTimeBitmap(new Bitmap(optMatch.gettBitmap()));
        }
        if (match instanceof EthOFMatch) {
            EthOFMatch ethMatch = (EthOFMatch) match;
            output.setIP(ethMatch.getIP());
        }
        return output;
	}

	@Override
	public OptOFOutput getOutput() {
        OptOFOutput result = new OptOFOutput();
        result.setOutputPort(output.getOutputPort());
        if (output instanceof OptOFOutput){
            OptOFOutput optOut = (OptOFOutput) output;
            result.setLambda(optOut.getLambda());
            result.setTimeBitmap(new Bitmap(optOut.getTimeBitmap()));
        }
        return result;
	}

}
