package com.sbm.sevenrooms.sevenroomshandler.services;

import com.fasterxml.jackson.databind.JsonNode;

public interface VenuesService {
    JsonNode parseVenues(String json);
}
