package it.nextworks.nephele.OFAAService.ODLInventory;

import java.util.ArrayList;

import it.nextworks.nephele.OFAAService.Inventory.OptOFOutput;

public class OpendaylightActionContainer {
	
	private ArrayList<OpendaylightAction> action = new ArrayList<>();
	
	public OpendaylightActionContainer(OptOFOutput inOutAction){
		action.add(new OpendaylightAction(inOutAction));
	}

	public ArrayList<OpendaylightAction> getAction() {
		return action;
	}

}
