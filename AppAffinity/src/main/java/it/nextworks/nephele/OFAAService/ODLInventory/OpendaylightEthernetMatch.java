package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpendaylightEthernetMatch {

    @JsonProperty("ethernet-type")
    private OpendaylightEthernetType eType;

    public OpendaylightEthernetMatch() {
        eType = new OpendaylightEthernetType();
    }

    @JsonProperty("ethernet-type")
    public OpendaylightEthernetType geteType() {
        return eType;
    }

}
