package it.nextworks.nephele.TrafficMatrixEngine;


import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping(value="/trafficmatrix")
public class TrafficMatrixController {


	@RequestMapping(value="/applicationprofile", method=RequestMethod.POST)
	@ApiOperation(value = "postAppProfile", nickname = "Post an app profile to be added")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")}) 
	public String postAppProfile(@RequestBody AppProfile appProfile){
		return TrafficData.addProfile(appProfile);
	}

	@RequestMapping(value="/applicationprofile/{appProfileId}", method=RequestMethod.DELETE)
	@ApiOperation(value = "DeleteAppProfile", nickname = "Delete an add profile")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")}) 
	public void deleteAppProfile(@PathVariable String appProfileId){
		if (!(TrafficData.deleteProfile(appProfileId))) {
			throw new NullPointerException("No such application profile");
		}
	}
		
	@RequestMapping(value="/matrix", method=RequestMethod.GET)
	@ApiOperation(value = "getMatrix", nickname = "Get the Traffic Matrix")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = int[][].class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")}) 
	public int[][] getMatrix(){
		return TrafficData.getMatrix();
	}
	
	
	@ExceptionHandler(NullPointerException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleAppException(NullPointerException ex) {
		  return ex.getMessage();
	}
}
