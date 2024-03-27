package com.sbm.sevenrooms.sevenroomshandler.services;

public interface TokenService {
    public String generateToken();

    public String extractTokenFromJson(String json);
}
