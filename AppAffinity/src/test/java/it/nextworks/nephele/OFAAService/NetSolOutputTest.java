package it.nextworks.nephele.OFAAService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

/**
 * Created by Marco Capitani on 06/10/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */
public class NetSolOutputTest {

    @Test
    public void testDeserFail() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String response = "{\n" +
                " \"Status\": \"FAILED\"\n" +
                "}";
        NetSolOutput netSolOutput = mapper.readValue(response, NetSolOutput.class);
        System.out.println(netSolOutput);
        switch (netSolOutput.status) {
            case FAILED:
                System.out.println("yay");
                break;
            default:
                System.out.println("nay");
        }
    }

}