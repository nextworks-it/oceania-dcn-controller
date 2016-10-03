package it.nextworks.nephele.OFAAService.Inventory;

import java.util.HashSet;
import java.util.Set;

public class AANodeImpl implements Node {
	
	protected String nodeId;
	protected Set<FlowEntry> flowTable;
	
	@Override
    public String getNodeId() {
		return nodeId;
	}
	@Override
    public Set<FlowEntry> getFlowTable() {
		return new HashSet<>(flowTable);
	}

	@Override
    public void setFlowTable(Set<? extends FlowEntry> flowTable) {
		this.flowTable = new HashSet<>(flowTable);
	}
	
}
