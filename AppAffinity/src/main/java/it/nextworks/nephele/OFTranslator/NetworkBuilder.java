package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;

import java.util.HashMap;
import java.util.Map;

class NetworkBuilder {

    private static short scheduleCounter = 0;

    //private Integer T=Const.T;
    private Integer W = Const.W;
    private Integer P = Const.P;
    private Integer R = Const.R;
    private Integer I = Const.I;
    private Integer Z = Const.Z;

    private ToR[] tors;
    private Pod[] pods;

    Inventory build() {

        tors = new ToR[P * W];

        Map<Integer, Integer[]> torAddressesMap = buildTorAddress();

        for (Integer p = 0; p < P; p++) {
            for (Integer w = 0; w < W; w++) {
                Map<Integer, Integer[]> torRack = new HashMap<>();
                for (Integer i = 0; i < Z; i++) {
                    torRack.put(I + i + 1, new Integer[]{10, p + 1, w + 1, i + 1});
                }
                Map<Integer, String> torPods = new HashMap<>();
                for (Integer i = 0; i < I; i++) {
                    torPods.put(i, String.valueOf(i + 1));
                }
                Map<Integer, Integer[]> torAddresses = new HashMap<>(torAddressesMap);
                torAddresses.remove(p * Const.W + w);
                tors[W * p + w] = new ToR(p, w, torRack, torPods, torAddresses);
            }
        }

        HashMap<Integer[], HashMap<Integer, String[]>> podRings = new HashMap<>();
        HashMap<Integer[], HashMap<Integer, String>> podTors = new HashMap<>();

        pods = new Pod[P * I];

        for (Integer pl = 0; pl < I; pl++) {
            for (Integer id = 0; id < P; id++) {

                Integer[] pID = new Integer[]{pl, id};
                podRings.put(pID, new HashMap<>());
                for (Integer i = 0; i < W; i++) {
                    Integer ringUsed = 1 + (i % R); // in case R < W
                    podRings.get(pID).put(
                            i, new String[]{String.valueOf(2 * ringUsed - 1), String.valueOf(2 * ringUsed)}
                            );
                }
                podTors.put(pID, new HashMap<>());
                for (Integer i = 0; i < W; i++) {
                    podTors.get(pID).put(i, String.valueOf(2 * R + i + 1));
                }
                pods[pl * P + id] = new Pod(pl, id, podRings.get(pID), podTors.get(pID));
            }
        }
        return makeInv();
    }

    private Inventory makeInv() {
        Inventory inventory = new Inventory();
        for (Node node : pods) {
            setSchedule(node);
            inventory.addNode(node.getNodeId(), node);
        }
        for (Node node : tors) {
            setSchedule(node);
            inventory.addNode(node.getNodeId(), node);
        }
        scheduleCounter = (short) (scheduleCounter + 1);
        return inventory;
    }

    private void setSchedule(Node node) {
        node.getOptFlowTable().forEach((f) -> {
            f.getMatch().setScheduleId(scheduleCounter);
            f.getOutput().setScheduleId(scheduleCounter);
        });
    }

    private Map<Integer, Integer[]> buildTorAddress() {
        Map<Integer, Integer[]> output = new HashMap<>();
        for (Integer p = 0; p < P; p++) {
            for (Integer w = 0; w < W; w++) {
                output.put(p * W + w, new Integer[]{10, p + 1, w + 1, 0});
            }
        }
        return output;
    }
}
