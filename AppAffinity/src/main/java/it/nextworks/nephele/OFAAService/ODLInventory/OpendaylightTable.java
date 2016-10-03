package it.nextworks.nephele.OFAAService.ODLInventory;

import it.nextworks.nephele.OFTranslator.FlowEntry;

import java.util.ArrayList;
import java.util.Set;

public class OpendaylightTable {
	
	private Integer id;
	
	private ArrayList<OpendaylightFlow> flow = new ArrayList<>();
	
	public OpendaylightTable(Set<? extends FlowEntry> input){
		
		id = 0;
		
		Integer i=0;
		for (FlowEntry inFlow : input){
			flow.add(new OpendaylightFlow(inFlow, i.toString(), id));
			i=i+1;
		}
	}

	public Integer getId() {
		return id;
	}

	public ArrayList<OpendaylightFlow> getFlow() {
		return flow;
	}
}
