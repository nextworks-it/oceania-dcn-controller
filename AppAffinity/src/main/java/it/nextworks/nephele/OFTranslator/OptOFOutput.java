package it.nextworks.nephele.OFTranslator;

public class OptOFOutput extends OFOutput {
	
	private Integer lambda;
	private Bitmap timeBitmap;
	
	public OptOFOutput(Integer inLambda, Bitmap inBitmap, String inPort) throws IllegalArgumentException{
		super(inPort);
		//System.out.println(inLambda);
		if ((inLambda<0) || (inLambda > Const.W -1)) {
			throw new IllegalArgumentException("Illegal lambda."); 
		}
		
		timeBitmap =inBitmap;
		lambda=inLambda;
		
	}
	
	public OptOFOutput(Integer inLambda, String inBitmap, String inPort) throws IllegalArgumentException{
		super(inPort);
		//System.out.println(inLambda);
		if ((inLambda<0) || (inLambda > Const.W -1)) {
			throw new IllegalArgumentException("Illegal lambda."); 
		}
		
		timeBitmap =new Bitmap(inBitmap);
		lambda=inLambda;
		
	}

    public OptOFOutput(){

    }

	public void setLambda(Integer lambda) {
		this.lambda = lambda;
	}

	public void setTimeBitmap(Bitmap timeBitmap) {
		this.timeBitmap = timeBitmap;
	}

	public Integer getLambda() {
		return lambda;
	}

	public String getTimeBitmap() {
		return timeBitmap.getBitmap();
	}
	
}
