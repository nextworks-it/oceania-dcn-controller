package it.nextworks.nephele.OFAAService;


import org.springframework.beans.factory.annotation.Value;
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

import it.nextworks.nephele.TrafficMatrixEngine.AppProfile;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping(value="/affinity")
public class AppAffinityController {

	private ConcurrentMap<String,Service> connList = new ConcurrentHashMap<>(); //service IDs dictionary
	
	@Autowired
	private Processor processor;

	@SuppressWarnings("unused")
	private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${server.port}")
    private String serverPort;

	@RequestMapping(value="/connection", method=RequestMethod.POST)
	@ApiOperation(value = "postConnection", nickname = "Post new Service")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = ConnectionResponse.class)})
	public ConnectionResponse postConnection(@RequestBody Service service){

		if (!service.validateAndInit()){
			throw new IllegalArgumentException("Invalid service: " +
                    "at least one connection with valid source and destination must be specified.");
		}

		//POST the provided service's connections to the traffic matrix engine,
		//return serviceID and status.
		
		RestTemplate restTemplate = new RestTemplate();
		
		//POST the appProfile
		UriComponentsBuilder urlbuilder = 
				UriComponentsBuilder.fromHttpUrl(
				"http://127.0.0.1:" + serverPort + "/trafficmatrix/applicationprofile");
		HttpEntity<AppProfile> reqEntity = new HttpEntity<>(service.makeAppProfile());

        try {
            ResponseEntity<String> respEntity = restTemplate.exchange(
                    urlbuilder.toUriString(), HttpMethod.POST, reqEntity, String.class);

            //add to internal list
			String responseID = respEntity.getBody();
            Service serviceWithId = new Service(service, ServiceStatus.SCHEDULED);

            connList.put(responseID, serviceWithId);

            processor.startRefreshing(serviceWithId);

            //return connection ID and status

            return new ConnectionResponse(
                    responseID, connList.get(responseID).status.toString());

        }
        catch (Exception exc){
            log.error("Could not POST the appProfile, nested exception is: \n", exc);
            throw new NullPointerException("Could not POST the appProfile");
        }
	}
	
	@RequestMapping(value="/connections", method=RequestMethod.GET)
	@ApiOperation(value = "getConnections", nickname = "get service Id")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Set.class)})
	public Set<String> getConnections(){		
		
		//GET list of all connection IDs
		Set<String> output;
		output = connList.keySet();
		return output;
	}
	
	@RequestMapping(value="/connection/{connID}", method=RequestMethod.GET)
	@ApiOperation(value = "getConnectionById", nickname = "Get service status")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = Service.class)})
	public Service getConnectionById(@PathVariable String connID){
		
		//GET a single connection's description by ID
		Service output = connList.get(connID);

		
		if(output == null) throw new NullPointerException("Nonexistent connection.");
		
		else return output;
	}
	
	@RequestMapping(value="/connection/{connID}", method=RequestMethod.DELETE)
	@ApiOperation(value = "deleteConnection", nickname = "Delete service")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success")})
	public void delConnectionById(@PathVariable String connID){
		
		RestTemplate restTemplate = new RestTemplate();
		
		Service requested = connList.get(connID);
		
		if(requested == null) throw new NullPointerException("Nonexistent connection.");
		
		else{
			requested.status = ServiceStatus.TERMINATING;
            processor.addTerminating(requested);
			UriComponentsBuilder urlbuilder = 
				UriComponentsBuilder.fromHttpUrl(
				"http://127.0.0.1:8080/trafficmatrix/applicationprofile/" + connID);
			restTemplate.delete(urlbuilder.toUriString());
		}
	}
	
/*
	@RequestMapping(value="/invtest", method=RequestMethod.GET)
	@ApiOperation(value = "getInvTest", nickname = "Test inventory generation")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = OpendaylightInventory.class)})
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
*/
	
	@ExceptionHandler(NullPointerException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleNullPointer(NullPointerException ex) {
		log.info(ex.getMessage(), ex);
		return ex.getMessage();
	}

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex) {
        log.info(ex.getMessage(), ex);
        return ex.getMessage();
    }

}

