package it.nextworks.nephele.OFTranslator;

public class OFOutput {

    protected String outputPort;

    public OFOutput(String port) {
        outputPort = port;
    }

    public String getOutputPort() {
        return outputPort;
    }

    public OFOutput() {

    }

    public void setOutputPort(String inOutputPort) {
        outputPort = inOutputPort;
    }

}
