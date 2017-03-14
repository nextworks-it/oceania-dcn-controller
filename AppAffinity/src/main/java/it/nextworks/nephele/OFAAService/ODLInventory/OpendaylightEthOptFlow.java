package it.nextworks.nephele.OFAAService.ODLInventory;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.FlowEntry;

public class OpendaylightEthOptFlow implements OpendaylightOpticalFlow{

    //private Logger log = LoggerFactory.getLogger("Flow");

    private String id;

    @JsonProperty("eth-opt-case")
    public EthOptCase opticalFlow;

    public OpendaylightEthOptFlow(FlowEntry inFlow, String inId) {

        id = inId;

        opticalFlow = new EthOptCase(inFlow);

    }

    @Override
    public String getFlowId() {
        return id;
    }

    public static class EthOptCase {

        @JsonProperty("eth-match-type")
        public OpendaylightMatch_Eth ethMatchType;

        @JsonProperty("opt-output-type")
        public OpendaylightOutputAction_Opt optOutputType;

        EthOptCase(FlowEntry inFlow) {
            ethMatchType = new OpendaylightMatch_Eth(inFlow.getMatch());
            optOutputType = new OpendaylightOutputAction_Opt(inFlow.getOutput());
        }

    }

}
