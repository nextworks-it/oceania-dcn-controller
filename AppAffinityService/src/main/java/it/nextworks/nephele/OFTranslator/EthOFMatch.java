package it.nextworks.nephele.OFTranslator;

public class EthOFMatch extends OFMatch {
	
	private Integer[] IP;
	
	public EthOFMatch(Integer[] inputIP, String port) throws IllegalArgumentException{
		super(port);
		if (!(IPValidator.validate(inputIP))){
			throw new IllegalArgumentException("Illegal IP address");
		}
		IP=inputIP;
	}

	public String getAddress() {
		return IP[0].toString()+"."+
				IP[1].toString()+"."+
				IP[2].toString()+"."+
				IP[3].toString();
	}
	
	public EthOFMatch(){
		
	}

	public Integer[] getIP() {
		return IP;
	}

	public void setIP(Integer[] iP) {
		IP = iP;
	}
	
}
