package opticaltranslator.nephele.flowutils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalResourceAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;

/**
 * Created by Marco Capitani on 03/07/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
public class MetadataMakerNew implements MetadataMaker {

    @Nonnull
    @Override
    public Metadata buildMetadata(@Nullable OpticalResourceAttributes optMatch,
                                  @Nullable OpticalResourceAttributes optOutput,
                                  @Nonnull NepheleFlowAttributes nepheleData,
                                  boolean matchActionBit)
            throws FlowParserException {

        String tempMatchWavelength;
        String timeslots = null;
        if (optMatch == null) {
            tempMatchWavelength = "";
        } else {
            tempMatchWavelength = optMatch.getWavelength().toString(2);
            timeslots = optMatch.getTimeslots();
            if (timeslots.length() != 80) {
                throw new FlowParserException("Metadata length is not 80, not supported.");
            }
        }
        if (tempMatchWavelength.length() > 8) {
            throw new FlowParserException("Match wavelength is longer than 8 bit, not supported.");
        }
        String matchWavelength = lPad(tempMatchWavelength, (short) 8);

        String scheduleId = lPad(Integer.toBinaryString(nepheleData.getScheduleId()), (short) 8);

        String flowCounter = lPad(Integer.toBinaryString(nepheleData.getFlowCounter()), (short) 8);

        String tempActionWavelength;
        if (optOutput == null) {
            tempActionWavelength = "";
        } else {
            tempActionWavelength = optOutput.getWavelength().toString(2);
            if (timeslots == null) {
                timeslots = optOutput.getTimeslots();
                if (timeslots.length() != 80) {
                    throw new FlowParserException("Metadata length is not 80, not supported.");
                }
            }
        }
        if (tempActionWavelength.length() > 8) {
            throw new FlowParserException("Match wavelength is longer than 8 bit, not supported.");
        }
        String actionWavelength = lPad(tempActionWavelength, (short) 8);

        if (timeslots == null) {
            throw new FlowParserException("Either opt match or opt output must be not null.");
        }
        String strMatchActionBit = matchActionBit ? "1" : "0";

        String metadataStr = timeslots.substring(0, 64);
        String maskStr =
                timeslots.substring(64)
                        + matchWavelength
                        + scheduleId
                        + flowCounter
                        + actionWavelength
                        + strMatchActionBit;

        assert maskStr.length() == 49;

        maskStr = rPad(maskStr, (short) 64);

        BigInteger metadata = new BigInteger(
                metadataStr,
                2
        );

        BigInteger metadataMask = new BigInteger(
                maskStr,
                2
        );

        return new MetadataBuilder()
                .setMetadata(metadata)
                .setMetadataMask(metadataMask)
                .build();
    }

    private String lPad(String inString, short size) throws FlowParserException {
        StringBuilder outString = new StringBuilder("");
        if (inString.length() > size) {
            throw new FlowParserException("field size is greater than " + String.valueOf(size) + ", not supported.");
        } else if (inString.length() < size) {
            short missingZeroes = (short) (size - inString.length());
            for (short i = 0; i < missingZeroes; i++) {
                outString.append("0");
            }
        }
        outString.append(inString);
        return outString.toString();
    }


    private String rPad(String inString, short size) throws FlowParserException {
        StringBuilder outString = new StringBuilder("");
        outString.append(inString);
        if (inString.length() > size) {
            throw new FlowParserException("field size is greater than " + String.valueOf(size) + ", not supported.");
        }
        else if (inString.length() < size) {
            short missingZeroes = (short) (size - inString.length());
            for (short i = 0; i < missingZeroes; i++) {
                outString.append("0");
            }
        }
        return outString.toString();
    }
}