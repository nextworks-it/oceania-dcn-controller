package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


class ToR extends Node {

    private static final Bitmap NULL_BITMAP = new Bitmap(new boolean[Const.T]);

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Integer torIdentifier;
    /* = pod*W + w
	 * where  w is the wavelength associated with the tor.
	 */

    private Map<Integer, Integer[]> rackPorts; //port -> IP
    private Map<Integer, String> podPorts; //plane ID -> plane-facing port name

    private Map<Integer, Integer[]> torAddresses;
	/* Key field is determined as torIdentifier above,
	 * but from the dest server's ToR specifics.
	 * Value field is IP address of a server.
	 */

    private Set<FlowEntry> staticFlowChart; //Intra-rack traffic
    private Set<FlowEntry> dynFlowChart; //Inter-rack traffic

    private void BuildStaticFlowChart() {
		/* Builds an entry for each in-rack IP forwarding all packets
		 * directed to that IP to the correct port
		 */
        for (Map.Entry<Integer, Integer[]> entry : rackPorts.entrySet()) {
            Integer[] IP = entry.getValue();
            Integer port = entry.getKey();
            staticFlowChart.add(new FlowEntry(new EthOFMatch(IP, (short) 32), port.toString()));
        }
    }

    private void BuildDynFlowChart() {
        for (Integer port : rackPorts.keySet()) {
            buildFlows(port);
        }
    }

    private void buildFlows(Integer inPort) {
        // inPort is actually inZone.
        boolean[][][] tmpMat = new boolean[Const.P * Const.W][Const.I][Const.T];
        for (Integer i = 0; i < (Const.T * Const.I); i++) {
            Integer dest = Const.matrix[i][torIdentifier * Const.Z + inPort - Const.I - 1] - 1; // 0 means no traffic
            for (Integer t = 0; t < (Const.P * Const.W); t++) {
                tmpMat[t][i / Const.T][i % Const.T] = t.equals(dest);
            }
        }
        for (Integer dest = 0; dest < (Const.P * Const.W); dest++) {
            if (!dest.equals(torIdentifier)) {
                Integer[] IP = torAddresses.get(dest);
                for (Integer i = 0; i < Const.I; i++) {
                    Bitmap bitmap = new Bitmap(tmpMat[dest][i]);
                    if (!bitmap.equals(NULL_BITMAP)) {
                        // Skip null bitmap flows
                        dynFlowChart.add(new FlowEntry(
                                IP,
                                (short) 24,
                                inPort.toString(),
                                (dest % Const.W),
                                new Bitmap(tmpMat[dest][i]),
                                podPorts.get(i),
                                30000 + i
                        ));
                    }
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
        nodeId = String.format("openflow:1%1$02d0%2$02d", p + 1, w + 1);

        flowTable = new HashSet<>();
        optFlowTable = new HashSet<>();
        staticFlowChart = new HashSet<>();
        dynFlowChart = new HashSet<>();

        BuildStaticFlowChart();
        BuildDynFlowChart();

        optFlowTable.addAll(dynFlowChart);
        flowTable.addAll(staticFlowChart);

        short totalFlows = (short) dynFlowChart.size();
        optFlowTable.forEach(
                (f) -> {
                    f.getMatch().setFlowCounter(totalFlows);
                    f.getOutput().setFlowCounter(totalFlows);
                }
        );

    }
}