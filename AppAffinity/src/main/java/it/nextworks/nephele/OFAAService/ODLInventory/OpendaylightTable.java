package it.nextworks.nephele.OFAAService.ODLInventory;

import it.nextworks.nephele.OFTranslator.FlowEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OpendaylightTable {

    static OpendaylightTable makeEmulationTable0(String nodeId) {
        OpendaylightTable table = new OpendaylightTable();
        table.flow = Arrays.asList(
                OpendaylightFlow.makePoppingFlow(nodeId),
                OpendaylightFlow.makeForwardingFlow(nodeId)
        );
        table.id = 0;
        return table;
    }

    private Integer id;

    private List<OpendaylightFlow> flow = new ArrayList<>();

    private OpendaylightTable() {

    }

    public OpendaylightTable(Set<? extends FlowEntry> input, String nodeId) {
        this(input, nodeId, 0);
    }

    public OpendaylightTable(Set<? extends FlowEntry> input, String nodeId, Integer tableId) {

        id = tableId;

        Integer i = 0;
        for (FlowEntry inFlow : input) {
            String flowId = String.format("OF%s_F%s", nodeId, i);
            flow.add(new OpendaylightFlow(inFlow, flowId, id));
            i = i + 1;
        }
    }

    public Integer getId() {
        return id;
    }

    public List<OpendaylightFlow> getFlow() {
        return flow;
    }
}
