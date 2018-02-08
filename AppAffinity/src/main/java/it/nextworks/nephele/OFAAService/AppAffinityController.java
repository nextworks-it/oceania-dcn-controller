package it.nextworks.nephele.OFAAService;

import it.nextworks.nephele.OFAAService.ODLInventory.OpendaylightInventory;
import it.nextworks.nephele.OFTranslator.Inventory;
import it.nextworks.nephele.appaffdb.DbManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import it.nextworks.nephele.TrafficMatrixEngine.AppProfile;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@CrossOrigin
@RestController
@RequestMapping(value = "/affinity")
public class AppAffinityController {

    @Autowired
    private Processor processor;

    @Autowired
    private DbManager db;

    @SuppressWarnings("unused")
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${server.port}")
    private String serverPort;

    @RequestMapping(value = "/connection", method = RequestMethod.POST)
    @ApiOperation(value = "postConnection", nickname = "Post new Service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ConnectionResponse.class)})
    public ConnectionResponse postConnection(@RequestBody Service service) {

        if (!service.validateAndInit()) { //actually it's either true or an exception...
            throw new IllegalArgumentException("Invalid service: " + //Safety net, should not be reached
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
            Service serviceWithId = new Service(service, ServiceStatus.REQUESTED);
            serviceWithId.registerId(responseID);

            //return connection ID and status

            log.info("Service request received. ID: {}.", responseID);
            // SAVE request in db
            db.saveService(responseID, serviceWithId);

            processor.startRefreshing();

            return new ConnectionResponse(
                responseID, db.queryServiceWithId(responseID).status.toString()
            );

        } catch (Exception exc) {
            log.error("Could not POST the appProfile, nested exception is: \n", exc);
            throw new NullPointerException("Could not POST the appProfile");
        }
    }

    @RequestMapping(value = "/connections", method = RequestMethod.GET)
    @ApiOperation(value = "getConnections", nickname = "get service Id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = Set.class)})
    public Set<String> getConnections() {

        //GET list of all connection IDs
        return db.queryServicesId();
    }

    @RequestMapping(value = "/connection/{connID}", method = RequestMethod.GET)
    @ApiOperation(value = "getConnectionById", nickname = "Get service status")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = Service.class)})
    public Service getConnectionById(@PathVariable String connID) {

        //GET a single connection's description by ID
        Service output = db.queryServiceWithId(connID);

        if (output == null) throw new NullPointerException("Nonexistent connection " + connID);

        else return output;
    }

    @RequestMapping(value = "/connection/{connID}", method = RequestMethod.DELETE)
    @ApiOperation(value = "deleteConnection", nickname = "Delete service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success")})
    public void delConnectionById(@PathVariable String connID) {

        RestTemplate restTemplate = new RestTemplate();

        Service requested = db.queryServiceWithId(connID);

        if (requested == null) throw new NullPointerException("Nonexistent connection " + connID);

        else {
            processor.addTerminating(requested);
            UriComponentsBuilder urlbuilder =
                UriComponentsBuilder.fromHttpUrl(
                    "http://127.0.0.1:" + serverPort + "/trafficmatrix/applicationprofile/" + connID);
            restTemplate.delete(urlbuilder.toUriString());
            processor.startRefreshing();
        }
    }


    @RequestMapping(value = "/test/invtest", method = RequestMethod.POST)
    @ApiOperation(value = "getInvTest", nickname = "Test inventory generation")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = OpendaylightInventory.class)})
    public OpendaylightInventory getInventory(@RequestBody int[][] nall) {

        RestTemplate restTemplate = new RestTemplate();


        UriComponentsBuilder urlbuilder =
            UriComponentsBuilder.fromHttpUrl("http://127.0.0.1:" + serverPort + "/translate");

        HttpEntity<int[][]> entity = new HttpEntity<>(nall);
        ResponseEntity<Inventory> response =
            restTemplate.exchange(urlbuilder.toUriString(),
                HttpMethod.POST, entity, Inventory.class);

        return new OpendaylightInventory(response.getBody());
    }


    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNullPointer(NullPointerException ex) {
        log.warn("Error during call: {}.", ex.getMessage());
        log.debug("Exception details: ", ex);
        return "{ \"message\": \"" + ex.getMessage() + "\"}";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Error during call: {}.", ex.getMessage());
        log.debug("Exception details: ", ex);
        return "{ \"message\": \"" + ex.getMessage() + "\"}";
    }

}

