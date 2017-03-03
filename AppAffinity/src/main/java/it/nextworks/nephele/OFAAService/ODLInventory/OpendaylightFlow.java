package it.nextworks.nephele.OFAAService.ODLInventory;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.nextworks.nephele.OFTranslator.FlowEntry;
import it.nextworks.nephele.OFTranslator.OFComprehensiveMatch;

public class OpendaylightFlow {

    static OpendaylightFlow makePoppingFlow(String nodeId) {
        OpendaylightFlow output = new OpendaylightFlow();
        output.flags = "";
        output.id = String.format("OF%s_POPPING_FLOW", nodeId);
        output.table_id = 0;
        output.match = OpendaylightMatch_Eth.makeAnyVlanMatch();
        output.instructions = OpendaylightInstructionsContainer.makeEmulationInstructions(true);
        output.htimeout = 0;
        output.itimeout = 0;
        output.priority = 1001; // Must come before forwarding flow.
        return output;
    }

    static OpendaylightFlow makeForwardingFlow(String nodeId) {
        OpendaylightFlow output = new OpendaylightFlow();
        output.flags = "";
        output.id = String.format("OF%s_FORWARDING_FLOW", nodeId);
        output.table_id = 0;
        output.match = OpendaylightMatch_Eth.makeEmptyMatch();
        output.instructions = OpendaylightInstructionsContainer.makeEmulationInstructions(false);
        output.htimeout = 0;
        output.itimeout = 0;
        output.priority = 1000;
        return output;
    }

    //private Logger log = LoggerFactory.getLogger("Flow");

    private String id;

    private OpendaylightInstructionsContainer instructions;

    private String flags;

    private OpendaylightMatch match;

    @JsonProperty("hard-timeout")
    private Integer htimeout;

    @JsonProperty("idle-timeout")
    private Integer itimeout;

    private Integer priority;

    private Integer table_id;

    private OpendaylightFlow() {

    }

    public OpendaylightFlow(FlowEntry inFlow, String inId, Integer inTableId) {

        flags = "";

        id = inId;

        table_id = inTableId;

        OFComprehensiveMatch inMatch = inFlow.getMatch();

        if (inMatch.getLambda() != null) {
            match = new OpendaylightMatch_Opt(inMatch);
        }
        if (inMatch.getAddress() != null) {
            match = new OpendaylightMatch_Eth(inMatch);
        }

        instructions = new OpendaylightInstructionsContainer(inFlow.getOutput());

        htimeout = 0;

        itimeout = 0;

        priority = 1000;

    }

    public String getId() {
        return id;
    }

    public OpendaylightInstructionsContainer getInstructions() {
        return instructions;
    }

    public String getFlags() {
        return flags;
    }

    public OpendaylightMatch getMatch() {
        return match;
    }

    @JsonProperty("hard-timeout")
    public Integer getHtimeout() {
        return htimeout;
    }

    @JsonProperty("idle-timeout")
    public Integer getItimeout() {
        return itimeout;
    }

    public Integer getPriority() {
        return priority;
    }

    public Integer getTable_id() {
        return table_id;
    }

}
