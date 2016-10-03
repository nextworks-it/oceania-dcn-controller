package it.nextworks.nephele.OFAAService.ODLInventory;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import it.nextworks.nephele.OFTranslator.Node;

public class OpendaylightNode {
	
	@ApiModelProperty(notes="Node ID")
	private String id;
	
	@JsonProperty("flow-node-inventory:table")
	@ApiModelProperty(notes="List of flow tables")
	private ArrayList<OpendaylightTable> flowTable = new ArrayList<>();
	
	public OpendaylightNode(Node node){
		id = node.getNodeId();
		flowTable.add(new OpendaylightTable(node.getFlowTable()));
	}

	public String getId() {
		return id;
	}

	@JsonProperty("flow-node-inventory:table")
	public ArrayList<OpendaylightTable> getFlowTable() {
		return flowTable;
	}
	
}
