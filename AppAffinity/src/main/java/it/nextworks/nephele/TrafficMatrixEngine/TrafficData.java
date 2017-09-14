package it.nextworks.nephele.TrafficMatrixEngine;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.UUID;

@Component
public class TrafficData {

    private int[][] matrix = new int[Const.P * Const.W * Const.Z][Const.P * Const.W];

    private HashMap<String, AppProfile> profiles = new HashMap<>();

    public synchronized String addProfile(AppProfile appProfile) {
        for (Tunnel conn : appProfile.tunnelList) {
            matrix[conn.source][(conn.dest / Const.Z)] =
                matrix[conn.source][(conn.dest / Const.Z)] + conn.bandwidth;
        }
        String id = UUID.randomUUID().toString();
        appProfile.id = id;
        profiles.put(id, appProfile);
        return id;
    }

    public synchronized boolean deleteProfile(String inputId) {
        if (profiles.containsKey(inputId)) {
            for (Tunnel conn : profiles.get(inputId).tunnelList) {
                matrix[conn.source][(conn.dest / Const.Z)] =
                    matrix[conn.source][(conn.dest / Const.Z)] - conn.bandwidth;
            }
            profiles.remove(inputId);
            return true;
        } else return false;
    }

    public int[][] getMatrix() {
        return matrix;
    }
}
