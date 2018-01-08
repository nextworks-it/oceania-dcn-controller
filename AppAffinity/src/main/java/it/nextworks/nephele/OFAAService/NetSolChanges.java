package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class NetSolChanges extends NetSolBase {

    public String method;

    @JsonProperty("network_allocation_id")
    public String netAllocId;

    @JsonProperty("status")
    public CompStatus status;

    @JsonProperty("network_allocation_changes")
    public int[][] changes;

    public NetSolChanges() {

    }

    @Override
    public String toString() {
        return "NetSolChanges{" +
                "netAllocId='" + netAllocId + '\'' +
                ", status=" + status.toString() +
                ", changes=" + Arrays.deepToString(changes) +
                '}';
    }

    @Override
    public int[][] getResult() {
        return changes;
    }
}
