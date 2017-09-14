package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class ConnectionResponse {

    @JsonProperty("Connection_ID")
    @ApiModelProperty(notes = "The ID of the service")
    public String serviceID;

    @ApiModelProperty(notes = "The status of the service")
    public String status;

    public ConnectionResponse(String inId, String inStatus) {
        serviceID = inId;
        status = inStatus;
    }

}
