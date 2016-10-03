package it.nextworks.nephele.OFAAService.ODLInventory;

import io.swagger.annotations.ApiModelProperty;
import it.nextworks.nephele.OFTranslator.Inventory;

public class OpendaylightInventory {
	
	@ApiModelProperty(notes="Nodes container")
	private OpendaylightNodesContainer nodes;
	
	public OpendaylightInventory(Inventory input){
		nodes = new OpendaylightNodesContainer(input);
	}

	public OpendaylightNodesContainer getNodes() {
		return nodes;
	}

}
