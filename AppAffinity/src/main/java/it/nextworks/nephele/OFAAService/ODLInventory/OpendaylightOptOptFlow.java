package it.nextworks.nephele.OFAAService.ODLInventory;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.FlowEntry;

public class OpendaylightOptOptFlow implements OpendaylightOpticalFlow{

    //private Logger log = LoggerFactory.getLogger("Flow");

    private String id;

    @JsonProperty("opt-opt-case")
    public OptOptCase opticalFlow;

    public OpendaylightOptOptFlow(FlowEntry inFlow, String inId) {

        id = inId;

        opticalFlow = new OptOptCase(inFlow);

    }

    @Override
    public String getFlowId() {
        return id;
    }

    public static class OptOptCase {

        @JsonProperty("opt-match-type")
        public OpendaylightMatch_Opt optMatchType;

        @JsonProperty("opt-output-type")
        public OpendaylightOutputAction_Opt optOutputType;

        OptOptCase(FlowEntry inFlow) {
            optMatchType = new OpendaylightMatch_Opt(inFlow.getMatch());
            optOutputType = new OpendaylightOutputAction_Opt(inFlow.getOutput());
        }

    }
}
