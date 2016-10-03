package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.nextworks.nephele.OFTranslator.OFComprehensiveMatch;

public class OpendaylightMatch {
	
	@JsonProperty("in-port")
	private String inPort;
	
	public OpendaylightMatch(OFComprehensiveMatch inMatch){
		inPort = inMatch.getInputPort();
	}

	@JsonProperty("in-port")
	public String getInPort() {
		return inPort;
	}

}
