package it.nextworks.nephele.OFAAService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import it.nextworks.nephele.OFAAService.Inventory.Inventory;
import it.nextworks.nephele.OFAAService.ODLInventory.OpendaylightInventory;

import java.util.Arrays;
import java.util.concurrent.Callable;

class ProcessingTasksTemplates {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String serverPort;

    ProcessingTasksTemplates(String serverPort) {
        this.serverPort = serverPort;
    }

    class TrafficMatGetter implements Callable<int[][]> {

        public int[][] call() {

            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder urlbuilder =
                    UriComponentsBuilder.fromHttpUrl(
                            "http://127.0.0.1:" + serverPort + "/trafficmatrix/matrix");
            UriComponents uri = urlbuilder.build();
            log.debug("Getting url " + uri);

            ResponseEntity<int[][]> responseEntity =
                    restTemplate.getForEntity(uri.toUri(), int[][].class);

            return responseEntity.getBody();
        }
    }

    class NetAllocGetter implements Callable<String> {

        int[][] matrix;
        String OEURL;

        NetAllocGetter(int[][] matrix, String OfflineEngineUrl)
        {
            this.matrix = matrix;
            this.OEURL = OfflineEngineUrl;
        }

        public String call() {

            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder urlbuilder =
                    UriComponentsBuilder.fromHttpUrl(
                            OEURL);

            HttpEntity<int[][]> entity = new HttpEntity<>(matrix);
            ResponseEntity<String> response =
                    restTemplate.exchange(urlbuilder.toUriString(),
                            HttpMethod.POST, entity, String.class);

            return response.getBody();
        }
    }

    class InventoryGetter implements Callable<Inventory> {

        int[][] netAlloc;

        InventoryGetter(int[][] nalloc){
            this.netAlloc = nalloc;
        }

        public Inventory call() {

            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder urlbuilder =
                    UriComponentsBuilder.fromHttpUrl("http://127.0.0.1:" + serverPort + "/translate");

            HttpEntity<int[][]> entity = new HttpEntity<>(netAlloc);
            ResponseEntity<Inventory> response =
                    restTemplate.exchange(urlbuilder.toUriString(),
                            HttpMethod.POST, entity, Inventory.class);

            return response.getBody();
        }
    }

    class NetAllocationMatrixGetter implements Callable<NetSolOutput> {

        String nallocId;
        String OEURL;

        NetAllocationMatrixGetter(String nallocId, String OEURL){
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

            if (responseEntity.getBody().status == CompStatus.COMPUTED) {
                return responseEntity.getBody();
            } else {
                return null;
            }
        }
    }

    class InventoryPutter implements Runnable {

        String ODLURL;
        Inventory inventory;

        InventoryPutter(Inventory inventory, String ODLURL){
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
            if (response1.getBody() == null) log.debug(response1.getStatusCode().toString());
            else log.debug(response1.getBody());

            HttpEntity<OpendaylightInventory> outgoingEntity =
                    new HttpEntity<>(odlInv, header);

            ResponseEntity<String> response2 =
                    restTemplate.exchange(urlbuilder.toUriString(), HttpMethod.PUT, outgoingEntity, String.class);
            if (response2.getBody() == null) log.debug(response2.getStatusCode().toString());
            else log.debug(response2.getBody());
        }
    }
}
