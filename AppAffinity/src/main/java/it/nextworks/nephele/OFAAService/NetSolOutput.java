package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class NetSolOutput {

    @JsonProperty("Network_Allocation_ID")
    public String netAllocId;

    @JsonProperty("Status")
    public CompStatus status;

    @JsonProperty("Network_Allocation_Solution")
    public int[][] matrix;

    public NetSolOutput() {

    }

    @Override
    public String toString() {
        return "NetSolOutput{" +
                "netAllocId='" + netAllocId + '\'' +
                ", status=" + status.toString() +
                ", matrix=" + Arrays.toString(matrix) +
                '}';
    }
}
