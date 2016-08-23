package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.nextworks.nephele.OFAAService.Inventory.OptOFOutput;

public class OpendaylightAction {
	
	private Integer order;
	
	@JsonProperty("output-action")
	private OpendaylightOutputAction oAction;
	
	public OpendaylightAction(OptOFOutput inOutAction){
		
		if (inOutAction.getLambda() != null) oAction = new OpendaylightOutputAction_Opt(inOutAction);
		else oAction = new OpendaylightOutputAction(inOutAction);
		
		order = 0;
	}

	public Integer getOrder() {
		return order;
	}
	
	@JsonProperty("output-action")
	public OpendaylightOutputAction getoAction() {
		return oAction;
	}
	
}
