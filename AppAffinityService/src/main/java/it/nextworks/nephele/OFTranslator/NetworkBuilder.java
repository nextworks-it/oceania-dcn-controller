package it.nextworks.nephele.OFTranslator;

import java.util.HashMap;
import java.util.Map;

public class NetworkBuilder {
	
	//private Integer T=Const.T;
	private Integer W=Const.W;
	private Integer P=Const.P;
	private Integer I=Const.I;
	private Integer z=Const.z;
	
	public ToR[] tors;
	public Pod[] pods;
	
	public void build(){

		tors = new ToR[P * W];

		for (Integer p = 0; p < P; p++) {
			for (Integer w = 0; w< W; w++){
				Map<Integer[], String> torRack = new HashMap<>();
				for (Integer i = 0; i < z; i++) {
					torRack.put(new Integer[] { 10, 0, W*p+w+1, i + 1 }, i.toString());
				}
				Map<Integer, String> torPods = new HashMap<>();
				for (Integer i = 0; i < I; i++) {
					torPods.put(i, "plane" + i.toString());
				}
				Map<Integer[], Integer> torAddresses = new HashMap<>();
				for (Integer i = 0; i < P * W; i++) {
					if (i != W*p+w) {
						torAddresses.put(new Integer[] { 10, 0, i+1, 0 }, i);
					}
				}
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
					podRings.get(pID).put(i, new String[] {"Ring"+i.toString()+"in","Ring"+i.toString()+"out"});
				}
				podTors.put(pID, new HashMap<>());
				for (Integer i = 0; i < W; i++) {
					podTors.get(pID).put(i, "ToR"+i.toString());
				}
				pods[pl*P+id]= new Pod(pl, id, podRings.get(pID), podTors.get(pID));
			}
		}
	}
}
