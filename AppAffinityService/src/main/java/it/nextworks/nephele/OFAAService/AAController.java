package it.nextworks.nephele.OFAAService;


import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.nextworks.nephele.OFAAService.Inventory.Inventory;
import it.nextworks.nephele.OFAAService.ODLInventory.OpendaylightInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping(value="/affinity")
public class AAController {
	
	private Object connListLock = new Object();	
	private Map<String,Service> connList = new HashMap<>();
	
	@Autowired
	private Processor processor;
	
	@SuppressWarnings("unused")
	private Logger log = LoggerFactory.getLogger(this.getClass());
	

	/**
	 * POST /affinity/connection: posts a new connection.
	 * 
	 * @param service
	 * @return the response from g
	 */
	
	@RequestMapping(value="/connection", method=RequestMethod.POST)
	@ApiOperation(value = "postConnection", nickname = "Post new Service")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = ConnectionResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")}) 
	public ConnectionResponse postConnection(@RequestBody Service service){
		
		//POST the provided service's connections to the traffic matrix engine,
		//return serviceID and status.
		
		RestTemplate restTemplate = new RestTemplate();
		
		//POST the appProfile
		UriComponentsBuilder urlbuilder = 
				UriComponentsBuilder.fromHttpUrl(
				"http://127.0.0.1:8080/trafficmatrix/applicationprofile");
		HttpEntity<AppProfile> reqEntity = new HttpEntity<>(service.getAppProfile());
		
		ResponseEntity<String> respEntity = restTemplate.exchange(
				urlbuilder.toUriString(), HttpMethod.POST, reqEntity, String.class);
		
		//add to internal list
		Service serviceWithId = new Service(service, ServiceStatus.SCHEDULED);
		synchronized(connListLock){
			connList.put(respEntity.getBody(), serviceWithId);
		}
		try{
			processor.addTask(serviceWithId, connListLock);
		} catch (InterruptedException e) {
			System.err.println("How did this happen?");
			e.printStackTrace();
		}
		
		//return connection ID and status
		String responseID = respEntity.getBody();
		ConnectionResponse resp;
		
		synchronized(connListLock){
			resp = new ConnectionResponse(responseID, connList.get(responseID).status.toString());
		}
		
		return resp;
	}
	
	@RequestMapping(value="/connections", method=RequestMethod.GET)
	@ApiOperation(value = "getConnections", nickname = "get service Id")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Set.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")}) 
	public Set<String> getConnections(){		
		
		//GET list of all connection IDs
		Set<String> output;
		synchronized(connListLock){
			output = connList.keySet();
		}
		return output;
	}
	
	@RequestMapping(value="/connection/{connID}", method=RequestMethod.GET)
	@ApiOperation(value = "getConnectionById", nickname = "Get service status")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Service.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")}) 
	public Service getConnectionById(@PathVariable String connID){
		
		//GET a single connection's description by ID
		Service output;
		synchronized(connListLock){
			output = connList.get(connID);
		}
		
		if(output == null) throw new NullPointerException();
		
		else return output;
	}
	
	@RequestMapping(value="/connection/{connID}", method=RequestMethod.DELETE)
	@ApiOperation(value = "deleteConnection", nickname = "Delete service")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")}) 
	public void delConnectionById(@PathVariable String connID){
		
		RestTemplate restTemplate = new RestTemplate();
		
		Service requested;
		synchronized(connListLock){
			requested = connList.get(connID);
		}
		
		if(requested == null) throw new NullPointerException();
		
		else{
			requested.status = ServiceStatus.TERMINATING;
			UriComponentsBuilder urlbuilder = 
				UriComponentsBuilder.fromHttpUrl(
				"http://127.0.0.1:8080/trafficmatrix/applicationprofile/" + connID);
			restTemplate.delete(urlbuilder.toUriString());
		}
	}
	

	
	@RequestMapping(value="/invtest", method=RequestMethod.GET)
	@ApiOperation(value = "getInvTest", nickname = "Test inventory generation")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = OpendaylightInventory.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Failure")}) 
	public OpendaylightInventory getInventory(){
	
		RestTemplate restTemplate = new RestTemplate();
		
		int[][] nall = { 	
							{2,0,4,0,6,0},
							{0,1,0,3,0,5},
							{3,0,0,0,0,2},
							{0,4,0,0,1,0},
							{4,0,0,5,0,0},
							{0,3,6,0,0,0}
						};
		
		
		UriComponentsBuilder urlbuilder = 
				UriComponentsBuilder.fromHttpUrl("http://127.0.0.1:8080/translate");
		
		HttpEntity<int[][]> entity = new HttpEntity<>(nall);
		ResponseEntity<Inventory> response = 
				restTemplate.exchange(urlbuilder.toUriString(),
				HttpMethod.POST, entity, Inventory.class);
		
		return new OpendaylightInventory(response.getBody());
	}
	
	
	@ExceptionHandler(NullPointerException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleAppException(NullPointerException ex) {
		ex.printStackTrace();
		  return ex.getMessage();
	}
	
}

