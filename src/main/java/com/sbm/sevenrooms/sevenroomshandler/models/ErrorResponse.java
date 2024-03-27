package com.sbm.sevenrooms.sevenroomshandler.models;

public class ErrorResponse {

    private int statusCode;
    private String reasonPhrase;
    private String message;

    public ErrorResponse(int statusCode, String reasonPhrase, String message) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
