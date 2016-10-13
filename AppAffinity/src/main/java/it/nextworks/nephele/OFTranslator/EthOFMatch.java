package it.nextworks.nephele.OFTranslator;

public class EthOFMatch extends OFMatch {
	
	private Integer[] IP;
	
	public EthOFMatch(Integer[] inputIP, String port) {
		super(port);
		if (!(IPValidator.validate(inputIP))){
			throw new IllegalArgumentException("Illegal IP address");
		}
		IP=inputIP;
	}

	public Integer[] getIP() {
		return IP;
	}

	public void setIP(Integer[] iP) {
		IP = iP;
	}
	
}
