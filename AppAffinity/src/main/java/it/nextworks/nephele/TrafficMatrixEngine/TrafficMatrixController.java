package it.nextworks.nephele.TrafficMatrixEngine;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.http.HttpStatus;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/trafficmatrix")
public class TrafficMatrixController {

    @Autowired
    private TrafficData data;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/applicationprofile", method = RequestMethod.POST)
    @ApiOperation(value = "postAppProfile", nickname = "Post an app profile to be added")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class)})
    public String postAppProfile(@RequestBody AppProfile appProfile) {
        return data.addProfile(appProfile);
    }

    @RequestMapping(value = "/applicationprofile/{appProfileId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "DeleteAppProfile", nickname = "Delete an add profile")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success")})
    public void deleteAppProfile(@PathVariable String appProfileId) {
        if (!(data.deleteProfile(appProfileId))) {
            throw new NullPointerException("No such application profile");
        }
    }

    @RequestMapping(value = "/matrix", method = RequestMethod.GET)
    @ApiOperation(value = "getMatrix", nickname = "Get the Traffic Matrix")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = int[][].class)})
    public int[][] getMatrix() {
        return data.getMatrix();
    }

    @RequestMapping(value = "/changes", method = RequestMethod.GET)
    @ApiOperation(value = "getChanges", nickname = "Get the Traffic Matrix")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = TrafficChanges.class)})
    public TrafficChanges getChanges() {  // TODO change to traffic changes
        return new TrafficChanges(data.getChanges());
    }

    @RequestMapping(value = "/computation", method = RequestMethod.POST)
    @ApiOperation(value = "getMatrix", nickname = "Get the Traffic Matrix")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = TrafficChanges.class)})
    public TrafficChanges startComputation() {
        return new TrafficChanges(data.getAndResetChanges());
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAppException(NullPointerException ex) {
        log.info(ex.getMessage(), ex);
        return ex.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleAppException(IllegalArgumentException ex) {
        log.info(ex.getMessage(), ex);
        return ex.getMessage();
    }
}
