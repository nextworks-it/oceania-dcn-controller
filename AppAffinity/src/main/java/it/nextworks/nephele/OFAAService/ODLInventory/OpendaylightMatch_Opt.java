package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.OFComprehensiveMatch;

public class OpendaylightMatch_Opt extends OpendaylightMatch {

    private String timeslots;

    private int wavelength;

    private short scheduleId;

    private short flowCounter;

    public OpendaylightMatch_Opt(OFComprehensiveMatch inMatch) {
        super(inMatch);

        timeslots = pad(inMatch.getTimeBitmap());

        wavelength = inMatch.getLambda();

        scheduleId = inMatch.getScheduleId();

        flowCounter = inMatch.getFlowCounter();

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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getInPort() {
        return inPort;
    }
}
