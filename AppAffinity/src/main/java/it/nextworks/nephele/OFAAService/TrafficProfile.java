package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrafficProfile {
	
	@JsonProperty("Reserved_bandwidth")
	public Integer bandwidth;

	public boolean validate(){
		return (bandwidth != null);
	}

}
