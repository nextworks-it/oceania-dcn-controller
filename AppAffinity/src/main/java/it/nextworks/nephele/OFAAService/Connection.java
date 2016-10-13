package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.TrafficMatrixEngine.Tunnel;


public class Connection {

	@JsonProperty("Connection_type")
	private ConnectionType connType;
	
	@JsonProperty("Source_end_point")
	public ConnectionSource source;
	
	@JsonProperty("Destination_end_point")
	public ConnectionEndPoint dest;
	
	@JsonProperty("Traffic_profile")
	public TrafficProfile profile;
	
	private Recovery recovery;
	
	public Tunnel makeTunnel(){
		return new Tunnel(source.intNode(), dest.intNode(), profile.bandwidth);
	}
	
	public void setConnType(ConnectionType type){
		if ( !(type.equals(ConnectionType.POINTTOPOINT)) ) 
			throw new IllegalArgumentException("Unsupported connection type.");
		else connType = type;
	}

	@JsonProperty("Recovery")
	public Recovery getRecovery() {
		return recovery;
	}

	@JsonProperty("Recovery")
	public void setRecovery(Recovery inRecovery) {
		if ( !(inRecovery.equals(Recovery.UNPROTECTED)) )
			throw new IllegalArgumentException("Unsupported recovery method.");
		recovery = inRecovery;
	}

	public ConnectionType getConnType() {
		return connType;
	}

	public boolean validateAndInit(){
	    if (source.validateAndInit()
				&& dest.validateAndInit()
				&& profile.validate()
				&& (source.intNode() != dest.intNode())
		) return true;
		else throw new IllegalArgumentException("Source and destination ToRs must be different.");
		// Every "validate" function either returns true or throws an exception.
		// Hence, false here means that source.intNode() == dest.intNode().
    }
	
	

}
