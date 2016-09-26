package it.nextworks.nephele.OFTranslator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@RestController
public class TranslatorController {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@RequestMapping(value="/translate", method=RequestMethod.POST)
	@ApiOperation(value = "postNetAllocMatrix",
		nickname = "Post network alocation matrix to be translated")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Inventory.class)})
	public Inventory makeinventory(
			@RequestBody int[][] networkAllocationSolution )
	{
		Const.init(networkAllocationSolution);
		NetworkBuilder net = new NetworkBuilder();
		return net.build();
	}

	@ExceptionHandler(NullPointerException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleAppException(NullPointerException ex) {
		log.info(ex.getMessage(), ex);
		return ex.getMessage();
	}
	
}
