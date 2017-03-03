package it.nextworks.nephele.OFAAService.ODLInventory;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.FlowEntry;

public class OpendaylightOptEthFlow implements OpendaylightOpticalFlow {

    //private Logger log = LoggerFactory.getLogger("Flow");

    private String id;

    @JsonProperty("opt-eth-case")
    public OptEthCase opticalFlow;

    public OpendaylightOptEthFlow(FlowEntry inFlow, String inId) {

        id = inId;

        opticalFlow = new OptEthCase(inFlow);

    }

    @Override
    public String getFlowId() {
        return id;
    }

    public static class OptEthCase {

        @JsonProperty("opt-match-type")
        public OpendaylightMatch_Opt optMatchType;

        @JsonProperty("eth-output-type")
        public OpendaylightOutputAction ethOutputType;

        OptEthCase(FlowEntry inFlow) {
            optMatchType = new OpendaylightMatch_Opt(inFlow.getMatch());
            ethOutputType = new OpendaylightOutputAction(inFlow.getOutput());
        }

    }

}
