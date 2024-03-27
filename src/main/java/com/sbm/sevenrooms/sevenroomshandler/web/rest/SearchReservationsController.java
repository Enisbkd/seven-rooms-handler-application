package com.sbm.sevenrooms.sevenroomshandler.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Stopwatch;
import com.sbm.sevenrooms.sevenroomshandler.services.ClientsAndReservationsService;
import com.sbm.sevenrooms.sevenroomshandler.services.TokenService;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class SearchReservationsController {

    private final Logger log = LoggerFactory.getLogger(SearchReservationsController.class);

    @Autowired
    ClientsAndReservationsService clientsAndReservationsService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    TokenService tokenService;

    @Value(value = "${sevenroomsApi.graviteeUrl}")
    private String sevenRoomsUrl;

    @Value(value = "${sevenroomsApi.api-key}")
    private String graviteeApiKey;

    @GetMapping("/searchReservations")
    public String getReservationsFromSevenRoomsApi(
        @RequestParam(required = false) String venue_id,
        @RequestParam(required = false, defaultValue = "50") int limit,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) String external_user_id,
        @RequestParam(required = false) String client_id,
        @RequestParam(required = false) String reference_code,
        @RequestParam(required = false) String external_reference_code,
        @RequestParam(required = false) String from_date,
        @RequestParam(required = false) String to_date,
        @RequestParam(required = false) String updated_since,
        @RequestParam(required = false) String shift_category,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String birthday_start,
        @RequestParam(required = false) String birthday_end,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String venue_group_id,
        @RequestParam(required = false) String external_id,
        @RequestParam(required = false) String sort_order,
        @RequestParam(required = false) String authToken
    ) {
        if (authToken == null) {
            authToken = tokenService.generateToken();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        // headers.set("X-Api-Key", graviteeApiKey);

        String reservationsUrl = sevenRoomsUrl + "reservations/export";

        StringBuilder queryString = new StringBuilder();
        queryString.append("?limit=").append(limit);
        putIfNotNull(queryString, "venue_id", venue_id);
        putIfNotNull(queryString, "cursor", cursor);
        putIfNotNull(queryString, "external_user_id", external_user_id);
        putIfNotNull(queryString, "client_id", client_id);
        putIfNotNull(queryString, "reference_code", reference_code);
        putIfNotNull(queryString, "external_reference_code", external_reference_code);
        putIfNotNull(queryString, "from_date", from_date);
        putIfNotNull(queryString, "to_date", to_date);
        putIfNotNull(queryString, "updated_since", updated_since);
        putIfNotNull(queryString, "shift_category", shift_category);
        putIfNotNull(queryString, "name", name);
        putIfNotNull(queryString, "phone", phone);
        putIfNotNull(queryString, "email", email);
        putIfNotNull(queryString, "birthday_start", birthday_start);
        putIfNotNull(queryString, "birthday_end", birthday_end);
        putIfNotNull(queryString, "status", status);
        putIfNotNull(queryString, "venue_group_id", venue_group_id);
        putIfNotNull(queryString, "external_id", external_id);
        putIfNotNull(queryString, "sort_order", sort_order);

        reservationsUrl += queryString.toString();

        HttpEntity<String> entity = new HttpEntity<>(headers);

        if (!authToken.isEmpty()) {
            log.info("Sending GET request to: {}", reservationsUrl);
            try {
                ResponseEntity<String> response = restTemplate.exchange(reservationsUrl, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Received response with status code: {}", response.getStatusCode().value());
                    try {
                        clientsAndReservationsService.appendSearchFields(response.getBody(), "reservation");
                    } catch (JsonProcessingException e) {
                        log.error("Error while formatting Json ", e);
                    }
                    // log.debug(response.getBody());
                    return response.getBody();
                } else {
                    return "Error: " + response.getStatusCode().value();
                }
            } catch (Exception e) {
                log.error("Error occurred while making GET request to {}: {}", reservationsUrl, e.getMessage());
                return "Error occurred while making GET request to " + reservationsUrl + e.getMessage();
            }
        } else {
            return "Error: Token empty , Request won't be sent.";
        }
    }

    @GetMapping("/searchAllReservations")
    public String getAllReservationsFromSevenRoomsApi() {
        String authToken = tokenService.generateToken();
        String cursor = "";
        Integer limit = 400;
        String response = getReservationsFromSevenRoomsApi(
            null,
            limit,
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
            null,
            authToken
        );
        cursor = clientsAndReservationsService.getCursor(response);
        int resultSize;
        int callCounter = 0;
        Stopwatch globalStopwatch = Stopwatch.createStarted();

        do {
            Stopwatch stopwatch = Stopwatch.createStarted();
            response = getReservationsFromSevenRoomsApi(
                null,
                limit,
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
                null,
                authToken
            );

            cursor = clientsAndReservationsService.getCursor(response);
            resultSize = clientsAndReservationsService.getResultsSize(response);
            callCounter += 1;
            log.info("Call number : " + callCounter);
            log.info("resultSize : " + resultSize);
            log.debug("cursor : " + cursor);
            stopwatch.stop();
            log.info("Iteration duration : " + stopwatch.elapsed().toString());
        } while (resultSize > 0);
        globalStopwatch.stop();
        log.info("Global duration : " + globalStopwatch.elapsed());

        return "Loop made " + callCounter + " calls with success.";
    }

    public void putIfNotNull(StringBuilder queryString, String key, String value) {
        if (value != null) {
            queryString.append("&").append(key).append("=").append(value);
        }
    }
}
