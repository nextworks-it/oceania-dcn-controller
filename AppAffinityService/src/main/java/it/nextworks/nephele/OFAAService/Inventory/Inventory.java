package it.nextworks.nephele.OFAAService.Inventory;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
	
	private HashMap<String, Node> nodes = new HashMap<>();

	public Map<String, Node> getNodes() {
		return nodes;
	}
	
	public void addNode(String id, Node node){
		nodes.put(id, node);
	}
}
