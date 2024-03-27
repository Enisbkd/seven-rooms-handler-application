package com.sbm.sevenrooms.sevenroomshandler.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sbm.sevenrooms.sevenroomshandler.services.ClientsAndReservationsService;
import com.sbm.sevenrooms.sevenroomshandler.services.KafkaProducerService;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClientsAndReservationsServiceImpl implements ClientsAndReservationsService {

    private final Logger log = LoggerFactory.getLogger(ClientsAndReservationsServiceImpl.class);

    @Value(value = "${spring.kafka.topics.client-topic}")
    private String clientTopic;

    @Value(value = "${spring.kafka.topics.reservation-topic}")
    private String reservationTopic;

    @Value(value = "${spring.kafka.topics.handler-deadletters-topic}")
    private String gtwDeadLetterTopic;

    List<String> acceptedTypes = Arrays.asList("client", "reservation");

    @Autowired
    KafkaProducerService kafkaProducerService;

    public void iterateAndSendToTopic(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(json);

        if (actualObj.isArray()) {
            for (JsonNode jsonNode : actualObj) {
                checkAndSendToKafkaTopic(jsonNode);
            }
        }
    }

    public JsonNode appendSearchFields(String json, String entity_type) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(json);
            JsonNode resultsField = actualObj.path("data").path("results");

            if (resultsField.isArray()) {
                for (JsonNode jsonNode : resultsField) {
                    ObjectNode entityNode = mapper.createObjectNode();
                    entityNode.set("entity", jsonNode);
                    entityNode.put("entity_type", entity_type);
                    entityNode.put("event_type", "created");
                    entityNode.put("techSource", "SevenRoomsHandler Search Endpoint");
                    checkAndSendToKafkaTopic(entityNode);
                }
                return actualObj;
            }
            return resultsField;
        } catch (JsonProcessingException e) {
            log.error("Error processing JSON: " + e.getMessage());
            return null;
        }
    }

    private void checkAndSendToKafkaTopic(JsonNode jsonNode) {
        String id = extractId(jsonNode);
        String entityType = extractEntityType(jsonNode);
        String topic = "";

        if (id.isEmpty() || entityType.isEmpty() || !acceptedTypes.contains(entityType)) {
            topic = gtwDeadLetterTopic;
        } else if (entityType.equalsIgnoreCase("reservation")) {
            topic = reservationTopic;
        } else if (entityType.equalsIgnoreCase("client")) {
            topic = clientTopic;
        } else {
            topic = gtwDeadLetterTopic;
        }
        log.debug("Sending message : " + jsonNode.toString() + " with key: " + id + "to Topic " + topic);

        kafkaProducerService.sendToKafka(jsonNode, id, topic);
    }

    public String extractId(JsonNode jsonNode) {
        String id = "";
        if (jsonNode.has("entity")) {
            JsonNode entityNode = jsonNode.get("entity");
            if (entityNode.has("id")) {
                id = entityNode.get("id").toString().replace("\"", "");
            }
        }
        return id;
    }

    public String extractEntityType(JsonNode jsonNode) {
        String entityType = "";
        if (jsonNode.has("entity_type")) {
            entityType = jsonNode.get("entity_type").toString().replace("\"", "");
        }
        return entityType;
    }

    public int getResultsSize(String jsonData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonData);
            JsonNode resultsNode = jsonNode.at("/data/results");
            return resultsNode.size();
        } catch (Exception e) {
            log.error("Error while extracting resultsSize : ", e);
        }
        return -1;
    }

    public String getCursor(String jsonData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonData);
            JsonNode cursorNode = jsonNode.at("/data/cursor");
            return cursorNode.asText();
        } catch (Exception e) {
            log.error("Error while extracting cursor : ", e);
        }
        return null;
    }
}
