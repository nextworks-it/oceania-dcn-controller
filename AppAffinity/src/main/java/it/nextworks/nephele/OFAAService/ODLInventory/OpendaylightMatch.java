package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.nextworks.nephele.OFTranslator.OFComprehensiveMatch;

public class OpendaylightMatch {

	protected OpendaylightMatch() {

	}

	protected String inPort;
	
	public OpendaylightMatch(OFComprehensiveMatch inMatch){
		inPort = inMatch.getInputPort();
	}

}
