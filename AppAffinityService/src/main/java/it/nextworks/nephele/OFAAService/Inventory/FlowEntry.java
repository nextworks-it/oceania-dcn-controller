package it.nextworks.nephele.OFAAService.Inventory;

public class FlowEntry {
	
	private OFComprehensiveMatch match;
	private OptOFOutput output;
	
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
