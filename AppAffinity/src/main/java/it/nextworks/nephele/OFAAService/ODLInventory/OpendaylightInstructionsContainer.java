package it.nextworks.nephele.OFAAService.ODLInventory;

import it.nextworks.nephele.OFTranslator.OptOFOutput;

import java.util.ArrayList;

public class OpendaylightInstructionsContainer {

    static OpendaylightInstructionsContainer makeEmulationInstructions(boolean popping) {
        OpendaylightInstructionsContainer out = new OpendaylightInstructionsContainer();
        if (popping) {
            out.instruction.add(OpendaylightInstruction.makeEmulationInstructionPop());
            out.instruction.add(OpendaylightInstruction.makeEmulationInstructionForward(1));
        } else {
            out.instruction.add(OpendaylightInstruction.makeEmulationInstructionForward(0));
        }
        return out;
    }

    private ArrayList<OpendaylightInstruction> instruction = new ArrayList<>();

    private OpendaylightInstructionsContainer() {

    }

    public OpendaylightInstructionsContainer(OptOFOutput outAction) {
        instruction.add(new OpendaylightInstruction(outAction));
    }

    public ArrayList<OpendaylightInstruction> getInstruction() {
        return instruction;
    }

}
