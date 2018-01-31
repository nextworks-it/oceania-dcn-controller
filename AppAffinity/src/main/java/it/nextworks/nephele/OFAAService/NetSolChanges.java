package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class NetSolChanges extends NetSolBase {

    public String method;

    @JsonProperty("Network_Allocation_ID")
    public String netAllocId;

    @JsonProperty("Status")
    public CompStatus status;

    @JsonProperty("Network_Allocation_Changes")
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
