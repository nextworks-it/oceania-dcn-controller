package it.nextworks.nephele.OFAAService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by Marco Capitani on 13/09/17.
 *
 * @author Marco Capitani <m.capitani AT nextworks.it>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AppAffinityApplication.class)
@WebAppConfiguration
public class AppAffinityAppTest {

    @Test
    public void contextLoads() {
    }

}