package it.nextworks.nephele.OFAAService.ODLInventory;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import it.nextworks.nephele.OFTranslator.Node;

public class OpendaylightNode {

    @ApiModelProperty(notes = "Node ID")
    private String id;

    @JsonProperty("optical-translator:optical-flow-table")
    private OpendaylightOpticalTable opticalFlowTable;

    public OpendaylightNode(Node node) {
        id = node.getNodeId();
        opticalFlowTable = new OpendaylightOpticalTable(node.getOptFlowTable(), id);
        if (Const.EMULATED) {
            // Setup vlan popping (if necessary) in table 0
            flowTable.add(OpendaylightTable.makeEmulationTable0(id));
            flowTable.add(new OpendaylightTable(node.getFlowTable(), id, 1));
        } else {
            flowTable.add(new OpendaylightTable(node.getFlowTable(), id));
        }
    }

    public String getId() {
        return id;
    }

    @JsonProperty("optical-translator:optical-flow-table")
    public OpendaylightOpticalTable getOpticalFlowTable() {
        return opticalFlowTable;
    }

    @JsonProperty("flow-node-inventory:table")
    @ApiModelProperty(notes = "List of flow tables")
    public ArrayList<OpendaylightTable> getFlowTable() {
        return flowTable;
    }

    @JsonProperty("flow-node-inventory:table")
    @ApiModelProperty(notes = "List of flow tables")
    private ArrayList<OpendaylightTable> flowTable = new ArrayList<>();

}
