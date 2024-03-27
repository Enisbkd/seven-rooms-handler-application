package com.sbm.sevenrooms.sevenroomshandler.services;

import com.fasterxml.jackson.databind.JsonNode;

public interface KafkaProducerService {
    void sendToKafka(JsonNode json, String id, String topic);
}
