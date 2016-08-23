package it.nextworks.nephele.OFTranslator;

import java.util.HashMap;
import java.util.HashSet;

public class Pod extends Node {
	
	private Integer plane; //plane ID
	
	private Integer podID; //ID of this switch's pod
	
	private HashMap<Integer, String[]> ringPorts = new HashMap<>(); 
	//ring no (= wavelenght no) -> {in port, out port}
	
	private HashMap<Integer, String> torPorts = new HashMap<>(); //wavelength->port

	private void BuildFlowChart(){
		for (Integer lam=0; lam<Const.W ; lam++){
			String tBmp = new String();
			for (Integer t=0; t<Const.T; t++){
				Integer i=0;
				while (i<(Const.P * Const.W)){
					if (Const.matrix[t*Const.I+plane][i] == 0){i= i+1;}
					else if ((Const.matrix[t*Const.I+plane][i]-1) == podID*Const.W+lam){
						tBmp = tBmp + "0";
						break;
					}
					else{ i= i+1;}
				}
				if (i== (Const.P * Const.W)){ //there was no need to drop the wavelenght
					tBmp = tBmp + "1";
				}
			}
			OptOFMatch match = new OptOFMatch(lam, tBmp, "any");
			OptOFOutput out = new OptOFOutput(lam, tBmp, ringPorts.get(lam)[1]);
			flowTable.add(new FlowEntry(match,out));
			Bitmap bmp2 = new Bitmap(tBmp,true);
			OptOFMatch match2 = new OptOFMatch(lam, bmp2, "any");
			OptOFOutput out2 = new OptOFOutput(lam, bmp2, torPorts.get(lam));
			flowTable.add(new FlowEntry(match2,out2));
		}
	}
	
	public Pod(Integer inpl, Integer ID, HashMap<Integer, String[]> inrPorts, HashMap<Integer,String> intPorts){
		flowTable = new HashSet<>();
		plane=inpl;
		podID=ID;
		ringPorts=inrPorts;
		torPorts=intPorts;
		nodeId = "POD:" + podID.toString() + ":" + plane.toString();
		BuildFlowChart();
	}

	public Integer getPlane() {
		return plane;
	}

	public Integer getPodID() {
		return podID;
	}
}
