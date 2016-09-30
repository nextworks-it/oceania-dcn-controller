package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrafficProfile {
	
	@JsonProperty("Reserved_bandwidth")
	public Integer bandwidth;

	public boolean validate(){
		if (bandwidth != null) return true;
		else throw new IllegalArgumentException("Reserved bandwidth must not be null.");
	}

}
