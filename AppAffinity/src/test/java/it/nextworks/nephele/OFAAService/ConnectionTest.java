package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by Marco Capitani on 23/08/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
public class ConnectionTest {

    @Test
    public void testConstruction() {
        NephConnection connection = new NephConnection();

        ConnectionSource source = new ConnectionSource();
        source.pod = 10;
        source.tor = 5;
        source.server = 2;
        connection.source = source;

        ConnectionEndPoint dest = new ConnectionEndPoint();
        dest.pod = 17;
        dest.tor = 10;
        dest.server = 1;
        connection.dest = dest;

        TrafficProfile profile = new TrafficProfile();
        profile.bandwidth = 2;
        connection.profile = profile;

        connection.setConnType(ConnectionType.POINT_TO_POINT);
        connection.setRecovery(Recovery.UNPROTECTED);

        ObjectMapper mapper = new ObjectMapper();

        try {
            String s = mapper.writeValueAsString(connection);
            System.out.println(s);

            NephConnection rebuilt = mapper.readValue(s, NephConnection.class);

            System.out.println(mapper.writeValueAsString(rebuilt));
            assertTrue(connection.profile.equals(rebuilt.profile));
            assertTrue(connection.getConnType().equals(rebuilt.getConnType()));
            assertTrue(connection.getRecovery().equals(rebuilt.getRecovery()));
            assertTrue(connection.source.equals(rebuilt.source));
            assertTrue(connection.dest.equals(rebuilt.dest));
            assertTrue(connection.destIp == null ? rebuilt.destIp == null : connection.destIp.equals(rebuilt.destIp));
        } catch (IOException exc) {
            System.out.println(exc.toString());
        }
    }

    @Test
    public void testConstructionWithDestIp() {
        NephConnection connection = new NephConnection();

        ConnectionSource source = new ConnectionSource();
        source.pod = 10;
        source.tor = 5;
        source.server = 2;
        connection.source = source;

        ConnectionEndPoint dest = new ConnectionEndPoint();
        dest.pod = 17;
        dest.tor = 10;
        dest.server = 1;
        connection.dest = dest;

        TrafficProfile profile = new TrafficProfile();
        profile.bandwidth = 2;
        connection.profile = profile;

        connection.setConnType(ConnectionType.POINT_TO_POINT);
        connection.setRecovery(Recovery.UNPROTECTED);

        connection.destIp = "127.0.0.1";

        ObjectMapper mapper = new ObjectMapper();

        try {
            String s = mapper.writeValueAsString(connection);
            System.out.println(s);

            NephConnection rebuilt = mapper.readValue(s, NephConnection.class);

            System.out.println(mapper.writeValueAsString(rebuilt));
            assertTrue(connection.profile.equals(rebuilt.profile));
            assertTrue(connection.getConnType().equals(rebuilt.getConnType()));
            assertTrue(connection.getRecovery().equals(rebuilt.getRecovery()));
            assertTrue(connection.source.equals(rebuilt.source));
            assertTrue(connection.dest.equals(rebuilt.dest));
            assertTrue(connection.destIp == null ? rebuilt.destIp == null : connection.destIp.equals(rebuilt.destIp));
        } catch (IOException exc) {
            System.out.println(exc.toString());
        }
    }

}