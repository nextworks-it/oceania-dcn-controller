package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFTranslator.Bitmap;

public class OFComprehensiveMatch{
	

	private Integer lambda;
	private Bitmap timeBitmap;
	
	private Integer[] IP;
	
	private String inputPort;

	public String getInputPort() {
		return inputPort;
	}
	
	public void setInputPort(String inputPort) {
		this.inputPort = inputPort;
	}
	
	public Integer getLambda() {
		return lambda;
	}

	public String getTimeBitmap() {
		return timeBitmap.getBitmap();
	}
	
	public OFComprehensiveMatch(){
		
	}

	public OFComprehensiveMatch(OFMatch match){
        inputPort = match.getInputPort();
        if (match instanceof OptOFMatch){
            OptOFMatch optMatch = (OptOFMatch) match;
            lambda = optMatch.getLambda();
            timeBitmap = new Bitmap(optMatch.getTimeBitmap());
        }
        if (match instanceof EthOFMatch){
            IP = ((EthOFMatch) match).getIP();
        }
	}

	public void setLambda(Integer lambda) {
		this.lambda = lambda;
	}

	public void setTimeBitmap(Bitmap timeBitmap) {
		this.timeBitmap = timeBitmap;
	}
	
	public String getAddress() {
		if (IP == null) return null;
		else return  IP[0].toString()+"."+
				IP[1].toString()+"."+
				IP[2].toString()+"."+
				IP[3].toString();
	}

    public Integer[] getIP() {
        return IP;
    }

	public void setIP(Integer[] iP) {
		IP = iP;
	}

}
