package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.nextworks.nephele.OFTranslator.OFComprehensiveMatch;

public class OpendaylightMatch_Eth extends OpendaylightMatch {
	
	@JsonProperty("ethernet-match")
	private OpendaylightEthernetMatch eMatch;
	
	@JsonProperty("ipv4-destination")
	private String ipDest;
	
	public OpendaylightMatch_Eth(OFComprehensiveMatch inMatch){
		super(inMatch);
		
		eMatch = new OpendaylightEthernetMatch();
		
		String address = inMatch.getAddress();
		//adding subnet mask
		if (address.charAt(address.length() -1) == 0) ipDest = address + "/24"; //inter-rack
		else ipDest = address + "/32"; //intra-rack
				
	}

	@JsonProperty("ethernet-match")
	public OpendaylightEthernetMatch geteMatch() {
		return eMatch;
	}

	@JsonProperty("ipv4-destination")
	public String getIpDest() {
		return ipDest;
	}

	
}
