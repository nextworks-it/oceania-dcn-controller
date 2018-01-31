package it.nextworks.nephele.OFAAService;

import it.nextworks.nephele.OFTranslator.Inventory;
import it.nextworks.nephele.TrafficMatrixEngine.TrafficChanges;
import it.nextworks.nephele.TrafficMatrixEngine.TrafficMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import it.nextworks.nephele.OFAAService.ODLInventory.OpendaylightInventory;

import java.util.Arrays;
import java.util.concurrent.Callable;

class ProcessingTasksTemplates {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String serverPort;

    ProcessingTasksTemplates(String serverPort) {
        this.serverPort = serverPort;
    }

    class TrafficMatGetter implements Callable<TrafficMatrix> {

        @Override
        public TrafficMatrix call() { // TODO might change

            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder urlbuilder =
                UriComponentsBuilder.fromHttpUrl(
                    "http://127.0.0.1:" + serverPort + "/trafficmatrix/matrix");
            UriComponents uri = urlbuilder.build();

            ResponseEntity<int[][]> responseEntity = // TODO might change
                restTemplate.getForEntity(uri.toUri(), int[][].class); // TODO might change

            return new TrafficMatrix(responseEntity.getBody()); // TODO might change
        }
    }

    class TrafficMatChangesGetter implements Callable<TrafficChanges> {

        public TrafficChanges call() {

            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder urlbuilder =
                    UriComponentsBuilder.fromHttpUrl(
                            "http://127.0.0.1:" + serverPort + "/trafficmatrix/computation");
            UriComponents uri = urlbuilder.build();

            ResponseEntity<TrafficChanges> responseEntity =
                    restTemplate.postForEntity(uri.toUri(), null, TrafficChanges.class);

            return responseEntity.getBody();
        }
    }

    class NetAllocIdGetter implements Callable<String> {

        TrafficMatrix matrix;
        String OEURL;

        NetAllocIdGetter(TrafficMatrix matrix, String OfflineEngineUrl) {
            this.matrix = matrix;
            this.OEURL = OfflineEngineUrl;
        }

        public String call() {

            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder urlbuilder =
                UriComponentsBuilder.fromHttpUrl(
                    OEURL);

            HttpEntity<TrafficMatrix> entity = new HttpEntity<>(matrix);  // TODO might change
            ResponseEntity<String> response =
                restTemplate.exchange(urlbuilder.toUriString(),
                    HttpMethod.POST, entity, String.class);

            return response.getBody();
        }
    }

    class NetAllocChangesIdGetter implements Callable<String> {

        TrafficChanges matrix;
        String OEURL;

        NetAllocChangesIdGetter(TrafficChanges matrix, String OfflineEngineUrl) {
            this.matrix = matrix;
            this.OEURL = OfflineEngineUrl;
        }

        public String call() {

            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder urlbuilder =
                    UriComponentsBuilder.fromHttpUrl(
                            OEURL);

            HttpEntity<TrafficChanges> entity = new HttpEntity<>(matrix);
            ResponseEntity<String> response =
                    restTemplate.exchange(urlbuilder.toUriString(),
                            HttpMethod.POST, entity, String.class);

            return response.getBody();
        }
    }

    class InventoryGetter implements Callable<Inventory> {

        NetSolBase netAlloc;

        InventoryGetter(NetSolBase nalloc) {
            this.netAlloc = nalloc;
        }

        public Inventory call() {

            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder urlbuilder =
                UriComponentsBuilder.fromHttpUrl("http://127.0.0.1:" + serverPort + "/translate");

            HttpEntity<NetSolBase> entity = new HttpEntity<>(netAlloc);
            ResponseEntity<Inventory> response =
                restTemplate.exchange(urlbuilder.toUriString(),
                    HttpMethod.POST, entity, Inventory.class);

            return response.getBody();
        }
    }

    class NetAllocationMatrixGetter implements Callable<NetSolOutput> {

        String nallocId;
        String OEURL;

        NetAllocationMatrixGetter(String nallocId, String OEURL) {
            this.nallocId = nallocId;
            this.OEURL = OEURL;
        }

        public NetSolOutput call() {

            RestTemplate restTemplate = new RestTemplate();
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            converter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.APPLICATION_JSON));
            restTemplate.getMessageConverters().add(converter);

            UriComponentsBuilder geturlbuilder =
                UriComponentsBuilder.fromHttpUrl(
                    OEURL + "/" + nallocId);
            UriComponents uri = geturlbuilder.build();

            ResponseEntity<NetSolOutput> responseEntity =
                restTemplate.getForEntity(uri.toUri(), NetSolOutput.class);

            NetSolOutput response = responseEntity.getBody();
            if (null == response.method) {
                response.method = "FULL";
                log.warn("{}", response.method);
                log.warn("Offline engine returned null method. Assuming FULL");
            if (!"FULL".equals(response.method)) {
                    throw new IllegalArgumentException(String.format("Offline engine provided solution with wrong method." +
                            "Expected: %s. Found: %s.", "FULL", response.method));
                }
            }
            return response;
        }
    }

    class NetAllocationChangesGetter implements Callable<NetSolChanges> {

        String nallocId;
        String OEURL;

        NetAllocationChangesGetter(String nallocId, String OEURL) {
            this.nallocId = nallocId;
            this.OEURL = OEURL;
        }

        public NetSolChanges call() {

            RestTemplate restTemplate = new RestTemplate();
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            converter.setSupportedMediaTypes(Arrays.asList(
                    MediaType.APPLICATION_OCTET_STREAM,
                    MediaType.APPLICATION_JSON));
            restTemplate.getMessageConverters().add(converter);

            UriComponentsBuilder geturlbuilder =
                    UriComponentsBuilder.fromHttpUrl(
                            OEURL + "/" + nallocId);
            UriComponents uri = geturlbuilder.build();

            ResponseEntity<NetSolChanges> responseEntity =
                    restTemplate.getForEntity(uri.toUri(), NetSolChanges.class);

            if (!"INCREMENTAL".equals(responseEntity.getBody().method)) {
                throw new IllegalArgumentException(String.format("Offline engine provided solution with wrong method." +
                        "Expected: %s. Found: %s.", "INCREMENTAL", responseEntity.getBody().method));
            }

            return responseEntity.getBody();
        }
    }

    class InventoryPutter implements Runnable {

        String ODLURL;
        Inventory inventory;

        InventoryPutter(Inventory inventory, String ODLURL) {
            this.inventory = inventory;
            this.ODLURL = ODLURL;
        }

        public void run() {

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

            ResponseEntity<String> response1 =
                    restTemplate.exchange(urlbuilder.toUriString(), HttpMethod.DELETE, deleteEntity, String.class);
            log.debug("Inventory wipe got status {}.", response1.getStatusCode().toString());

            HttpEntity<OpendaylightInventory> outgoingEntity =
                new HttpEntity<>(odlInv, header);

            ResponseEntity<String> response2 =
                restTemplate.exchange(urlbuilder.toUriString(), HttpMethod.PUT, outgoingEntity, String.class);
            log.debug("Inventory put got status {}.", response2.getStatusCode().toString());
        }
    }
}
