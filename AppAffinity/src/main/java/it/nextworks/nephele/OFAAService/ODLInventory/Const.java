package it.nextworks.nephele.OFAAService.ODLInventory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Const {

    public static Integer P;
    public static Integer I;
    public static Integer W;
    public static Integer T;
    public static Integer R;
    public static Integer Z; //servers under each ToR
    public static Integer firstPod;
    public static boolean EMULATED;

    @Value("${P}")
    private int auxP;
    @Value("${I}")
    private int auxI;
    @Value("${W}")
    private int auxW;
    @Value("${T}")
    private int auxT;
    @Value("${R}")
    private int auxR;
    @Value("${Z}")
    private int auxz;
    @Value("${firstPod}")
    private int auxFirstPod;
    @Value("${emulated}")
    private boolean emulated;

    @PostConstruct
    public void initialize() {
        P = auxP;
        I = auxI;
        W = auxW;
        T = auxT;
        R = auxR;
        Z = auxz;
        firstPod = auxFirstPod;
        EMULATED = emulated;
        matrix = new int[I * T][P * W * Z];
    }

    public static int[][] matrix;

    public static void init(int[][] inMat) {
        matrix = new int[I * T][P * W * Z];
        for (int i = 0; i < (I * T); i++) {
            for (int j = 0; j < (P * W * Z); j++) {
                matrix[i][j] = inMat[i][j];
            }
        }
    }

    public static void update(int[][] changes) {
        for (int[] change : changes) {
            if (change[0] < 0 || change[0] > I * T) {
                String msg = String.format("Row index must be between 0 and %s. Got %s.", I * T, change[0]);
                throw new IllegalArgumentException(msg);
            }
            if (change[1] < 0 || change[1] > P * W * Z) {
                String msg = String.format("Col index must be between 0 and %s. Got %s.", P*W*T, change[1]);
                throw new IllegalArgumentException(msg);
            }
            matrix[change[0]][change[1]] = change[2];
        }
    }
}
