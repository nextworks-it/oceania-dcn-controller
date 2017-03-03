/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.nephele.mock.flowutils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.mock.rev161228.vlan.optical.correspondance.OpticalResourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.rev161228.OpticalResourceAttributes;

import java.math.BigInteger;

public class ComparableResource {

    private final String timeslots;

    private final BigInteger wavelength;

    ComparableResource(OpticalResourceAttributes optResource) {
        timeslots = optResource.getTimeslots();
        wavelength = optResource.getWavelength();
    }

    OpticalResourceAttributes toOpticalResource() {
        return new OpticalResourceBuilder()
                .setTimeslots(timeslots)
                .setWavelength(wavelength)
                .build();
    }

    public boolean includes(ComparableResource other) {
        if (!this.wavelength.equals(other.wavelength)) {
            return false;
        }
        if (!(this.timeslots.length() == other.timeslots.length())) {
            return false;
        }
        for (int i = 0; i< this.timeslots.length(); i++) {
            if (this.timeslots.charAt(i) == '0' && other.timeslots.charAt(i) == '1')
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("optical resource [wavelength: %s, timeslots: %s]", wavelength, timeslots);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComparableResource that = (ComparableResource) o;

        return timeslots.equals(that.timeslots) && wavelength.equals(that.wavelength);
    }

    @Override
    public int hashCode() {
        int result = timeslots.hashCode();
        result = 31 * result + wavelength.hashCode();
        return result;
    }
}
