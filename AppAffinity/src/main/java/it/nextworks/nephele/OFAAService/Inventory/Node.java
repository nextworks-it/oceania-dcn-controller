package it.nextworks.nephele.OFAAService.Inventory;

import java.util.Set;


public interface Node {
    String getNodeId();

    Set<FlowEntry> getFlowTable();

    void setFlowTable(Set<? extends FlowEntry> flowTable);
}
