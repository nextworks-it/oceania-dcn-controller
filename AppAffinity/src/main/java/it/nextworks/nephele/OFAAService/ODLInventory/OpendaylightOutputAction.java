package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.OptOFOutput;

public class OpendaylightOutputAction {
	
	@JsonProperty("max-length")
	private Integer len;
	
	@JsonProperty("output-node-connector")
	private String outPort;
	
	public OpendaylightOutputAction(OptOFOutput inOutAction){
		outPort=inOutAction.getOutputPort();
		len = 0;
	}
	
	@JsonProperty("max-length")
	public Integer getLen() {
		return len;
	}
	
	@JsonProperty("output-node-connector")
	public String getOutPort() {
		return outPort;
	}

}
