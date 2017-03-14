package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.FlowEntry;

public interface OpendaylightOpticalFlow {

    static OpendaylightOpticalFlow buildFlow(FlowEntry inFlow, String id) {
        if (inFlow.getMatch().getAddress() != null) {
            //eth match
            return new OpendaylightEthOptFlow(inFlow, id);
        }
        //opt match
        else if (inFlow.getOutput().getLambda() != null) {
            //opt out
            return new OpendaylightOptOptFlow(inFlow, id);
        }
        // eth out
        else {
            return new OpendaylightOptEthFlow(inFlow, id);
        }
    }

    @JsonProperty("flow-id")
    String getFlowId();
}
