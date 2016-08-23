package it.nextworks.nephele.TrafficMatrixEngine;

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
	
	public int getIntSource(){
		return Integer.parseInt(source.replaceAll("[^0-9]",""));
	}
	
	public int getIntDest(){
		return Integer.parseInt(dest.replaceAll("[^0-9]",""));
	}

	public Tunnel(){
		
	}
}
