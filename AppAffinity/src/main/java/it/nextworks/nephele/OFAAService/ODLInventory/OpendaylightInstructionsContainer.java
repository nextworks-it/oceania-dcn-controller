package it.nextworks.nephele.OFAAService.ODLInventory;

import it.nextworks.nephele.OFTranslator.OptOFOutput;

import java.util.ArrayList;

public class OpendaylightInstructionsContainer {
	
	private ArrayList<OpendaylightInstruction> instruction= new ArrayList<>();
	
	public OpendaylightInstructionsContainer(OptOFOutput outAction){
		instruction.add(new OpendaylightInstruction(outAction));
	}

	public ArrayList<OpendaylightInstruction> getInstruction() {
		return instruction;
	}
	
}
