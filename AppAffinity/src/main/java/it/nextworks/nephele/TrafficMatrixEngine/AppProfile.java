package it.nextworks.nephele.TrafficMatrixEngine;

import java.util.ArrayList;

import io.swagger.annotations.ApiModelProperty;

/**
 * Contains a list of tunnels required by an application
 * @author MCapitani
 *
 */

public class AppProfile {
	
	@ApiModelProperty(notes="The ID of the application profile")
	public String id;
	
	@ApiModelProperty(notes="The list of tunnels required")
	public ArrayList<Tunnel> tunnelList = new ArrayList<>();
	
	public AppProfile(String inputId, ArrayList<Tunnel> inputList){
		id = inputId;
		tunnelList = inputList;
	}

	public AppProfile(){
		
	}

}
