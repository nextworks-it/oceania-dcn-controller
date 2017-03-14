package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.OptOFOutput;

public class OpendaylightInstruction {

    static OpendaylightInstruction makeEmulationInstructionPop() {
        OpendaylightInstruction out = new OpendaylightInstruction();
        out.actions = OpendaylightActionContainer.makeEmulationActions();
        out.order = 0;
        return out;
    }

    static OpendaylightInstruction makeEmulationInstructionForward(Integer order) {
        OpendaylightInstruction out = new OpendaylightInstruction();
        out.goToTable = new OpendaylightGoToTable(1);
        out.order = order;
        return out;
    }

    private Integer order;

    private OpendaylightActionContainer actions;

    private OpendaylightInstruction() {

    }

    public OpendaylightInstruction(OptOFOutput inOutAction) {
        order = 0;
        actions = new OpendaylightActionContainer(inOutAction);
    }

    public Integer getOrder() {
        return order;
    }

    @JsonProperty("go-to-table")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public OpendaylightGoToTable goToTable;

    @JsonProperty("apply-actions")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public OpendaylightActionContainer getActions() {
        return actions;
    }

    public static class OpendaylightGoToTable {

        private OpendaylightGoToTable(Integer tableId) {
            this.tableId = tableId;
        }

        @JsonProperty("table_id")
        public Integer tableId;
    }
}
