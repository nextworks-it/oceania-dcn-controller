package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.Const;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionEndPoint {
	
	@JsonProperty("Node_ID")
	public String node;
	
	@JsonProperty("Zone_ID")
	public Integer server;

    private int intNode;

	public boolean validateAndInit(){
		if (node == null) return false;
		if ((server == null) || (server >= Const.Z)) return false;
        Matcher matcher = Pattern.compile("^ToR:(\\d+):(\\d+)$").matcher(node);
        if (!matcher.find()) return false;
        int w = Integer.parseInt(matcher.group(1));
        if (w >= Const.W) return false;
        int p = Integer.parseInt(matcher.group(2));
        if (p >= Const.P) return false;
        intNode = p*Const.W + w;
        return true;
	}

	public int intNode(){
        return intNode;
    }

}
