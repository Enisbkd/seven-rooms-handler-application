package com.sbm.sevenrooms.sevenroomshandler.services.impl;

import com.sbm.sevenrooms.sevenroomshandler.services.TibcoService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class TibcoServiceImpl implements TibcoService {

    private final Logger log = LoggerFactory.getLogger(TibcoServiceImpl.class);

    @Value(value = "${tibco.url}")
    private String tibcoUrl;

    public void makePostRequest(String requestBody) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        try {
            log.info("Sending JsonNode to Tibco: Url : " + tibcoUrl);

            Mono<String> future = asyncHttpCall(HttpMethod.POST, tibcoUrl, requestBody, headers);

            // Log the response
            future.doOnNext(response -> log.info("Recieved Response : {}", response));
        } catch (Exception e) {
            // Log and handle exceptions
            log.error("Error occurred while making POST request: {}", e.getMessage());
        }
    }

    private Mono<String> asyncHttpCall(HttpMethod httpMethod, String url, String body, Map<String, String> headers) {
        return WebClient.create(url)
            .method(httpMethod)
            .bodyValue(body)
            .headers(httpHeaders -> headers.forEach(httpHeaders::add))
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(response -> log.info("Recieved Response : {}", response));
    }
}
