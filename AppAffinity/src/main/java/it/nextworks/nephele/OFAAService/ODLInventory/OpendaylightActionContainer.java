package it.nextworks.nephele.OFAAService.ODLInventory;

import it.nextworks.nephele.OFTranslator.OptOFOutput;

import java.util.ArrayList;

public class OpendaylightActionContainer {

    static OpendaylightActionContainer makeEmulationActions() {
        OpendaylightActionContainer out = new OpendaylightActionContainer();
        out.action.add(OpendaylightAction.makeEmulationActionPop());
        return out;
    }

    private ArrayList<OpendaylightAction> action = new ArrayList<>();

    private OpendaylightActionContainer() {

    }

    public OpendaylightActionContainer(OptOFOutput inOutAction) {
        action.add(new OpendaylightAction(inOutAction));
    }

    public ArrayList<OpendaylightAction> getAction() {
        return action;
    }

}
