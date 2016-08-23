package it.nextworks.nephele.OFAAService;


/**
 * Bare host-to-host connection.
 * @author MCapitani
 *
 */
public class Tunnel {

	private String source;
	private String dest;
	private Integer bandwidth;
	
	public String getSource() {
		return source;
	}
	
	public String getDest() {
		return dest;
	}
	
	public Integer getBandwidth() {
		return bandwidth;
	}
		
	public void setSource(String source) {
		this.source = source;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public void setBandwidth(Integer bandwidth) {
		this.bandwidth = bandwidth;
	}

	public Tunnel(){
		
	}
	
	public Tunnel(String inSource, String inDest, Integer inBandwidth){
		source = inSource;
		dest = inDest;
		bandwidth = inBandwidth;
	}
}
