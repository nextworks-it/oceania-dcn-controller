package it.nextworks.nephele.OFAAService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import it.nextworks.nephele.OFAAService.Inventory.Const;
import it.nextworks.nephele.OFAAService.Inventory.Inventory;
import it.nextworks.nephele.OFAAService.ODLInventory.OpendaylightInventory;

public class ProcessingTask implements Runnable {

	/**
	 * Service that is being added
	 */
	private Service serv;
	private Object lock;
	
	private String ODLURL;
	private String OfflineEngineURL;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public ProcessingTask(Service inServ, Object inLock, String inODLURL, String inOEURL){
		serv = inServ;
		lock = inLock;
		ODLURL = inODLURL;
		OfflineEngineURL = inOEURL;
	}

	public void run() {

		int[][] traffMatrix = new int[Const.P * Const.W][Const.P * Const.W];
		int[][] matrix = new int[Const.I * Const.T][Const.P * Const.W];
		Inventory inventory	= new Inventory();

		try {traffMatrix = getTraffMatrix();}		
		catch (Exception e) {
			log.error("wrong URI or connection problem on GETting the traffic matrix"); 
			log.error(e.getMessage()); 
			for (StackTraceElement stack : e.getStackTrace()){log.error(stack.toString());}
			System.exit(1);
		}

		try {
			String netallocID = postTraffMatrix(traffMatrix);
			
			synchronized(lock){
				if (serv.status.equals(ServiceStatus.SCHEDULED)){
					serv.status = ServiceStatus.ESTABLISHING;
				}
			}
			
			matrix = getMatrix(netallocID);
			
			if (matrix == null){
				//TODO: gestire davvero
			}
		}		
		catch (Exception e) {
			log.error("wrong URI or connection problem on POSTting the traffic matrix and/or"
					+ "GETting the scheduling matrix"); 
			log.error(e.getMessage()); 
			for (StackTraceElement stack : e.getStackTrace()){log.error(stack.toString());}
			System.exit(1);
		}

		try {inventory = getInventory(matrix);}
		catch (Exception e) {
			log.error("wrong URI or connection problem on POSTing the scheduling matrix"); 
			log.error(e.getMessage()); 
			for (StackTraceElement stack : e.getStackTrace()){log.error(stack.toString());}
			System.exit(1);
		}

		try {putInventory(inventory);}
		catch (Exception e) {
			log.error("wrong URI or connection problem on PUTting the flow rules"); 
			log.error(e.getMessage()); 
			for (StackTraceElement stack : e.getStackTrace()){log.error(stack.toString());}
			System.exit(1);
		}
		
		synchronized(lock){
			if (serv.status.equals(ServiceStatus.ESTABLISHING)){
				serv.status = ServiceStatus.ACTIVE;
			}
			else if (serv.status.equals(ServiceStatus.TERMINATING)){
				serv.status = ServiceStatus.DELETED;

			}
		}
	}
	

	/**
	 * GET matrix from traffic matrix engine
	 * @return traffic matrix
	 */
	public int[][] getTraffMatrix(){

		RestTemplate restTemplate = new RestTemplate();
	
		UriComponentsBuilder geturlbuilder = 
				UriComponentsBuilder.fromHttpUrl(
				"http://127.0.0.1:8080/trafficmatrix/matrix");
		UriComponents uri = geturlbuilder.build();
			 
		ResponseEntity<int[][]> responseEntity = 
				restTemplate.getForEntity(uri.toUri(), int[][].class);

		return responseEntity.getBody();
	}
	
	
	/**
	 * POST traffic matrix and get net allocation ID.
	 * @param matrix traffic matrix
	 * @return net allocation ID
	 */
	public String postTraffMatrix(int[][] matrix){
	
		RestTemplate restTemplate = new RestTemplate();
		
		UriComponentsBuilder urlbuilder = 
				UriComponentsBuilder.fromHttpUrl(
				OfflineEngineURL);
		
		HttpEntity<int[][]> entity = new HttpEntity<>(matrix);
		ResponseEntity<String> response = 
				restTemplate.exchange(urlbuilder.toUriString(),
				HttpMethod.POST, entity, String.class);
		
		return response.getBody();
	}
	

	/**
	 * GET matrix from offline scheduling engine
	 * @param nallocId network allocation ID
	 * @return Scheduling matrix
	 */
	public int[][] getMatrix(String nallocId){

		RestTemplate restTemplate = new RestTemplate();
	
		UriComponentsBuilder geturlbuilder = 
				UriComponentsBuilder.fromHttpUrl(
				OfflineEngineURL + "/" + nallocId);
		UriComponents uri = geturlbuilder.build();
			 
		ResponseEntity<NetSolOutput> responseEntity = 
				restTemplate.getForEntity(uri.toUri(), NetSolOutput.class);

		if (responseEntity.getBody().status == CompStatus.COMPUTED)
		{
			return responseEntity.getBody().matrix;
		}
		else{
			return null;
		}
	}

	
	/**
	 * POST matrix and get flow rules in return from translator component.
	 * @param nalloc Scheduling matrix (i.e. allocation of network paths)
	 * @return Inventory of the network topology w/ flow rules
	 */
	public Inventory getInventory(int[][] nalloc){
	
		RestTemplate restTemplate = new RestTemplate();
		
		UriComponentsBuilder urlbuilder = 
				UriComponentsBuilder.fromHttpUrl("http://127.0.0.1:8080/translate");
		
		HttpEntity<int[][]> entity = new HttpEntity<>(nalloc);
		ResponseEntity<Inventory> response = 
				restTemplate.exchange(urlbuilder.toUriString(),
				HttpMethod.POST, entity, Inventory.class);
		
		return response.getBody();
	}
	
	
	/**
	 * PUT network inventory in ODL controller's config data tree
	 * @param inventory Network inventory
	 */
	public void putInventory(Inventory inventory){
		
		//
		
		RestTemplate restTemplate = new RestTemplate();
	
		OpendaylightInventory odlInv = new OpendaylightInventory(inventory);
		
		UriComponentsBuilder urlbuilder = 
				UriComponentsBuilder.fromHttpUrl(
				ODLURL);
		
		HttpHeaders header = new HttpHeaders();
		header.add("Authorization", "Basic " + "YWRtaW46YWRtaW4=");
		header.add("Content-Type", "application/json");
		
		HttpEntity<?> deleteEntity = 
				new HttpEntity<>(header);
		
		restTemplate.exchange(urlbuilder.toUriString(), HttpMethod.DELETE, deleteEntity, String.class);
		HttpEntity<OpendaylightInventory> outgoingEntity = 
				new HttpEntity<OpendaylightInventory>(odlInv,  header);
		
		
		restTemplate.put(urlbuilder.toUriString(), outgoingEntity);
	
		//ResponseEntity<Object> response = 
		//		restTemplate.exchange(.toUriString(),
		//		HttpMethod.PUT, outgoingEntity, Object.class);
	}
}
