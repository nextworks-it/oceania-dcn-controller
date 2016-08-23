package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectionEndPoint {
	
	@JsonProperty("Node_ID")
	public String node;
	
	@JsonProperty("Zone_ID")
	public String server;

}
