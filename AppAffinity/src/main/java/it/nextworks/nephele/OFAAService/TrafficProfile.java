package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrafficProfile {
	
	@JsonProperty("Reserved_bandwidth")
	public Integer bandwidth;

	public boolean validate(){
		if (bandwidth != null) return true;
		else throw new IllegalArgumentException("Reserved bandwidth must not be null.");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TrafficProfile that = (TrafficProfile) o;

		return bandwidth.equals(that.bandwidth);
	}

	@Override
	public int hashCode() {
		return bandwidth.hashCode();
	}
}
