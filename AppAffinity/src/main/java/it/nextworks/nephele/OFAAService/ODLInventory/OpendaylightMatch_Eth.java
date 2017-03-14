package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.nextworks.nephele.OFTranslator.OFComprehensiveMatch;

public class OpendaylightMatch_Eth extends OpendaylightMatch {

    static OpendaylightMatch_Eth makeEmptyMatch() {
        return new OpendaylightMatch_Eth();
    }

    static OpendaylightMatch_Eth makeAnyVlanMatch() {
        OpendaylightMatch_Eth out = new OpendaylightMatch_Eth();
        out.vlanMatch = new OpendaylightVlanMatch();
        return out;
    }


    private OpendaylightEthernetMatch eMatch;

    private String ipDest;

    private OpendaylightMatch_Eth() {

    }

    public OpendaylightMatch_Eth(OFComprehensiveMatch inMatch) {
        super(inMatch);

        eMatch = new OpendaylightEthernetMatch();

        String address = inMatch.getAddress();

        //adding subnet mask
        ipDest = address + "/" + String.valueOf(inMatch.getIpMask());
    }

    @JsonProperty("vlan-match")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public OpendaylightVlanMatch vlanMatch;

    @JsonProperty("ethernet-match")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public OpendaylightEthernetMatch geteMatch() {
        return eMatch;
    }

    @JsonProperty("ipv4-destination")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getIpDest() {
        return ipDest;
    }

    @JsonProperty("in-port")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getInPort() {
        return inPort;
    }

    public static class OpendaylightVlanMatch {

        @JsonProperty("vlan-id")
        public OpendaylightVlanId vlanId = new OpendaylightVlanId();
    }

    public static class OpendaylightVlanId {

        @JsonProperty("vlan-id-present")
        public boolean vlanIdPresent = true;
    }

}
