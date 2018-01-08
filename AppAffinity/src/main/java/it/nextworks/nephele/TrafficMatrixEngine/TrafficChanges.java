package it.nextworks.nephele.TrafficMatrixEngine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Marco Capitani on 18/12/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
public class TrafficChanges {

    @JsonProperty("traffic_changes")
    public List<int[]> changes;

    public String method = "INCREMENTAL";

    public TrafficChanges(List<int[]> changes) {
        this.changes = changes;
    }
}
