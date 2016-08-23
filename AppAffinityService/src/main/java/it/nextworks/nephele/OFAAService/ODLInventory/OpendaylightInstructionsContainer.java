package it.nextworks.nephele.OFAAService.ODLInventory;

import java.util.ArrayList;

import it.nextworks.nephele.OFAAService.Inventory.OptOFOutput;

public class OpendaylightInstructionsContainer {
	
	private ArrayList<OpendaylightInstruction> instruction= new ArrayList<>();
	
	public OpendaylightInstructionsContainer(OptOFOutput outAction){
		instruction.add(new OpendaylightInstruction(outAction));
	}

	public ArrayList<OpendaylightInstruction> getInstruction() {
		return instruction;
	}
	
}
