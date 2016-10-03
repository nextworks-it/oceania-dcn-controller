package it.nextworks.nephele.OFTranslator;

import java.util.HashSet;
import java.util.Set;

public class Node{
	
	protected String nodeId;
	protected Set<FlowEntry> flowTable;
	
	public String getNodeId() {
		return nodeId;
	}
	public Set<FlowEntry> getFlowTable() {
		return new HashSet<>(flowTable);
	}
	public void setFlowTable(Set<? extends FlowEntry> newFlowTable) {
        flowTable = new HashSet<>(newFlowTable);
	}
	
}
