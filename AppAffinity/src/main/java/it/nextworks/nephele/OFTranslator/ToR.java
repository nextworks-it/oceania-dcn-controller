package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;
import it.nextworks.nephele.appaffdb.ExtConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


class ToR extends Node {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private int p;

    private int w;

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

    private void BuildDynFlowChart(Map<Integer, List<ExtConnection>> extConnections) {
        for (Integer port : rackPorts.keySet()) {
            buildFlows(port, extConnections.getOrDefault(port - Const.I, Collections.emptyList()));
        }
    }

    private void buildFlows(Integer inPort, List<ExtConnection> extConn) {
        // inPort is actually inZone.
        boolean[][][] tmpMat = new boolean[Const.P * Const.W][Const.I][Const.T];
        Map<Integer, Set<Integer>> activeDst = new HashMap<>();
        for (Integer i = 0; i < (Const.T * Const.I); i++) {
            Integer dest = Const.matrix[i][torIdentifier * Const.Z + inPort - Const.I - 1] - 1; // 0 means no traffic
            if (dest >= 1 && !dest.equals(torIdentifier)) {
                tmpMat[dest][i / Const.T][i % Const.T] = true;
                activeDst.putIfAbsent(dest, new HashSet<>());
                activeDst.get(dest).add(i / Const.T);
            }
//            for (Integer t = 0; t < (Const.P * Const.W); t++) {
//                tmpMat[t][i / Const.T][i % Const.T] = t.equals(dest);
//            }
        }
        Map<Integer, List<ExtConnection>> perDestConns =
            extConn.stream().collect(Collectors.groupingBy((c) -> (c.dstPod - Const.firstPod) * Const.W + c.dstTor - 1));

        for (Map.Entry<Integer, Set<Integer>> entry : activeDst.entrySet()) {
            Integer dest = entry.getKey();
            Set<Integer> planes = entry.getValue();
//            if (dest.equals(torIdentifier)) {
//                continue;  // Skip intra-rack traffic, that has been taken care of in the static flows
//            }
            List<ExtConnection> extFlows = perDestConns.getOrDefault(dest, Collections.emptyList());
            Integer[] IP = torAddresses.get(dest);
            for (Integer i : planes) {
                Bitmap bitmap = new Bitmap(tmpMat[dest][i]);
                if (bitmap.equals(Bitmap.NULL_BITMAP)) {
                    log.error("GOT NULL BMP! tor {}, dest {}, plane {}, bitmap: {}", torIdentifier, dest, i, bitmap.getBitmap());
                    continue;
                    // Skip null bitmap flows -> no actual traffic
                }
                for (ExtConnection extFlow : extFlows) {
                    Bitmap slice = bitmap.splice(extFlow.bandwidth);
                    dynFlowChart.add(new FlowEntry(
                        encodeIP(extFlow.dstIp),
                        (short) 32,
                        inPort.toString(),
                        (dest % Const.W) + 1,
                        slice,
                        podPorts.get(i),
                        30000 + i
                    ));
                }
                Bitmap other = bitmap.remainingSlice();
                if (!Bitmap.NULL_BITMAP.equals(other)) {
                    dynFlowChart.add(new FlowEntry(
                        IP,
                        (short) 24,
                        inPort.toString(),
                        (dest % Const.W) + 1,
                        other,
                        podPorts.get(i),
                        30000 + i
                    ));
                }
            }
        }
        dynFlowChart.add(new FlowEntry(
                encodeIP("127.0.0.1"),
                (short) 32,
                "1",
                0,
                Bitmap.NULL_BITMAP,
                "2",
                30000
        ));
    }

    private Integer[] encodeIP(String ip) {
        try {
            String[] splitted = ip.split("\\.");
            if (splitted.length != 4) {
                throw new IllegalArgumentException("wrong split");
            }

            Integer[] result = new Integer[4];
            for (int i = 0; i < 4; i++) {
                int number = Integer.parseInt(splitted[i]);
                if (255 < number || 0 > number) {
                    throw new IllegalArgumentException("octet out of bounds: " + splitted[i]);
                }
                result[i] = number;
            }
            return result;
        } catch (IllegalArgumentException exc) {
            throw new IllegalArgumentException("'" + ip + "' is not a valid IP address: " + exc.getMessage() + ".");
        }
    }

    ToR(Integer p, Integer w, Map<Integer, Integer[]> rack,
        Map<Integer, String> inPodPorts, Map<Integer, Integer[]> tors, List<ExtConnection> extConnections) {

        this.p = p;
        this.w = w;
        torIdentifier = Const.W * (p - Const.firstPod) + (w - 1);
        rackPorts = rack;
        podPorts = inPodPorts;
        torAddresses = tors;
        nodeId = String.format("openflow:1%1$02d%2$02d", p, w);

        if (extConnections == null) {
            extConnections = Collections.emptyList();
        }

        Map<Integer, List<ExtConnection>> actualConns =
            extConnections.stream().collect(Collectors.groupingBy(ExtConnection::getSourceServer));

        flowTable = new HashSet<>();
        optFlowTable = new HashSet<>();
        staticFlowChart = new HashSet<>();
        dynFlowChart = new HashSet<>();

        BuildStaticFlowChart();
        BuildDynFlowChart(actualConns);

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