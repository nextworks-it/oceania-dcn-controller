package it.nextworks.nephele.OFAAService.ODLInventory;


import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.OptOFOutput;

public class OpendaylightOutputAction_Opt extends OpendaylightBaseOutputAction {

    private String timeslots;

    private int wavelength;

    private short scheduleId;

    private short flowCounter;


    public OpendaylightOutputAction_Opt(OptOFOutput inOutAction) {
        super(inOutAction);

        timeslots = pad(inOutAction.getTimeBitmap());

        wavelength = inOutAction.getLambda();

        scheduleId = inOutAction.getScheduleId();

        flowCounter = inOutAction.getFlowCounter();

    }

    public String pad(String str) {
        while (str.length() < 80) {
            str = str + "0";
        }
        return str;
    }

    public String getTimeslots() {
        return timeslots;
    }

    public int getWavelength() {
        return wavelength;
    }

    @JsonProperty("schedule-id")
    public short getScheduleId() {
        return scheduleId;
    }

    @JsonProperty("flow-counter")
    public short getFlowCounter() {
        return flowCounter;
    }

    @JsonProperty("wport")
    public String getOutPort() {
        return outPort;
    }
}
