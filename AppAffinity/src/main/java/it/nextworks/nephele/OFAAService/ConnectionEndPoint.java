package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFAAService.ODLInventory.Const;


public class ConnectionEndPoint {

    @JsonProperty("Pod_ID")
    public Integer pod;

    @JsonProperty("ToR_ID")
    public Integer tor;

    @JsonProperty("Zone_ID")
    public Integer server;

    private Integer intNode;

    private String auxValidateAndInit() {
        if (pod == null) return "No pod number specified.";
        if (pod < Const.firstPod) return String.format("Pod number %s too low: accepted %s to %s",
                pod, Const.firstPod, Const.firstPod + Const.P - 1);
        if (pod >= Const.firstPod + Const.P) return String.format("Pod number %s too high: accepted %s to %s",
                pod, Const.firstPod, Const.firstPod + Const.P - 1);
        if (tor == null) return "No tor number specified.";
        if (tor <= 0) return String.format("Tor number %s too low: accepted %s to %s",
                tor, 1, Const.W);
        if (tor > Const.W) return String.format("Tor number %s too high: accepted %s to %s",
                tor, 1, Const.W);
        if (server == null) return "No server number specified.";
        if (server < 0) return String.format("Server number %s too low: accepted %s to %s",
                pod, 1, Const.Z);
        if (server > Const.Z) return String.format("Server number %s too high: accepted %s to %s",
                pod, 1, Const.Z);
        int realPod = pod - Const.firstPod;
        int realTor = tor - 1;
        int realServer = server - 1;
        intNode = (realPod * Const.W * Const.Z) + (realTor * Const.Z) + realServer;
        return null;
    }

    public int intNode() {
        if (intNode == null) throw new IllegalStateException("Connection not yet validated.");
        return intNode;
    }

    public boolean validateAndInit() {
        String errorString = auxValidateAndInit();
        if (errorString == null) return true;
        else throw new IllegalArgumentException("Error: " + errorString);
    }
}
