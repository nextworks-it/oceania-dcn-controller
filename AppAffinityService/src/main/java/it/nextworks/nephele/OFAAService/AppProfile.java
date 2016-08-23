package it.nextworks.nephele.OFAAService;

import java.util.ArrayList;

/**
 * Contains a list of tunnels required by an application
 * @author MCapitani
 *
 */

public class AppProfile {
	
	private String id;
	public ArrayList<Tunnel> tunnelList = new ArrayList<>();
	
	public AppProfile(String inputId, ArrayList<Tunnel> inputList){
		id = inputId;
		tunnelList = inputList;
	}
	
	public AppProfile(ArrayList<Tunnel> inputList){
		tunnelList = inputList;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<Tunnel> getConnList() {
		return tunnelList;
	}

	public AppProfile(){
		tunnelList = new ArrayList<>();

	}

}
