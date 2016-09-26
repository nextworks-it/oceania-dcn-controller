package it.nextworks.nephele.OFTranslator;

import io.swagger.annotations.ApiModelProperty;

public class NetworkAllocationSolution {
	//bean wrapping the solution matrix
	
	@ApiModelProperty(notes="The network allocation solution")
	private Integer[][] matrix;
	
	public Integer[][] getMatrix(){
		return matrix;
	}

	public void setMatrix(Integer[][] inMatrix){
		matrix = inMatrix;
	}

}
