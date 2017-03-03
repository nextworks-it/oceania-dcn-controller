package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import it.nextworks.nephele.OFTranslator.OptOFOutput;

public class OpendaylightAction {

    static OpendaylightAction makeEmulationActionPop() {
        OpendaylightAction out = new OpendaylightAction();
        out.order = 0;
        out.popAction = new OpendaylightPopVlanAction();
        return out;
    }

    private Integer order;

    private OpendaylightOutputAction oAction;

    private OpendaylightAction() {

    }

    public OpendaylightAction(OptOFOutput inOutAction) {

        // if (inOutAction.getLambda() != null) oAction = new OpendaylightOutputAction_Opt(inOutAction);
        // Should not happen.
        oAction = new OpendaylightOutputAction(inOutAction);

        order = 0;
    }

    @JsonProperty("order")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getOrder() {
        return order;
    }

    @JsonProperty("output-action")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public OpendaylightOutputAction getoAction() {
        return oAction;
    }

    @JsonProperty("pop-vlan-action")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public OpendaylightPopVlanAction popAction;

    @JsonSerialize
    public static class OpendaylightPopVlanAction {

    }

}
