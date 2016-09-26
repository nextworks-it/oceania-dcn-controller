package it.nextworks.nephele.OFTranslator;

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

    @PostConstruct
	public void initialize(){
        P = auxP;
        I = auxI;
        W = auxW;
        T = auxT;
        R = auxR;
        Z = auxz;
    }
	
	public static Integer[][] matrix;

	static void init(int[][] inMat) {
        matrix = new Integer[I * T][P * W * Z];
		for (int i = 0; i< I * T; i++){
			for (int j = 0; j< P * W * Z; j++){
				matrix[i][j] = inMat[i][j];
			}
		}
	}
}
