package it.nextworks.nephele.OFTranslator;

public class OptOFOutput extends OFOutput {
	
	private Integer lambda;
	private Bitmap tBitmap;
	
	public OptOFOutput(Integer inLambda, Bitmap inBitmap, String inPort) throws IllegalArgumentException{
		super(inPort);
		//System.out.println(inLambda);
		if ((inLambda<0) || (inLambda > Const.W -1)) {
			throw new IllegalArgumentException("Illegal lambda."); 
		}
		
		tBitmap=inBitmap;		
		lambda=inLambda;
		
	}
	
	public OptOFOutput(Integer inLambda, String inBitmap, String inPort) throws IllegalArgumentException{
		super(inPort);
		//System.out.println(inLambda);
		if ((inLambda<0) || (inLambda > Const.W -1)) {
			throw new IllegalArgumentException("Illegal lambda."); 
		}
		
		tBitmap=new Bitmap(inBitmap);		
		lambda=inLambda;
		
	}

	public Integer getLambda() {
		return lambda;
	}

	public String gettBitmap() {
		return tBitmap.getBitmap();
	}
	
}
