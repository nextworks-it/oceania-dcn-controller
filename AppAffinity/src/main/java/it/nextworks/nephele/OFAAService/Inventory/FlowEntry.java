package it.nextworks.nephele.OFAAService.Inventory;


import it.nextworks.nephele.OFTranslator.OptOFOutput;

public interface FlowEntry {
    OFComprehensiveMatch getMatch();

    OptOFOutput getOutput();

}
