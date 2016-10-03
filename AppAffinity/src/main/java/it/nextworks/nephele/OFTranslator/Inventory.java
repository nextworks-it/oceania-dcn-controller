package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.Inventory.Node;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
	
	private HashMap<String, Node> nodes = new HashMap<>();

	public Map<String, Node> getNodes() {
		return nodes;
	}
	
	public void addNode(String id, TranslNodeImpl node){
		nodes.put(id, node);
	}
}
