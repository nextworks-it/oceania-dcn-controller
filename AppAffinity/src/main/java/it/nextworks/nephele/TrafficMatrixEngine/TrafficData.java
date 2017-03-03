package it.nextworks.nephele.TrafficMatrixEngine;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class TrafficData {
	
	private int[][] matrix = new int[Const.P * Const.W * Const.Z][Const.P * Const.W];
	
	private HashMap<String, AppProfile> profiles = new HashMap<>();
	
	private String id = "0";
	
	public synchronized String addProfile(AppProfile appProfile){
		for (Tunnel conn : appProfile.tunnelList){
			matrix[conn.source][(conn.dest / Const.Z)] =
					matrix[conn.source][(conn.dest / Const.Z)] + conn.bandwidth;
		}

        appProfile.id = id;
        profiles.put(id, appProfile);
        String outputId = id;
        Integer temp = (Integer.parseInt(id) + 1);
        id = temp.toString();

		return outputId;
	}
	
	public synchronized boolean deleteProfile(String inputId){
		if (profiles.containsKey(inputId)) {
			for (Tunnel conn : profiles.get(inputId).tunnelList){
				matrix[conn.source][(conn.dest / Const.Z)] =
						matrix[conn.source][(conn.dest / Const.Z)] - conn.bandwidth;
			}
            profiles.remove(inputId);
			return true;
		}
		else return false;
	}

	public int[][] getMatrix(){
		return matrix;
	}
}
