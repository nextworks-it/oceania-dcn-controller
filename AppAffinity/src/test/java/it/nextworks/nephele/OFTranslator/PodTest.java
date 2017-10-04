package it.nextworks.nephele.OFTranslator;

import it.nextworks.nephele.OFAAService.ODLInventory.Const;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Marco Capitani on 04/10/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
public class PodTest {
    @Test
    public void buildFlowChart() throws Exception {
        Const.P = 3;
        Const.W = 2;
        Const.Z = 4;
        Const.I = 2;
        Const.T = 6;
        Const.R = 2;
        Const.firstPod = 1;

        int[][] matrix = new int[Const.I * Const.T][Const.P * Const.W * Const.Z];
        matrix[0][0] = 1; // intra pod traffic
        matrix[1][7] = 3; // inter pod traffic
        matrix[5][16] = 2; // inter pod traffic
        Const.init(matrix);

        Map<Integer, String[]> ringPorts = new HashMap<>();
        ringPorts.put(0, new String[] {"1", "2"});
        ringPorts.put(1, new String[] {"3", "4"});
        Map<Integer, String> torPorts = new HashMap<>();
        torPorts.put(0, "5");
        torPorts.put(1, "6");

        Pod pod = new Pod(0, 1, ringPorts, torPorts);

        String str = pod.optFlowTable.toString();
        str = str.replace("{match", "{\n\tmatch");
        str = str.replace("}, out", "},\n\tout");
        str = str.replace("}, FlowEntry", "},\nFlowEntry");
        str = str.replace("}}", "}\n}");
        System.out.println(str);
        String str2 = pod.flowTable.toString();
        str2 = str2.replace("{match", "{\n\tmatch");
        str2 = str2.replace("}, out", "},\n\tout");
        System.out.println(str2);
    }
}
