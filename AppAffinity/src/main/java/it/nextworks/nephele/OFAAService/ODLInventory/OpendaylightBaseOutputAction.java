package it.nextworks.nephele.OFAAService.ODLInventory;

import it.nextworks.nephele.OFTranslator.OptOFOutput;

public class OpendaylightBaseOutputAction {

    protected String outPort;

    OpendaylightBaseOutputAction(OptOFOutput inOutAction) {
        this.outPort = inOutAction.getOutputPort();
    }
}
