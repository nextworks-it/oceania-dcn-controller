package it.nextworks.nephele.OFTranslator;

import java.util.HashMap;
import java.util.Map;

class NetworkBuilder {
	
	//private Integer T=Const.T;
	private Integer W=Const.W;
	private Integer P=Const.P;
    private Integer R=Const.R;
	private Integer I=Const.I;
	private Integer Z=Const.Z;
	
	private ToR[] tors;
	private Pod[] pods;
	
	Inventory build(){

		tors = new ToR[P * W];

        Map<Integer, Integer[]> torAddressesMap = buildTorAddress();

		for (Integer p = 0; p < P; p++) {
			for (Integer w = 0; w< W; w++){
				Map<Integer, Integer[]> torRack = new HashMap<>();
				for (Integer i = 0; i < Z; i++) {
					torRack.put(i, new Integer[] { 10, 0, W*p+w+1, i + 1 });
				}
				Map<Integer, String> torPods = new HashMap<>();
				for (Integer i = 0; i < I; i++) {
					torPods.put(i, "plane" + i.toString());
				}
				Map<Integer, Integer[]> torAddresses = new HashMap<>(torAddressesMap);
                torAddresses.remove(p * Const.W + w);
				tors[W*p+w] = new ToR(p, w, torRack, torPods, torAddresses);
			}
		}

        HashMap<Integer[], HashMap<Integer, String[]>> podRings = new HashMap<>();
        HashMap<Integer[], HashMap<Integer, String>> podTors = new HashMap<>();

		pods = new Pod[P * I];

		for (Integer pl = 0; pl < I; pl++) {
			for (Integer id = 0; id < P; id++) {

				Integer[] pID= new Integer[] {pl,id};
				podRings.put(pID, new HashMap<>());
				for (Integer i = 0; i < W; i++) {
                    Integer ringUsed = i % R; // in case R < W
					podRings.get(pID).put(i, new String[] {"Ring"+ringUsed.toString()+"in","Ring"+ringUsed.toString()+"out"});
				}
				podTors.put(pID, new HashMap<>());
				for (Integer i = 0; i < W; i++) {
					podTors.get(pID).put(i, "ToR"+i.toString());
				}
				pods[pl*P+id]= new Pod(pl, id, podRings.get(pID), podTors.get(pID));
			}
		}
		return makeInv();
	}

	private Inventory makeInv(){
        Inventory inventory = new Inventory();
        for (TranslNodeImpl node : pods){
            inventory.addNode(node.getNodeId(), node);
        }
        for (TranslNodeImpl node : tors){
            inventory.addNode(node.getNodeId(), node);
        }
        return inventory;
    }

    private Map<Integer, Integer[]> buildTorAddress(){
        Map<Integer, Integer[]> output = new HashMap<>();
        for (Integer i = 0; i < (P * W); i++) {
            output.put(i, new Integer[]{10, 0, i + 1, 0});
        }
        return output;
    }
}
