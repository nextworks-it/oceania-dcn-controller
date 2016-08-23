package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.nextworks.nephele.OFAAService.Inventory.OptOFOutput;

public class OpendaylightInstruction {
	
	private Integer order;
	
	@JsonProperty("apply-actions")
	private OpendaylightActionContainer actions;
	
	public OpendaylightInstruction(OptOFOutput inOutAction){
		order = 0;
		actions = new OpendaylightActionContainer(inOutAction);
	}

	public Integer getOrder() {
		return order;
	}

	@JsonProperty("apply-actions")
	public OpendaylightActionContainer getActions() {
		return actions;
	}
}
