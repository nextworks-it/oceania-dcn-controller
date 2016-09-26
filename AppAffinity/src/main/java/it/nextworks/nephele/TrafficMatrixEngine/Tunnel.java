package it.nextworks.nephele.TrafficMatrixEngine;

public class Tunnel {

	public Integer source;
	public Integer sourcePort;
	public Integer dest;
	public Integer bandwidth;

	public Tunnel(){

	}

	public Tunnel(Integer inSource, Integer inSourcePort, Integer inDest, Integer inBandwidth){
		source = inSource;
		dest = inDest;
		bandwidth = inBandwidth;
		sourcePort = inSourcePort;
	}
}
