package it.nextworks.nephele.appaffdb;

import it.nextworks.nephele.OFAAService.ConnectionEndPoint;
import it.nextworks.nephele.OFAAService.ConnectionSource;
import it.nextworks.nephele.OFAAService.ConnectionType;
import it.nextworks.nephele.OFAAService.NephConnection;
import it.nextworks.nephele.OFAAService.Recovery;
import it.nextworks.nephele.OFAAService.Service;
import it.nextworks.nephele.OFAAService.ServiceStatus;
import it.nextworks.nephele.OFAAService.TrafficProfile;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marco Capitani on 13/09/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */

@SpringBootTest
public class DbTest {

    ReflectionTestUtils testUtils = new ReflectionTestUtils();

    private DbManager manager = new DbManager(":memory:", true);

    private Service service = new Service();

    {
        service.status = ServiceStatus.ACTIVE;
        service.connections = new ArrayList<>();

        NephConnection conn1 = new NephConnection();
        conn1.profile = new TrafficProfile();
        conn1.profile.bandwidth = 15;
        conn1.setRecovery(Recovery.UNPROTECTED);
        conn1.setConnType(ConnectionType.POINT_TO_POINT);
        conn1.destIp = "10.0.0.1";
        conn1.source = new ConnectionSource();
        conn1.source.pod = 1;
        conn1.source.tor = 2;
        conn1.source.server = 3;
        conn1.dest = new ConnectionEndPoint();
        conn1.dest.pod = 54;
        conn1.dest.tor = 22;
        conn1.dest.server = 33;


        NephConnection conn2 = new NephConnection();
        conn2.profile = new TrafficProfile();
        conn2.profile.bandwidth = 22;
        conn2.setRecovery(Recovery.UNPROTECTED);
        conn2.setConnType(ConnectionType.POINT_TO_POINT);
        conn2.destIp = "111.111.111.111";
        conn2.source = new ConnectionSource();
        conn2.source.pod = 7;
        conn2.source.tor = 12;
        conn2.source.server = 35;
        conn2.dest = new ConnectionEndPoint();
        conn2.dest.pod = 901;
        conn2.dest.tor = 2313;
        conn2.dest.server = 311;

        service.connections.add(conn1);
        service.connections.add(conn2);
    }

    @Before
    public void setup() {
        manager.buildConnection();
    }

    @Test
    public void testSave() {
        String serviceId = "pRoVa";
        manager.saveService(serviceId, service);
    }

    @Test
    public void testQuery() {
        testSave();
        Service gotBack = manager.queryServiceWithId("pRoVa");
        Assert.assertEquals(service, gotBack);
    }

    @Test
    public void testIp() {
        String ip1 = "120.213.222.111";
        String ip2 = "1.0.0.1";
        String ip3 = "127.0.0.1";
        Assert.assertEquals(ip1, manager.decodeIp(manager.encodeIP(ip1)));
        Assert.assertEquals(ip2, manager.decodeIp(manager.encodeIP(ip2)));
        Assert.assertEquals(ip3, manager.decodeIp(manager.encodeIP(ip3)));
    }

    @After
    public void tearDown() throws Exception {
        manager.close();
    }
}
