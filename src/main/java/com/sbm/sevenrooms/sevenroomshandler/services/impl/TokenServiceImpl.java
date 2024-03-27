package com.sbm.sevenrooms.sevenroomshandler.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbm.sevenrooms.sevenroomshandler.services.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class TokenServiceImpl implements TokenService {

    private final Logger log = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Value(value = "${sevenroomsApi.graviteeUrl}")
    private String sevenRoomsUrl;

    @Value(value = "${sevenroomsApi.clientId}")
    private String clientId;

    @Value(value = "${sevenroomsApi.clientSecret}")
    private String clientSecret;

    @Value(value = "${sevenroomsApi.api-key}")
    private String graviteeApiKey;

    @Autowired
    RestTemplate restTemplate;

    public String generateToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // headers.set("X-Api-Key", graviteeApiKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        String authUrl = sevenRoomsUrl + "auth";

        log.debug("authurl : " + authUrl);

        try {
            ResponseEntity<String> response = restTemplate.exchange(authUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Token : " + extractTokenFromJson(response.getBody()));
                return extractTokenFromJson(response.getBody());
            } else return "";
        } catch (Exception e) {
            log.error(
                "Could not generate Token , This may be caused by bad credentials (clientId And clientSecret) Token will be Empty..."
            );
            return "";
        }
    }

    public String extractTokenFromJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse JSON string
            JsonNode rootNode = objectMapper.readTree(json);

            // Extract token from JSON
            JsonNode tokenNode = rootNode.path("data").path("token");
            String token = tokenNode.asText();
            log.info("Extracted Token successfully.");
            return token;
        } catch (Exception e) {
            log.error("Error occurred while extracting token from JSON", e);
            return null;
        }
    }
}
