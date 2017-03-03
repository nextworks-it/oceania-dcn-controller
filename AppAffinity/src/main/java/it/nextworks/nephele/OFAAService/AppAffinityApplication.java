package it.nextworks.nephele.OFAAService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages =
        {"it.nextworks.nephele.OFAAService",
                "it.nextworks.nephele.OFTranslator",
                "it.nextworks.nephele.TrafficMatrixEngine"})
public class AppAffinityApplication {


    public static void main(String[] args) {
        SpringApplication.run(AppAffinityApplication.class, args);
    }
}
