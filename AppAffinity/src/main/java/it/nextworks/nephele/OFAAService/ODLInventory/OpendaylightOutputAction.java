package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.OptOFOutput;

public class OpendaylightOutputAction extends OpendaylightBaseOutputAction {

    @JsonProperty("max-length")
    private Integer len;

    public OpendaylightOutputAction(OptOFOutput inOutAction) {
        super(inOutAction);
        len = 0;
    }

    @JsonProperty("max-length")
    public Integer getLen() {
        return len;
    }

    @JsonProperty("output-node-connector")
    public String getOutPort() {
        return outPort;
    }

}
