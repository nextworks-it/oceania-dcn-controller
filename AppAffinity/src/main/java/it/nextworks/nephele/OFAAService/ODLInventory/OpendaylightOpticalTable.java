package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.FlowEntry;

import java.util.ArrayList;
import java.util.Set;

public class OpendaylightOpticalTable {

    @JsonProperty("optical-flow")
    private ArrayList<OpendaylightOpticalFlow> flow = new ArrayList<>();

    public OpendaylightOpticalTable(Set<? extends FlowEntry> input, String nodeId) {

        Integer i = 1000;
        for (FlowEntry inFlow : input) {
            String flowId = String.format("OF%s_F%s", nodeId, i);
            flow.add(OpendaylightOpticalFlow.buildFlow(inFlow, flowId));
            i = i + 1;
        }
    }

    public ArrayList<OpendaylightOpticalFlow> getFlow() {
        return flow;
    }
}
