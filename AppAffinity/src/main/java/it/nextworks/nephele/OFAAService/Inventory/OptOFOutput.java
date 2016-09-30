package it.nextworks.nephele.OFAAService.Inventory;

import it.nextworks.nephele.OFTranslator.Bitmap;

public class OptOFOutput extends OFOutput {
	
	private Integer lambda;
	private Bitmap timeBitmap;
	
	public Integer getLambda() {
		return lambda;
	}

	public String getTimeBitmap() {
		return timeBitmap.getBitmap();
	}

	public void setLambda(Integer lambda) {
		this.lambda = lambda;
	}

	public void setTimeBitmap(Bitmap timeBitmap) {
		this.timeBitmap = timeBitmap;
	}
	
	public OptOFOutput(){
		
	}

	
}
