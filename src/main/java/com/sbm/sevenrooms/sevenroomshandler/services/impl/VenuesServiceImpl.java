package com.sbm.sevenrooms.sevenroomshandler.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbm.sevenrooms.sevenroomshandler.services.KafkaProducerService;
import com.sbm.sevenrooms.sevenroomshandler.services.VenuesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VenuesServiceImpl implements VenuesService {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    KafkaProducerService kafkaProducerService;

    @Value(value = "${spring.kafka.topics.venue-topic}")
    private String venuesTopic;

    @Value(value = "${spring.kafka.topics.handler-deadletters-topic}")
    private String gtwDeadLetterTopic;

    public JsonNode parseVenues(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode actualObj = mapper.readTree(json);

            logger.debug("JSON parsing successful for input: {}", json);

            JsonNode resultsField = actualObj.path("data").path("results");
            if (resultsField.isArray()) {
                for (JsonNode jsonNode : resultsField) {
                    String venueId = extractVenueId(jsonNode);

                    String topic = (venueId == null) ? gtwDeadLetterTopic : venuesTopic;

                    kafkaProducerService.sendToKafka(jsonNode, extractVenueId(jsonNode), topic);
                }
                return actualObj;
            }
            return resultsField;
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON: {}", e.getMessage());
            logger.debug("Failed JSON: {}", json);
            return null;
        }
    }

    public String extractVenueId(JsonNode jsonNode) {
        try {
            JsonNode idNode = jsonNode.get("id");
            if (idNode != null && idNode.isTextual()) {
                return idNode.asText();
            } else {
                return "";
            }
        } catch (Exception e) {
            logger.error("Could not extract Id from Venue", e.getMessage());
            return "";
        }
    }
}
