package musinco.web.inference;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@RestController
public class Controller {

    @GetMapping("/test")
    public String test() {
        return KafkaConnector.execTestSparqlQuery();
    }

    @GetMapping("/mptest")
    public String mptest() {
        KafkaConnector mp = new KafkaConnector();
        mp.sendDataForInference();
        return "Data sent to inference engine.";
    }
}
