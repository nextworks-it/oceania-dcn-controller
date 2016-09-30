package it.nextworks.nephele.OFTranslator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;

class Pod extends Node {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Integer plane; //plane ID
	
	private Integer podID; //ID of this switch's pod
	
	private HashMap<Integer, String[]> ringPorts = new HashMap<>(); 
	//ring no (= wavelenght no) -> {in port, out port}
	
	private HashMap<Integer, String> torPorts = new HashMap<>(); //wavelength->port

	private void BuildFlowChart(){
		for (Integer lam=0; lam<Const.W ; lam++){
			StringBuilder bmpBuilder = new StringBuilder("");
			for (Integer t=0; t<Const.T; t++){
				Integer i=0;
				while (i<(Const.P * Const.W * Const.Z)){ //Check if it should be forwarded
					if (Const.matrix[t + (plane * Const.T)][i] == 0){i= i+1;}
					else if ((Const.matrix[t + (plane * Const.T)][i]-1) == podID*Const.W+lam){
						bmpBuilder.append('0'); //must be dropped, hence not forwarded.
						break;
					}
					else{ i= i+1;}
				}
				if (i== (Const.P * Const.W * Const.Z)){ //there was no need to drop the wavelength
					bmpBuilder.append('1');
				}
			}
			try {
				String tBmp = bmpBuilder.toString();
				OptOFMatch match = new OptOFMatch(lam, tBmp, "any");
				OptOFOutput out = new OptOFOutput(lam, tBmp, ringPorts.get(lam)[1]);
				flowTable.add(new FlowEntry(match, out));
				Bitmap bmp2 = Bitmap.inverting(tBmp);
				OptOFMatch match2 = new OptOFMatch(lam, bmp2, "any");
				OptOFOutput out2 = new OptOFOutput(lam, bmp2, torPorts.get(lam));
				flowTable.add(new FlowEntry(match2, out2));
			}
			catch (IllegalArgumentException exc) {
				log.error("Pod: " + nodeId + " while processing wavelength " + lam.toString() +
                " got exception ", exc);
                throw exc;
			}
		}
	}
	
	Pod(Integer inpl, Integer ID, HashMap<Integer, String[]> inrPorts, HashMap<Integer,String> intPorts){
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
