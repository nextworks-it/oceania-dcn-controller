package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.Const;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionEndPoint {

    @JsonProperty("Pod_ID")
    public Integer pod;

    @JsonProperty("ToR_ID")
    public Integer tor;

    @JsonProperty("Zone_ID")
    public Integer server;

    private Integer intNode;

    private boolean auxValidateAndInit() {
        if ((pod == null) || (pod >= Const.P)) return false;
        if ((tor == null) || (tor >= Const.W)) return false;
        if ((server == null) || (server >= Const.Z)) return false;
        intNode = pod * Const.W * Const.Z + tor * Const.Z + server;
        return true;
    }

    public int intNode() {
        if (intNode == null) throw new IllegalStateException("Connection not yet validated.");
        return intNode;
    }

    public boolean validateAndInit() {
        if (auxValidateAndInit()) return true;
        else throw new IllegalArgumentException("Connection end point identifiers null or out of bounds.");
    }
}
