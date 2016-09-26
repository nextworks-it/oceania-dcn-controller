package it.nextworks.nephele.OFAAService.ODLInventory;

import java.util.ArrayList;
import java.util.Set;

import it.nextworks.nephele.OFAAService.Inventory.FlowEntry;

public class OpendaylightTable {
	
	private Integer id;
	
	private ArrayList<OpendaylightFlow> flow = new ArrayList<>();
	
	public OpendaylightTable(Set<FlowEntry> input){
		
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
