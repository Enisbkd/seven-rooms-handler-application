package com.sbm.sevenrooms.sevenroomshandler.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public interface ClientsAndReservationsService {
    public void iterateAndSendToTopic(String json) throws JsonProcessingException;

    public String extractId(JsonNode jsonNode);

    public String extractEntityType(JsonNode jsonNode);

    public JsonNode appendSearchFields(String json, String entity_type) throws JsonProcessingException;

    public String getCursor(String jsonData);

    public int getResultsSize(String jsonData);
}
