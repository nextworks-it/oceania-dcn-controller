package it.nextworks.nephele.OFAAService.Inventory;

import it.nextworks.nephele.OFTranslator.Bitmap;

public class OptOFOutput extends OFOutput {
	
	private Integer lambda;
	private Bitmap tBitmap;
	
	public Integer getLambda() {
		return lambda;
	}

	public String gettBitmap() {
		return tBitmap.getBitmap();
	}

	public void setLambda(Integer lambda) {
		this.lambda = lambda;
	}

	public void settBitmap(Bitmap tBitmap) {
		this.tBitmap = tBitmap;
	}
	
	public OptOFOutput(){
		
	}

	
}
