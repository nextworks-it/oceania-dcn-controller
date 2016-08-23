package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains connection data: specification, bandwidth, source and destination,
 * recovery mode
 *  
 * @author MCapitani
 *
 *
 */
public class Connection {
	
	/**
	 * Point-to-point or point-to-multipoint (not yet supported)
	 */
	@JsonProperty("Connection_type")
	private ConnectionType connType;
	
	@JsonProperty("Source_end_point")
	public ConnectionSource source;
	
	@JsonProperty("Destination_end_point")
	public ConnectionEndPoint dest;
	
	@JsonProperty("Traffic_profile")
	public TrafficProfile profile;
	
	private Recovery recovery;
	
	public Tunnel getTunnel(){
		return new Tunnel(source.node, dest.node, profile.bandwidth);
	}
	
	public void setConnType(ConnectionType type){
		if ( !(type.equals(ConnectionType.POINTTOPOINT)) ) 
			throw new IllegalArgumentException("Unsupported connection type.");
		else connType = type;
	}

	public Recovery getRecovery() {
		return recovery;
	}

	public void setRecovery(Recovery inRecovery) {
		if ( !(inRecovery.equals(Recovery.UNPROTECTED)) )
			throw new IllegalArgumentException("Unsupported recovery method.");
		recovery = inRecovery;
	}

	public ConnectionType getConnType() {
		return connType;
	}
	
	

}
