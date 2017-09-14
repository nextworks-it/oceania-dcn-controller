package it.nextworks.nephele.OFAAService;

import java.util.ArrayList;
import java.util.List;

import it.nextworks.nephele.TrafficMatrixEngine.AppProfile;

import io.swagger.annotations.ApiModelProperty;


/**
 * Contains a list of connections, as in host-to-host communication
 * channels.
 *
 * @author MCapitani
 */
public class Service {

    @ApiModelProperty(notes = "The tunnels to be opened")
    public List<NephConnection> connections = new ArrayList<>();

    @ApiModelProperty(notes = "The status of the service")
    public ServiceStatus status;

    public AppProfile makeAppProfile() {
        AppProfile appProfile = new AppProfile();
        appProfile.tunnelList = new ArrayList<>();
        for (NephConnection connection : connections) {
            appProfile.tunnelList.add(connection.makeTunnel());
        }
        return appProfile;
    }


    public Service() {

    }

    public Service(Service inService, ServiceStatus inStatus) {
        connections = inService.connections;
        status = inStatus;
    }

    public boolean validateAndInit() {
        if ((connections != null) && (connections.size() > 0)) {
            for (NephConnection conn : connections) {
                if (!conn.validateAndInit()) return false;
            }
            return true;
        } else throw new IllegalArgumentException("At least one connection must be submitted.");
    }

    @Override
    public String toString() {
        return String.format("Status: %s\nConnections: %s", status, connections);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;

        if (!connections.equals(service.connections)) return false;
        return status == service.status;
    }

    @Override
    public int hashCode() {
        int result = connections.hashCode();
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
