package it.nextworks.nephele.OFAAService.ODLInventory;

public class OpendaylightMetadataContainer {
	
	private Integer metadata;
	
	public OpendaylightMetadataContainer(Integer lambda, String tBitmap){
		metadata = lambda;
		for (Integer i = 0; i< Const.T; i++){
			metadata = metadata + (2^(63-i)*tBitmap.charAt(i));
		}
	}

	public Integer getMetadata() {
		return metadata;
	}

}
