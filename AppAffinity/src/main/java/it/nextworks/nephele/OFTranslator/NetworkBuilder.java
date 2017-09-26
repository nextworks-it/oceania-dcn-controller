package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;
import it.nextworks.nephele.appaffdb.DbManager;
import it.nextworks.nephele.appaffdb.ExtConnection;

import java.util.*;
import java.util.stream.Collectors;

class NetworkBuilder {


    private DbManager db;

    private static short scheduleCounter = 0;

    //private Integer T=Const.T;
    private Integer W = Const.W;
    private Integer P = Const.P;
    private Integer R = Const.R;
    private Integer I = Const.I;
    private Integer Z = Const.Z;
    private Integer startP = Const.firstPod;

    private ToR[] tors;
    private Pod[] pods;

    NetworkBuilder(DbManager db) {
        this.db = db;
    }

    Inventory build() {

        tors = new ToR[P * W];

        Map<Integer, Integer[]> torAddressesMap = buildTorAddress();


        List<ExtConnection> extConnections = db.queryExtConn();
        if (null == extConnections) {
            extConnections = Collections.emptyList();
        }

        for (Integer p = 0; p < P; p++) {
            Integer podNo = p + startP;
            for (Integer w = 0; w < W; w++) {
                Map<Integer, Integer[]> torRack = new HashMap<>();
                for (Integer i = 0; i < Z; i++) {
                    torRack.put(I + i + 1, new Integer[]{10, podNo, w + 1, i + 1});
                }
                Map<Integer, String> torPods = new HashMap<>();
                for (Integer i = 0; i < I; i++) {
                    torPods.put(i, String.valueOf(i + 1));
                }
                Map<Integer, Integer[]> torAddresses = new HashMap<>(torAddressesMap);
                torAddresses.remove(p * W + w);
                Map<List<Integer>, List<ExtConnection>> extConnMap
                        = extConnections.stream().collect(Collectors.groupingBy(ExtConnection::getTor));
                ArrayList<Integer> key = new ArrayList<>();
                key.add(podNo);
                key.add(w + 1);
                tors[W * p + w] = new ToR(podNo, w, torRack, torPods, torAddresses, extConnMap.get(key));
            }
        }

        HashMap<Integer[], HashMap<Integer, String[]>> podRings = new HashMap<>();
        HashMap<Integer[], HashMap<Integer, String>> podTors = new HashMap<>();

        pods = new Pod[P * I];

        for (Integer pl = 0; pl < I; pl++) {
            for (Integer id = 0; id < P; id++) {
                Integer podNo = id + startP;

                Integer[] pID = new Integer[]{pl, podNo};
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
                pods[pl * P + id] = new Pod(pl, podNo, podRings.get(pID), podTors.get(pID));
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
        if (scheduleCounter > 255) {
            scheduleCounter = 0;
        }
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
            Integer podNo = p + startP;
            for (Integer w = 0; w < W; w++) {
                output.put(p * W + w, new Integer[]{10, podNo, w + 1, 0});
            }
        }
        return output;
    }
}
