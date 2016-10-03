package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.Inventory.FlowEntry;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


class ToR extends TranslNodeImpl {

	private Integer torIdentifier; 
	/* = pod*W + w
	 * where  w is the wavelenght associated with the tor.
	 */
	
	private Map<Integer, Integer[]> rackPorts; //port -> IP
	private Map<Integer, String> podPorts; //plane ID -> plane-facing port name
	
	private Map<Integer, Integer[]> torAddresses;
	/* Key field is determined as torIdentifier above,
	 * but from the dest server's ToR specifics.
	 * Value field is IP address of a server.
	 */
	
	private Set<TranslFlowEntryImpl> staticFlowChart; //Intra-rack traffic
	private Set<TranslFlowEntryImpl> dynFlowChart; //Inter-rack traffic
		
	private void BuildStaticFlowChart() {
		/* Builds an entry for each in-rack IP forwarding all packets
		 * directed to that IP to the correct port
		 * TODO: use 'ANY' port?
		 */
		for (Map.Entry<Integer, Integer[]> entry: rackPorts.entrySet()){
			Integer[] IP = entry.getValue();
			Integer port = entry.getKey();
			for (String inPort : podPorts.values()){
				staticFlowChart.add(new TranslFlowEntryImpl(new EthOFMatch(IP, inPort), port.toString()));
			}
			for (Integer inPort : rackPorts.keySet()){
				if (!inPort.equals(port)){
					staticFlowChart.add(new TranslFlowEntryImpl(new EthOFMatch(IP, inPort.toString()), port.toString()));
				}
			}
		}
	}

	private void BuildDynFlowChart() {
        for (Integer port : rackPorts.keySet()) {
            BuildFlows(port);
        }
    }

	private void BuildFlows(Integer inPort) {
		boolean[][][] tmpMat = new boolean[Const.P * Const.W][Const.I][Const.T];
		for (Integer i=0; i<(Const.T * Const.I); i++) {
            Integer dest = Const.matrix[i][torIdentifier * Const.Z + inPort] -1; // 0 means no traffic
            for (Integer t = 0; t < (Const.P * Const.W); t++) {
                    tmpMat[t][i / Const.T][i % Const.T] = t.equals(dest);
            }
        }
        for (Integer dest = 0; dest < (Const.P * Const.W); dest++) {
			if (!dest.equals(torIdentifier)) {
				Integer[] IP = torAddresses.get(dest);
				for (Integer i = 0; i < Const.I; i++) {
					dynFlowChart.add(new TranslFlowEntryImpl(
							IP,
							inPort.toString(),
							(dest % Const.W),
							new Bitmap(tmpMat[dest][i]),
							podPorts.get(i)));
				}
			}
        }
	}

	
	ToR(Integer p, Integer w, Map<Integer, Integer[]> rack,
			Map<Integer, String> inPodPorts, Map<Integer, Integer[]> tors) {

		torIdentifier = Const.W * p + w;
		rackPorts = rack;
		podPorts = inPodPorts;
		torAddresses = tors;
		nodeId = "ToR:" + w.toString() + ":" + p.toString();
		
		flowTable = new HashSet<>();
		staticFlowChart = new HashSet<>();
		dynFlowChart = new HashSet<>();
	
		BuildStaticFlowChart();
		BuildDynFlowChart();
				
		flowTable = new HashSet<>();
		
		flowTable.addAll(dynFlowChart);
		flowTable.addAll(staticFlowChart);

	}
}