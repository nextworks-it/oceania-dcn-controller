package it.nextworks.nephele.OFAAService.Inventory;

import it.nextworks.nephele.OFTranslator.OptOFOutput;

public class FlowEntryImpl implements FlowEntry {
	
	private OFComprehensiveMatch match;
	private OptOFOutput output;
	
	public FlowEntryImpl(){
		
	}
	
	@Override
    public OFComprehensiveMatch getMatch() {
		return match;
	}

	@Override
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
