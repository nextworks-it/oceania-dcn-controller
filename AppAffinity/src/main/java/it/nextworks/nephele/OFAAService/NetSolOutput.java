package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NetSolOutput {

    @JsonProperty("Network_Allocation_ID")
    public String netAllocId;

    @JsonProperty("Status")
    public CompStatus status;

    @JsonProperty("Network_Allocation_Solution")
    public int[][] matrix;

    public NetSolOutput() {

    }


}
