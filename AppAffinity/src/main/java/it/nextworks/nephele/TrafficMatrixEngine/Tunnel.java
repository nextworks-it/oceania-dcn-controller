package it.nextworks.nephele.TrafficMatrixEngine;

public class Tunnel {

    public Integer source;
    public Integer dest;
    public Integer bandwidth;

    public Tunnel() {

    }

    public Tunnel(Integer inSource, Integer inDest, Integer inBandwidth) {
        source = inSource;
        dest = inDest;
        bandwidth = inBandwidth;
    }
}
