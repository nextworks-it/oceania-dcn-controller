package it.nextworks.nephele.OFTranslator;

public class OptOFMatch extends OFMatch {
	
	private Integer lambda;
	private Bitmap tBitmap;
	
	public OptOFMatch(Integer inLambda, Bitmap inBitmap, String inPort){
		super(inPort);
		if ((inLambda<0) || (inLambda > (Const.W -1))) {
			throw new IllegalArgumentException("Illegal lambda."); 
		}
		lambda = inLambda;
		tBitmap = inBitmap;
	}
	
	public OptOFMatch(Integer inLambda, String inBitmap, String inPort){
		super(inPort);
		if ((inLambda<0) || (inLambda > (Const.W -1))) {
			throw new IllegalArgumentException("Illegal lambda."); 
		}
		lambda = inLambda;
		tBitmap = new Bitmap(inBitmap);
	}


	public Integer getLambda() {
		return lambda;
	}

	public String gettBitmap() {
		return tBitmap.getBitmap();
	}
	
}
