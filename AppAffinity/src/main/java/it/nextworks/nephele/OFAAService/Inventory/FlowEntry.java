package it.nextworks.nephele.OFAAService.Inventory;


import it.nextworks.nephele.OFTranslator.OFOutput;

public interface FlowEntry {
    OFComprehensiveMatch getMatch();

    OFOutput getOutput();

}
