package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class NetSolOutput extends NetSolBase {

    @JsonProperty("Network_Allocation_Solution") // TODO change
    public int[][] matrix;

    public NetSolOutput() {

    }

    @Override
    public String toString() {
        return "NetSolOutput{" +
                "netAllocId='" + netAllocId + '\'' +
                ", status=" + status.toString() +
                ", matrix=" + Arrays.deepToString(matrix) +
                '}';
    }

    @Override
    public int[][] getResult() {
        return matrix;
    }
}
