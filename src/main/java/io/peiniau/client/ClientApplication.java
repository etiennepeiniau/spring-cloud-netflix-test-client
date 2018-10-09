package io.peiniau.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
@EnableCircuitBreaker
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

}

@RestController
class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    private final RandomIntClient randomInt;

    private final RandomLetterClient randomLetter;

    private final RandomColorClient randomColor;

    @Autowired
    public ClientController(RandomIntClient randomInt, RandomLetterClient randomLetter, RandomColorClient randomColor) {
        this.randomInt = randomInt;
        this.randomLetter = randomLetter;
        this.randomColor = randomColor;
    }

    @GetMapping("/random")
    @ResponseBody
    public AllValuesResponse random() {
        // TODO sequential calls can be converted into reactive streams
        IntValueResponse randomIntegerResponse = randomInt.random();
        LetterValueResponse randomLetterResponse = randomLetter.random();
        ColorValueResponse randomColorResponse = randomColor.random();
        AllValuesResponse allValuesResponse = new AllValuesResponse(
                randomIntegerResponse.getValue(),
                randomLetterResponse.getValue(),
                new Color(
                        randomColorResponse.getR(),
                        randomColorResponse.getG(),
                        randomColorResponse.getB()
                ));
        log.info("Response: {}", allValuesResponse);
        return allValuesResponse;
    }
}

@FeignClient(value = "randomint", fallback = RandomIntClientFallback.class)
interface RandomIntClient {

    @RequestMapping(method = RequestMethod.GET, value = "/random")
    IntValueResponse random();

}

@Component
class RandomIntClientFallback implements RandomIntClient {

    @Override
    public IntValueResponse random() {
        IntValueResponse response = new IntValueResponse();
        response.setValue(0);
        return response;
    }

}

@FeignClient(value = "randomletter", fallback = RandomLetterClientFallback.class)
interface RandomLetterClient {

    @RequestMapping(method = RequestMethod.GET, value = "/random")
    LetterValueResponse random();

}

@Component
class RandomLetterClientFallback implements RandomLetterClient {

    @Override
    public LetterValueResponse random() {
        LetterValueResponse response = new LetterValueResponse();
        response.setValue('A');
        return response;
    }

}

@FeignClient(value = "randomcolor", fallback = RandomColorClientFallback.class)
interface RandomColorClient {

    @RequestMapping(method = RequestMethod.GET, value = "/random")
    ColorValueResponse random();

}

@Component
class RandomColorClientFallback implements RandomColorClient {

    @Override
    public ColorValueResponse random() {
        ColorValueResponse response = new ColorValueResponse();
        response.setR(0);
        response.setG(0);
        response.setB(0);
        return response;
    }

}

@Data
class IntValueResponse {

    private Integer value;

}

@Data
class LetterValueResponse {

    private Character value;

}

@Data
class ColorValueResponse {

    private Integer r;
    private Integer g;
    private Integer b;

}

@Data
@AllArgsConstructor
class Color {

    private Integer r;
    private Integer g;
    private Integer b;

}

@Data
@AllArgsConstructor
class AllValuesResponse {

    private Integer randomInteger;
    private Character randomLetter;
    private Color randomColor;

}