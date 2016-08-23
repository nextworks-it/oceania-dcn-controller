package it.nextworks.nephele.OFTranslator;

public class Const {
	
	public static Integer P=3;
	public static Integer I=2;
	public static Integer W=2;
	public static Integer T=3;
	public static Integer z=3; //servers under each ToR
	
	public static Integer[][] matrix = new Integer[Const.I * Const.T][Const.P * Const.W];

	public static void init(int[][] inMat) {
		for (int i = 0; i< Const.I * Const.T; i++){
			for (int j = 0; j< Const.P * Const.W; j++){
				matrix[i][j] = inMat[i][j];
			}
		}
	}
}
