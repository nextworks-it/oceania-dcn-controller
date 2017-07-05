/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele.flowutils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.nephele.rev161228.NepheleFlowAttributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalResourceAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;

public class MetadataMakerOld implements MetadataMaker {

    MetadataMakerOld() {

    }

    @Override
    public @Nonnull Metadata buildMetadata(@Nullable OpticalResourceAttributes optMatch,
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
        String matchWavelength = pad(tempMatchWavelength, (short) 8);

        String scheduleId = pad(Integer.toBinaryString(nepheleData.getScheduleId()), (short) 8);

        String flowCounter = pad(Integer.toBinaryString(nepheleData.getFlowCounter()), (short) 8);

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
        String actionWavelength = pad(tempActionWavelength, (short) 8);

        if (timeslots == null) {
            throw new FlowParserException("Either opt match or opt output must be not null.");
        }
        String strMatchActionBit = matchActionBit ? "1" : "0";

        BigInteger allData = new BigInteger(
                timeslots
                        + matchWavelength
                        + scheduleId
                        + flowCounter
                        + actionWavelength
                        + strMatchActionBit,
                2
        );

        System.out.println("all data: " + allData.toString(2));
        System.out.println("all data: " + allData.toString(10));
        String strData = allData.toString(10);
        String strMetadata;
        String strMetadataMask;
        if (strData.length() < 19) {
            strMetadata = strData;
            strMetadataMask = "0";
        } else {
            strMetadata = strData.substring(0, 18);
            strMetadataMask = strData.substring(18);
        }

        BigInteger metadata = new BigInteger(strMetadata, 10);
        BigInteger metadataMask = new BigInteger(strMetadataMask, 10);

        return new MetadataBuilder()
                .setMetadata(metadata)
                .setMetadataMask(metadataMask)
                .build();
    }

    private String pad(String inString, short size) throws FlowParserException {
        StringBuilder outString = new StringBuilder("");
        if (inString.length() > size) {
            throw new FlowParserException("field size is greater than " + String.valueOf(size) + ", not supported.");
        }
        else if (inString.length() < size) {
            short missingZeroes = (short) (size - inString.length());
            for (short i = 0; i < missingZeroes; i++) {
                outString.append("0");
            }
        }
        outString.append(inString);
        return outString.toString();
    }
}
