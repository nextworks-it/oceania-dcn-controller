package it.nextworks.nephele.OFAAService.ODLInventory;

import java.util.ArrayList;

import io.swagger.annotations.ApiModelProperty;
import it.nextworks.nephele.OFAAService.Inventory.Inventory;
import it.nextworks.nephele.OFAAService.Inventory.Node;

public class OpendaylightNodesContainer {
	
	@ApiModelProperty(notes="List of all the nodes")
	private ArrayList<OpendaylightNode> node = new ArrayList<>();

	public ArrayList<OpendaylightNode> getNode() {
		return node;
	}
	
	public OpendaylightNodesContainer(Inventory input){
		for (Node inNode : input.getNodes().values()){
			node.add(new OpendaylightNode(inNode));
		}
	}
}
