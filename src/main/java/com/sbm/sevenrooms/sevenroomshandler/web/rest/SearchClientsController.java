package com.sbm.sevenrooms.sevenroomshandler.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Stopwatch;
import com.sbm.sevenrooms.sevenroomshandler.services.ClientsAndReservationsService;
import com.sbm.sevenrooms.sevenroomshandler.services.TokenService;
import io.undertow.util.Headers;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

@RestController
@RequestMapping("/api")
public class SearchClientsController {

    private static final Logger log = LoggerFactory.getLogger(SearchClientsController.class);

    @Autowired
    TokenService tokenService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ClientsAndReservationsService clientsAndReservationsService;

    @Value(value = "${sevenroomsApi.graviteeUrl}")
    private String sevenRoomsUrl;

    @Value(value = "${sevenroomsApi.api-key}")
    private String graviteeApiKey;

    @Value(value = "${sevenroomsApi.venueGroupId}")
    private String venueGroupId;

    @GetMapping(path = "/searchClients", produces = "application/json")
    public String getClientsFromSevenRoomsApi(
        @RequestParam(required = true) String venue_group_id,
        @RequestParam(required = false) String limit,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) String venue_id,
        @RequestParam(required = false) String external_user_id,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String contact_type,
        @RequestParam(required = false) String tag,
        @RequestParam(required = false) String sort_order,
        @RequestParam(required = false) String has_loyalty_id,
        @RequestParam(required = false) String loyalty_id,
        @RequestParam(required = false) String loyalty_tier,
        @RequestParam(required = false) String loyalty_rank_min,
        @RequestParam(required = false) String loyalty_rank_max,
        @RequestParam(required = false) String birthday_start,
        @RequestParam(required = false) String birthday_end,
        @RequestParam(required = false) String updated_since,
        @RequestParam(required = false) String authToken
    ) {
        String url = sevenRoomsUrl + "clients/export";
        if (authToken == null) {
            authToken = tokenService.generateToken();
        }
        if (venue_group_id == null) {
            venue_group_id = venueGroupId;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.set(Headers.ACCEPT_STRING, "*/*");
        // headers.set("X-Api-Key", graviteeApiKey);

        StringBuilder query = new StringBuilder();
        query.append("?limit=").append(limit);
        putIfNotNull(query, "venue_group_id", venue_group_id);
        putIfNotNull(query, "cursor", cursor);
        putIfNotNull(query, "venue_id", venue_id);
        putIfNotNull(query, "external_user_id", external_user_id);
        putIfNotNull(query, "name", name);
        putIfNotNull(query, "email", email);
        putIfNotNull(query, "phone", phone);
        putIfNotNull(query, "contact_type", contact_type);
        putIfNotNull(query, "tag", tag);
        putIfNotNull(query, "sort_order", sort_order);
        putIfNotNull(query, "has_loyalty_id", has_loyalty_id);
        putIfNotNull(query, "loyalty_id", loyalty_id);
        putIfNotNull(query, "loyalty_tier", loyalty_tier);
        putIfNotNull(query, "birthday_start", birthday_start);
        putIfNotNull(query, "birthday_end", birthday_end);
        putIfNotNull(query, "updated_since", updated_since);
        putIfNotNull(query, "loyalty_rank_min", loyalty_rank_min);
        putIfNotNull(query, "loyalty_rank_max", loyalty_rank_max);

        url += query.toString();

        HttpEntity<String> entity = new HttpEntity<>(headers);

        if (!authToken.isEmpty()) {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Received response with status code: {}", response.getStatusCode().value());
                try {
                    clientsAndReservationsService.appendSearchFields(response.getBody(), "client");
                } catch (JsonProcessingException e) {
                    log.error("Error while formatting Json ", e);
                }
                return response.getBody();
            } else {
                return "Error: " + response.getStatusCode().value();
            }
        } else {
            return "Error: Token empty , Request won't be sent.";
        }
    }

    @GetMapping("/searchAllClients")
    public ResponseEntity<String> getAllReservationsFromSevenRoomsApi() {
        String authToken = tokenService.generateToken();
        if (authToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Auth Token Empty, cannot make request.");
        }
        String cursor = "";
        Integer limit = 400;
        String response = getClientsFromSevenRoomsApi(
            venueGroupId,
            limit.toString(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            authToken
        );
        cursor = clientsAndReservationsService.getCursor(response);
        log.debug("First response : " + response);
        int resultSize;
        Integer callCounter = 0;
        Stopwatch globalStopwatch = Stopwatch.createStarted();

        do {
            Stopwatch stopwatch = Stopwatch.createStarted();
            response = getClientsFromSevenRoomsApi(
                venueGroupId,
                limit.toString(),
                cursor,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                authToken
            );

            cursor = clientsAndReservationsService.getCursor(response);
            resultSize = clientsAndReservationsService.getResultsSize(response);
            callCounter += 1;

            log.info("Call number : " + callCounter);
            log.debug("Response : " + response);
            log.info("resultSize : " + resultSize);
            log.debug("cursor : " + cursor);
            stopwatch.stop();
            log.info("Iteration duration : " + stopwatch.elapsed().toString());
        } while (resultSize > 0);
        globalStopwatch.stop();
        log.info("Global duration : " + globalStopwatch.elapsed());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Loop made " + callCounter + " calls with success.");
    }

    public void putIfNotNull(StringBuilder queryString, String key, String value) {
        if (value != null) {
            queryString.append("&").append(key).append("=").append(value);
        }
    }
}
