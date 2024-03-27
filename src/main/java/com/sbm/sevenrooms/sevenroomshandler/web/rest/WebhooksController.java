package com.sbm.sevenrooms.sevenroomshandler.web.rest;

import com.sbm.sevenrooms.sevenroomshandler.services.TibcoService;
import com.sbm.sevenrooms.sevenroomshandler.services.impl.ClientsAndReservationsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhooksController {

    private final Logger log = LoggerFactory.getLogger(WebhooksController.class);

    @Autowired
    ClientsAndReservationsServiceImpl clientsAndReservationsServiceImpl;

    @Autowired
    TibcoService tibcoService;

    @GetMapping(path = "testGet")
    public String testGetWebhook() {
        return "Get Webhooks";
    }

    @PostMapping(path = "testPost")
    public String testPostEndpoint(@RequestBody String plaintext) {
        return "Received message: " + plaintext;
    }

    @PostMapping(path = "webhooks")
    public ResponseEntity<String> handleWebhookPost(@RequestBody String jsonNode) {
        log.debug("Post Endpoint : Received Json : {}", jsonNode);
        try {
            clientsAndReservationsServiceImpl.iterateAndSendToTopic(jsonNode);
            tibcoService.makePostRequest(jsonNode);
            return ResponseEntity.ok("Messages sent to Kafka synchronously And to Tibco asynchronously");
        } catch (Exception e) {
            log.error("Error : ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error : " + e);
        }
    }

    public WebhooksController() {}
}
