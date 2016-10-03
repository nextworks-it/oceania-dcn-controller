package it.nextworks.nephele.OFAAService.ODLInventory;

import it.nextworks.nephele.OFTranslator.OptOFOutput;

import java.util.ArrayList;

public class OpendaylightActionContainer {
	
	private ArrayList<OpendaylightAction> action = new ArrayList<>();
	
	public OpendaylightActionContainer(OptOFOutput inOutAction){
		action.add(new OpendaylightAction(inOutAction));
	}

	public ArrayList<OpendaylightAction> getAction() {
		return action;
	}

}
