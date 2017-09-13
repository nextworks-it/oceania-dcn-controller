package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.TrafficMatrixEngine.Tunnel;


public class NephConnection {

	@JsonProperty("Connection_type")
	private ConnectionType connType;
	
	@JsonProperty("Source_end_point")
	public ConnectionSource source;
	
	@JsonProperty("Destination_end_point")
	public ConnectionEndPoint dest;
	
	@JsonProperty("Traffic_profile")
	public TrafficProfile profile;

	@JsonProperty("Destination_IP")
	public String destIp;
	
	private Recovery recovery;
	
	public Tunnel makeTunnel(){
		return new Tunnel(source.intNode(), dest.intNode(), profile.bandwidth);
	}
	
	public void setConnType(ConnectionType type){
		if ( !(type.equals(ConnectionType.POINT_TO_POINT)) ) 
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		NephConnection that = (NephConnection) o;

		if (connType != that.connType) return false;
		if (!source.equals(that.source)) return false;
		if (!dest.equals(that.dest)) return false;
		if (!profile.equals(that.profile)) return false;
		if (destIp != null ? !destIp.equals(that.destIp) : that.destIp != null) return false;
		return recovery == that.recovery;
	}

	@Override
	public int hashCode() {
		int result = connType.hashCode();
		result = 31 * result + source.hashCode();
		result = 31 * result + dest.hashCode();
		result = 31 * result + profile.hashCode();
		result = 31 * result + (destIp != null ? destIp.hashCode() : 0);
		result = 31 * result + recovery.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return String.format(
				"\n\tType: %s\n" +
				"\tBandwidth %s\n" +
				"\tRecovery: %s\n" +
				"\tDestination IP: %s\n" +
				"\tSrc_pod %s\n" +
				"\tSrc_tor: %s\n" +
				"\tSrc_srv: %s\n" +
				"\tDst_pod: %s\n" +
				"\tDst_tor: %s\n" +
				"\tDst_srv: %s\n",
				connType,
				profile.bandwidth,
				recovery,
				destIp,
				source.pod,
				source.tor,
				source.server,
				dest.pod,
				dest.tor,
				dest.server
		);
	}
}
