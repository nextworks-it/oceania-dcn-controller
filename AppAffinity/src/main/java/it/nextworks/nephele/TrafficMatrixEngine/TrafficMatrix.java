package it.nextworks.nephele.TrafficMatrixEngine;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Marco Capitani on 18/12/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
public class TrafficMatrix {

    @JsonProperty("traffic_matrix")
    public int[][] matrix;

    public String method = "FULL";

    public TrafficMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public TrafficMatrix() {

    }
}
