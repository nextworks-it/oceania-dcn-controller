package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.nextworks.nephele.OFAAService.ODLInventory.OpendaylightMatch_Eth;

public class ConnectionSource extends ConnectionEndPoint {
	
	@JsonProperty("Flow_Match")
	public OpendaylightMatch_Eth match;

}
