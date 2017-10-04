package it.nextworks.nephele.OFAAService.ODLInventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.nextworks.nephele.OFTranslator.OFComprehensiveMatch;

public class OpendaylightMatch_Opt extends OpendaylightMatch {

    private String timeslots;

    private int wavelength;

    private short scheduleId;

    private short flowCounter;

    private boolean intra;

    public OpendaylightMatch_Opt(OFComprehensiveMatch inMatch) {
        super(inMatch);

        timeslots = pad(inMatch.getTimeBitmap());

        wavelength = inMatch.getLambda();

        scheduleId = inMatch.getScheduleId();

        flowCounter = inMatch.getFlowCounter();

        intra = inMatch.isIntra();

    }

    public String pad(String str) {
        StringBuilder strBuilder = new StringBuilder(str);
        while (strBuilder.length() < 80) {
            strBuilder.append("0");
        }
        str = strBuilder.toString();
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

    @JsonProperty("intra-bit")
    public boolean getIntra() {
        return intra;
    }
}
