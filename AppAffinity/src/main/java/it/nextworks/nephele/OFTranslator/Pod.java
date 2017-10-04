package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class Pod extends Node {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Integer plane; //plane ID

    private Integer podID; //ID of this switch's pod

    private Map<Integer, String[]> ringPorts = new HashMap<>();
    //ring no (= wavelenght no) -> {in port, out port}

    private Map<Integer, String> torPorts = new HashMap<>(); //wavelength->port

    private void buildFlowChart() {
        for (Integer lam = 0; lam < Const.W; lam++) {
            StringBuilder intraBmpBuilder = new StringBuilder("");
            StringBuilder interBmpBuilder = new StringBuilder("");
            StringBuilder forwardBmpBuilder = new StringBuilder("");
            for (Integer t = 0; t < Const.T; t++) {
                Integer i = 0;
                while (i < (Const.P * Const.W * Const.Z)) { //Check if it should be forwarded
                    if (Const.matrix[t + (plane * Const.T)][i] == 0) {
                        i = i + 1;
                    } else if ((Const.matrix[t + (plane * Const.T)][i] - 1) == (podID - Const.firstPod) * Const.W + lam) {
                        if (((i / (Const.W * Const.Z)) + Const.firstPod) == podID) {
                            intraBmpBuilder.append('1'); //must be dropped, hence not forwarded.
                            interBmpBuilder.append('0');
                            forwardBmpBuilder.append('0');
                        } else {
                            interBmpBuilder.append('1');
                            intraBmpBuilder.append('0');
                            forwardBmpBuilder.append('0');
                        }
                        break;
                    } else {
                        i = i + 1;
                    }
                }
                if (i == (Const.P * Const.W * Const.Z)) { //there was no need to drop the wavelength
                    forwardBmpBuilder.append('1');
                    intraBmpBuilder.append('0');
                    interBmpBuilder.append('0');
                }
            }
            try {
                String fBmp = forwardBmpBuilder.toString();
                OptOFMatch match = new OptOFMatch(lam, fBmp);
                OptOFOutput out = new OptOFOutput(lam, fBmp, ringPorts.get(lam)[1]);
                optFlowTable.add(new FlowEntry(match, out));

                String inBmp = intraBmpBuilder.toString();
                if (inBmp.contains("1")) {
                    OptOFMatch match2 = new OptOFMatch(lam, inBmp);
                    OptOFOutput out2 = new OptOFOutput(lam, inBmp, torPorts.get(lam));
                    optFlowTable.add(new FlowEntry(match2, out2, true));
                }
                String exBmp = interBmpBuilder.toString();
                if (exBmp.contains("1")) {
                    OptOFMatch match3 = new OptOFMatch(lam, exBmp);
                    OptOFOutput out3 = new OptOFOutput(lam, exBmp, torPorts.get(lam));
                    optFlowTable.add(new FlowEntry(match3, out3, false));
                }

            } catch (IllegalArgumentException exc) {
                log.error("Pod: " + nodeId + " while processing wavelength " + lam.toString() +
                    " got exception ", exc);
                throw exc;
            }
        }
    }

    Pod(Integer inpl, Integer ID, Map<Integer, String[]> inrPorts, Map<Integer, String> intPorts) {
        flowTable = new HashSet<>();
        optFlowTable = new HashSet<>();
        plane = inpl;
        podID = ID;
        ringPorts = inrPorts;
        torPorts = intPorts;
        nodeId = String.format("openflow:2%1$02d%2$02d", plane + 1, podID);
        buildFlowChart();

        short totalFlows = (short) optFlowTable.size();
        optFlowTable.forEach(
            (f) -> {
                f.getMatch().setFlowCounter(totalFlows);
                f.getOutput().setFlowCounter(totalFlows);
            }
        );
    }

    public Integer getPlane() {
        return plane;
    }

    public Integer getPodID() {
        return podID;
    }
}
