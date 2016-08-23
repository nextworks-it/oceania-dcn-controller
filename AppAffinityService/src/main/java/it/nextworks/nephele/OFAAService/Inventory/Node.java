package it.nextworks.nephele.OFAAService.Inventory;

import java.util.Set;

public class Node {
	
	protected String nodeId;
	protected Set<FlowEntry> flowTable;
	
	public String getNodeId() {
		return nodeId;
	}
	public Set<FlowEntry> getFlowTable() {
		return flowTable;
	}
	public void setFlowTable(Set<FlowEntry> flowTable) {
		this.flowTable = flowTable;
	}
	
}
