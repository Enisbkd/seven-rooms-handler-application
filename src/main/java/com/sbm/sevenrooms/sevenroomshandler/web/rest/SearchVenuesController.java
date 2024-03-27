package com.sbm.sevenrooms.sevenroomshandler.web.rest;

import com.sbm.sevenrooms.sevenroomshandler.services.TokenService;
import com.sbm.sevenrooms.sevenroomshandler.services.VenuesService;
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

@RestController
@RequestMapping("/api")
public class SearchVenuesController {

    private static final Logger log = LoggerFactory.getLogger(SearchVenuesController.class);

    @Value(value = "${sevenroomsApi.graviteeUrl}")
    private String sevenRoomsUrl;

    @Value(value = "${sevenroomsApi.api-key}")
    private String graviteeApiKey;

    @Autowired
    TokenService tokenService;

    @Autowired
    VenuesService venuesService;

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/searchVenues")
    public ResponseEntity<String> getVenues(
        @RequestParam(value = "venue_group_id", required = false) String venueGroupId,
        @RequestParam(value = "limit", defaultValue = "50") int limit,
        @RequestParam(value = "cursor", required = false) String cursor,
        @RequestParam(value = "active", defaultValue = "false") String active
    ) {
        String authToken = tokenService.generateToken();

        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", authToken);

        // headers.set("X-7rooms-Authorization", authToken);
        // headers.set("X-Api-Key", graviteeApiKey);

        String venuesUrl = sevenRoomsUrl + "venues";

        StringBuilder query = new StringBuilder();
        query.append("?limit=").append(limit);
        putIfNotNull(query, "venue_group_id", venueGroupId);
        putIfNotNull(query, "cursor", cursor);
        putIfNotNull(query, "active", active);

        venuesUrl += query.toString();

        try {
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Making the GET request
            ResponseEntity<String> response = restTemplate.exchange(venuesUrl, HttpMethod.GET, entity, String.class);
            // Logging successful response
            log.info("GET request to {} successful with status code: {}", venuesUrl, response.getStatusCode().value());

            venuesService.parseVenues(response.getBody());

            return response;
        } catch (Exception e) {
            // Logging error
            log.error("Error occurred while making GET request to {}: {}", venuesUrl, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while fetching venues");
        }
    }

    public void putIfNotNull(StringBuilder queryString, String key, String value) {
        if (value != null) {
            queryString.append("&").append(key).append("=").append(value);
        }
    }
}
