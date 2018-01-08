package it.nextworks.nephele.appaffdb;

import it.nextworks.nephele.OFAAService.ConnectionEndPoint;
import it.nextworks.nephele.OFAAService.ConnectionSource;
import it.nextworks.nephele.OFAAService.ConnectionType;
import it.nextworks.nephele.OFAAService.NephConnection;
import it.nextworks.nephele.OFAAService.Recovery;
import it.nextworks.nephele.OFAAService.Service;
import it.nextworks.nephele.OFAAService.ServiceStatus;
import it.nextworks.nephele.OFAAService.TrafficProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by Marco Capitani on 12/09/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */

@Repository
public class DbManager implements AutoCloseable {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${databaseFile}")
    private String dbFile;

    @Value("${wipeTablesOnStartup}")
    private boolean refresh;

    private Connection connection;

    public DbManager() {
    }

    DbManager(String dbFile, boolean refresh) {
        this.dbFile = dbFile;
        this.refresh = refresh;
    }

    @PostConstruct
    public void buildConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);

            Statement setup = connection.createStatement();
            setup.setQueryTimeout(30); // 30 sec for setup operations.

            if (refresh) {
                setup.executeUpdate("drop table if exists service");
                setup.executeUpdate("drop table if exists connection");
            }
            setup.executeUpdate("create table if not exists service (id string primary key, status string)");
            setup.executeUpdate("create table if not exists connection (" +
                "id integer, " +
                "service_id string, " +
                "type integer, " +
                "src_pod integer, " +
                "src_tor integer, " +
                "src_zone integer, " +
                "dst_pod integer, " +
                "dst_tor integer, " +
                "dst_zone integer, " +
                "bandwidth integer, " +
                "recovery integer, " +
                "dest_ip integer, " +
                "primary key (id, service_id), " +
                "foreign key (service_id) references service(id)" +
                ")");
            /*
            create table if not exists connection (
                id integer,
                service_id string,
                type integer,
                src_pod integer,
                src_tor integer,
                src_zone integer,
                dst_pod integer,
                dst_tor integer,
                dst_zone integer,
                bandwidth integer,
                recovery integer,
                dest_ip integer,
                primary key (id, service_id),
                foreign key (service_id) references service(id)
                )
             */
        } catch (SQLException exc) {
            log.error("Failed to setup database. Cause: {}.", exc.getMessage());
            throw new RuntimeException(exc);
        }
    }

    @Override
    public void close() throws Exception {
        if (null != connection) {
            connection.close();
        }
    }

    public void saveService(String serviceId, Service service) {
        try (Statement save = getStatement();
             PreparedStatement connSave = getPrepared(
                 "insert into connection values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
             )
        ) {
            log.debug("Storing in database service:\nServiceId: {}\n{}", serviceId, service);
            String query = String.format(
                "insert into service values('%s', %s)",
                serviceId,
                service.status.value
            );
            log.trace("Executing update query: '{}'.", query);
            save.executeUpdate(query);
            int index = 0;
            for (NephConnection s_connection : service.connections) {
                connSave.setInt(1, index);
                connSave.setString(2, serviceId);
                connSave.setInt(3, s_connection.getConnType().value);
                connSave.setInt(4, s_connection.source.pod);
                connSave.setInt(5, s_connection.source.tor);
                connSave.setInt(6, s_connection.source.server);
                connSave.setInt(7, s_connection.dest.pod);
                connSave.setInt(8, s_connection.dest.tor);
                connSave.setInt(9, s_connection.dest.server);
                connSave.setInt(10, s_connection.profile.bandwidth);
                connSave.setInt(11, s_connection.getRecovery().value);
                connSave.setInt(12, encodeIP(s_connection.destIp));
                index++;
                connSave.addBatch();
                log.trace("Adding to batch query: 'insert into connection values( " +
                        "{}, '{}', {}, {}, {}, {}, {}, {}, {}, {}, {}, {} )",
                    index, serviceId, s_connection.getConnType().value,
                    s_connection.source.pod, s_connection.source.tor, s_connection.source.server,
                    s_connection.dest.pod, s_connection.dest.tor, s_connection.dest.server,
                    s_connection.profile.bandwidth, s_connection.getRecovery().value,
                    encodeIP(s_connection.destIp));
            }
            int[] results = connSave.executeBatch();
            boolean successful = Arrays.stream(results).noneMatch((i) -> i == Statement.EXECUTE_FAILED);
            if (!successful) {
                throw new SQLException("Save failed for unknown reasons (probably key conflict)");
            }
        } catch (SQLException exc) {
            log.error("Save failed. Cause: {}.", exc.getMessage());
        }
    }

    private Statement getStatement() {
        try {
            return connection.createStatement();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement getPrepared(String s) {
        try {
            return connection.prepareStatement(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    int encodeIP(String ip) {
        try {
            String[] splitted = ip.split("\\.");
            if (splitted.length != 4) {
                throw new IllegalArgumentException("wrong split");
            }

            int result = 0;
            for (int i = 0; i < 4; i++) {
                int number = Integer.parseInt(splitted[i]);
                if (255 < number || 0 > number) {
                    throw new IllegalArgumentException("octet out of bounds: " + splitted[i]);
                }
                result = result + (number << 8 * (3 - i));
            }
            return result;
        } catch (IllegalArgumentException exc) {
            throw new IllegalArgumentException("'" + ip + "' is not a valid IP address: " + exc.getMessage() + ".");
        }
    }

    String decodeIp(int ip) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(ip).array();
        String[] results = new String[4];
        for (int i = 0; i < 4; i++) {
            byte b = bytes[i];
            Integer octet = (int) b;
            if (octet < 0) {
                octet = 256 + octet;
            }
            results[i] = octet.toString();
        }
        return String.join(".", results);
    }

    public Set<String> queryServicesId() {
        try (Statement query = connection.createStatement()) {
            String s = "select s.id from service as s";
            log.trace("Executing query: '{}'.", s);
            ResultSet results = query.executeQuery(s);
            Set<String> output = new HashSet<>();
            while (results.next()) {
                output.add(results.getString("id"));
            }
            return output;
        } catch (SQLException exc) {
            log.error("Query failed. Cause: {}.", exc.getMessage());
            return null;
        }
    }

    public int resetToRequested(ServiceStatus status) {
        return resetToStatus(status, ServiceStatus.REQUESTED);
    }

    public int resetToStatus(ServiceStatus fromStatus, ServiceStatus toStatus) {
        try (Statement query = connection.createStatement()) {
            String s = String.format("update service " +
                    "set status = %s " +
                    "where (status == %s)", fromStatus.value, toStatus.value);
            log.trace("Executing query: '{}'.", s);
            return query.executeUpdate(s);
        } catch (SQLException exc) {
            log.error("Query failed. Cause: {}.", exc.getMessage());
            return -1;
        }
    }

    public Service queryServiceWithId(String id) {
        try (Statement query = connection.createStatement()) {
            String s = String.format("select s.status, " +
                "c.type, " +
                "c.src_pod, c.src_tor, c.src_zone, " +
                "c.dst_pod, c.dst_tor, c.dst_zone, " +
                "c.bandwidth, c.recovery, " +
                "c.dest_ip " +
                "from service as s join connection as c on s.id == c.service_id " +
                "where s.id == '%s' " +
                "order by c.id ASC", id);
            log.debug("Executing query: '{}'.", s);
            ResultSet results = query.executeQuery(s);
            Service output = null;
            while (results.next()) {
                if (null == output) {
                    output = new Service();
                    output.registerId(id);
                    output.status = ServiceStatus.getFromValue(results.getInt("status"));
                    output.connections = new ArrayList<>();
                }
                NephConnection connection = new NephConnection();
                connection.profile = new TrafficProfile();
                connection.profile.bandwidth = results.getInt("bandwidth");
                connection.setConnType(ConnectionType.getFromValue(results.getInt("type")));
                connection.setRecovery(Recovery.getFromValue(results.getInt("recovery")));

                ConnectionSource src = new ConnectionSource();
                src.pod = results.getInt("src_pod");
                src.tor = results.getInt("src_tor");
                src.server = results.getInt("src_zone");

                ConnectionEndPoint dst = new ConnectionEndPoint();
                dst.pod = results.getInt("dst_pod");
                dst.tor = results.getInt("dst_tor");
                dst.server = results.getInt("dst_zone");

                connection.source = src;
                connection.dest = dst;

                connection.destIp = decodeIp(results.getInt("dest_ip"));
                output.connections.add(connection);
            }
            if (null == output) {
                throw new SQLException("no service with ID " + id + " found");
            }
            return output;
        } catch (SQLException exc) {
            log.error("Query failed. Cause: {}.", exc.getMessage());
            return null;
        }
    }

    public List<ExtConnection> queryExtConnOutOfTor(int srcPod, int srcTor) {
        try (Statement query = connection.createStatement()) {
            String s = String.format(
                    "select c.src_zone, c.dst_pod, c.dst_tor, c.bandwidth, c.dest_ip " +
                            "from connection as c join service as s on s.id == c.service_id " +
                            "where c.src_pod == %s and c.src_tor == %s and (s.status == %s or s.status == %s)",
                srcPod, srcTor, ServiceStatus.ESTABLISHING.value, ServiceStatus.ACTIVE.value);
            log.trace("Executing query: '{}'.", s);
            ResultSet results = query.executeQuery(s);
            List<ExtConnection> output = new ArrayList<>();
            while (results.next()) {
                output.add(new ExtConnection(
                        0,
                        0,
                        results.getInt("src_zone"),
                        results.getInt("dst_pod"),
                        results.getInt("dst_tor"),
                        results.getInt("bandwidth"),
                        results.getString("dest_ip")
                ));
            }
            return output;
        } catch (SQLException exc) {
            log.error("Query failed. Cause: {}.", exc.getMessage());
            return null;
        }
    }

    public List<ExtConnection> queryExtConn() {
        try (Statement query = connection.createStatement()) {
            String s = String.format(
                    "select c.src_pod, c.src_tor, c.src_zone, c.dst_pod, c.dst_tor, c.bandwidth, c.dest_ip " +
                            "from connection as c join service as s on s.id == c.service_id " +
                            "where (s.status == %s or s.status == %s)",
                    ServiceStatus.ESTABLISHING.value, ServiceStatus.ACTIVE.value);
            log.trace("Executing query: '{}'.", s);
            ResultSet results = query.executeQuery(s);
            log.trace("Query '{}' done.", s);
            List<ExtConnection> output = new ArrayList<>();
            while (results.next()) {
                output.add(new ExtConnection(
                        results.getInt("src_pod"),
                        results.getInt("src_tor"),
                        results.getInt("src_zone"),
                        results.getInt("dst_pod"),
                        results.getInt("dst_tor"),
                        results.getInt("bandwidth"),
                        decodeIp(results.getInt("dest_ip"))
                ));
            }
            return output;
        } catch (SQLException exc) {
            log.error("Query failed. Cause: {}.", exc.getMessage());
            return null;
        }
    }

    public List<String> queryWithStatus(ServiceStatus status) {
        try (Statement query = connection.createStatement()) {
            String s = String.format(
                    "select s.id " +
                            "from service as s " +
                            "where (s.status == %s)",
                    status.value);
            log.trace("Executing query: '{}'.", s);
            ResultSet results = query.executeQuery(s);
            log.trace("Query '{}' done.", s);
            List<String> output = new ArrayList<>();
            while (results.next()) {
                output.add(results.getString("id"));
            }
            return output;
        } catch (SQLException exc) {
            log.error("Query failed. Cause: {}.", exc.getMessage());
            return null;
        }
    }

    public void updateStatus(Service service, ServiceStatus status) {
        updateStatus(service.getId(), status);
    }

    public void updateStatus(String serviceId, ServiceStatus status) {
        try (Statement query = connection.createStatement()) {
            String s = String.format(
                    "update service set status = %s where id == '%s'",
                    status.value, serviceId);
            log.trace("Executing query: '{}'.", s);
            query.executeUpdate(s);
        } catch (SQLException exc) {
            log.error("Query failed. Cause: {}.", exc.getMessage());
        }
    }
}
