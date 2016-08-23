package it.nextworks.nephele.OFTranslator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//import org.slf4j.LoggerFactory;
//import org.slf4j.Logger;

public class ToR extends Node {
		
	@SuppressWarnings("unused")
	private Integer pod;
	
	@SuppressWarnings("unused")
	private Integer wavelength;
	
	private Integer torIdentifier; 
	/* = pod*w + w 
	 * where  w is the wavelenght associated with the tor.
	 */
	
	private Map<Integer[], String> rackPorts; //IP->port
	private Map<Integer, String> podPorts; //plane ID -> plane name
	
	private Map<Integer[], Integer> torIDs; 
	/* Key field is IP address of a server,
	 * value field is determined as torIdentifier above,
	 * but from the dest server's ToR specifics
	 */
	
	private Set<FlowEntry> staticFlowChart; //Intra-rack traffic
	private Set<FlowEntry> dynFlowChart; //Inter-rack traffic
		
	public void BuildStaticFlowChart() {
		/* Builds an entry for each in-rack IP and each rack in-port,
		 * excluding rackPorts(IP)==inport, of course
		 */
		for (Integer[] IP : rackPorts.keySet()){
			for (String inPort : podPorts.values()){
				staticFlowChart.add(new FlowEntry(new EthOFMatch(IP, inPort), rackPorts.get(IP)));
			}
			for (String inPort : rackPorts.values()){
				if (inPort != rackPorts.get(IP)){
					staticFlowChart.add(new FlowEntry(new EthOFMatch(IP, inPort), rackPorts.get(IP)));
				}
			}
		}
	}
	
	public void BuildDynFlowChart(){
		for (Integer[] IP : torIDs.keySet()){
			for (String port : rackPorts.values()){
				//System.out.print("BuildFlows(");
				//System.out.println(IP[0].toString()+"."+IP[1].toString()+"."+IP[2].toString()+"."+IP[3].toString()+", " + port+ ")");
				BuildFlows(IP, port);
			}
		}
	}
	
	private void BuildFlows(Integer[] IP, String inPort) {
		String[] tmpMat = new String[Const.I];
		for (Integer i=0; i<Const.I;i++){tmpMat[i] = "";}
		for (Integer i=0; i<(Const.T * Const.I); i++) {
			if (Const.matrix[i][torIdentifier] == 0) {
				tmpMat[i % Const.I] = tmpMat[i % Const.I] + "0";
			}
			else if ((Const.matrix[i][torIdentifier]-1) == torIDs.get(IP)) {
				tmpMat[i % Const.I] = tmpMat[i % Const.I] + "1";
			}
			else tmpMat[i % Const.I] = tmpMat[i % Const.I] + "0";
		}
		for (Integer i=0; i<Const.I; i++){
			//log.info(tmpMat[i]);
			dynFlowChart.add(new FlowEntry(IP, inPort, (torIDs.get(IP) % Const.W), new Bitmap(tmpMat[i]), podPorts.get(i)));
	    }
	}
	
	public ToR(Integer p, Integer w, Map<Integer[], String> rack,
			Map<Integer, String> inPodPorts, Map<Integer[], Integer> tors) {

		pod = p;
		wavelength = w;
		torIdentifier = Const.W * p + w;
		rackPorts = rack;
		podPorts = inPodPorts;
		torIDs = tors;
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