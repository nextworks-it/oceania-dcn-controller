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

	public boolean validateAndInit(){
		if (pod == null || tor == null) return false;
		if ((server == null) || (server >= Const.Z)) return false;
        if (tor >= Const.W) return false;
        if (pod >= Const.P) return false;
        intNode = pod*Const.W + tor;
        return true;
	}

	public int intNode(){
        if (intNode == null) throw new IllegalStateException("Connection not yet validated.");
        return intNode;
	}

}
