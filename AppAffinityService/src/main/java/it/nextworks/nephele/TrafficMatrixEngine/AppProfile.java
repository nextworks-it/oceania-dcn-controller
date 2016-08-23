package it.nextworks.nephele.TrafficMatrixEngine;

import java.util.ArrayList;

import io.swagger.annotations.ApiModelProperty;

public class AppProfile {
	
	@ApiModelProperty(notes="The ID of the application profile")
	private String id;
	
	@ApiModelProperty(notes="The list of tunnels required")
	private ArrayList<Tunnel> connList = new ArrayList<>();
	
	public AppProfile(String inputId, ArrayList<Tunnel> inputList){
		id = inputId;
		connList = inputList;
	}


	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<Tunnel> getConnList() {
		return connList;
	}

	public void setConnList(ArrayList<Tunnel> connList) {
		this.connList = connList;
	}
	
	public AppProfile(){
		
	}

}
