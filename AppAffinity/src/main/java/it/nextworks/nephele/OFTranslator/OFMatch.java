package it.nextworks.nephele.OFTranslator;

public class OFMatch {
	
	protected String inputPort;
	
	public OFMatch(String inPort) throws IllegalArgumentException {
		inputPort=inPort;
	}

	public String getInputPort() {
		return inputPort;
	}
	
	public OFMatch(){
		
	}

	public void setInputPort(String inputPort) {
		this.inputPort = inputPort;
	}
}
