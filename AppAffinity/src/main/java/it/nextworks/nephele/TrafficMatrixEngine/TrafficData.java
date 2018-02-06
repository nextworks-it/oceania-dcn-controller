package it.nextworks.nephele.TrafficMatrixEngine;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TrafficData {

    private int[][] matrix = new int[Const.P * Const.W * Const.Z][Const.P * Const.W];

    private Map<int[], Integer> changes = new HashMap<>(); // TODO cannot use arrays!111!!!!

    private HashMap<String, AppProfile> profiles = new HashMap<>();

    public synchronized String addProfile(AppProfile appProfile) {
        for (Tunnel conn : appProfile.tunnelList) {
            if (Arrays.stream(matrix[conn.source]).sum() >= 80) {
                throw new IllegalArgumentException(String.format(
                        "Cannot allocate flow: row %s already has 80 slots assigned.",
                        conn.source
                ));
            }
            int newValue = matrix[conn.source][(conn.dest / Const.Z)] + conn.bandwidth;
            matrix[conn.source][(conn.dest / Const.Z)] = newValue;
            addChange(conn.source, conn.dest / Const.Z, newValue);
        }
        String id = UUID.randomUUID().toString();
        appProfile.id = id;
        profiles.put(id, appProfile);
        return id;
    }

    public synchronized boolean deleteProfile(String inputId) {
        if (profiles.containsKey(inputId)) {
            for (Tunnel conn : profiles.get(inputId).tunnelList) {
                int newValue = matrix[conn.source][(conn.dest / Const.Z)] - conn.bandwidth;
                matrix[conn.source][(conn.dest / Const.Z)] = newValue;
                addChange(conn.source, conn.dest / Const.Z, newValue);
            }
            profiles.remove(inputId);
            return true;
        } else {
            return false;
        }
    }

    private void addChange(int row, int column, int newValue) {
        int[] coords = new int[] {row, column};
        changes.put(coords, newValue);
    }

    public synchronized List<int[]> getChanges() {
        return changes.entrySet().stream().map(
                e -> new int[] {e.getKey()[0], e.getKey()[1], e.getValue()}
        ).collect(Collectors.toList());
    }

    public synchronized List<int[]> getAndResetChanges() {
        List<int[]> output = getChanges();
        changes.clear();
        return output;
    }

    public int[][] getMatrix() {
        return matrix;
    }
}
