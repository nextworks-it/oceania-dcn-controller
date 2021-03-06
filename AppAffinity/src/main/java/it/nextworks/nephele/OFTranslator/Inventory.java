package it.nextworks.nephele.OFTranslator;

import java.util.HashMap;
import java.util.Map;

public class Inventory {

    private Map<String, Node> nodes = new HashMap<>();

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public void addNode(String id, Node node) {
        nodes.put(id, node);
    }

    public void setNodes(Map<String, Node> nodes) {
        this.nodes = nodes;
    }
}
