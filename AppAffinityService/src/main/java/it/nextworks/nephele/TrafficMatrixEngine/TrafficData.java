package it.nextworks.nephele.TrafficMatrixEngine;

import java.util.HashMap;

public class TrafficData {
	
	private static int[][] matrix = new int[Const.P * Const.W][Const.P * Const.W];
	
	private static HashMap<String,AppProfile> profiles = new HashMap<>();
	
	private static String id = "0";
	
	public static String addProfile(AppProfile appProfile){
		for (Tunnel conn : appProfile.getConnList()){
			matrix[conn.getIntSource()][conn.getIntDest()] = 
					matrix[conn.getIntSource()][conn.getIntDest()] + conn.getBandwidth();
		}
		appProfile.setId(id);
		profiles.put(id,appProfile);
		String assignedId = id;
		Integer temp = (Integer.parseInt(id)+1);
		id = temp.toString();
		return assignedId;
	}
	
	public static boolean deleteProfile(String inputId){
		if (profiles.containsKey(inputId)) {
			for (Tunnel conn : profiles.get(inputId).getConnList()){
				matrix[conn.getIntSource()][conn.getIntDest()] = 
						matrix[conn.getIntSource()][conn.getIntDest()] - conn.getBandwidth();
			}
			profiles.remove(inputId);
			return true;
		}
		else return false;
	}

	public static int[][] getMatrix(){
		return matrix;
	}
}
