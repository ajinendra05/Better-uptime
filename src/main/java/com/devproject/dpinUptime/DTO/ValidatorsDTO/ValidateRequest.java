package com.devproject.dpinUptime.DTO.ValidatorsDTO;

public class ValidateRequest {
    private String url;
    private String callbackId;
    private Long websiteId;

    // Getters, setters, constructor
 
    public ValidateRequest(String url, String callbackId, Long id) {
        this.url = url;
        this.callbackId = callbackId;
        this.websiteId = id;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public Long getWebsiteId() {
        return websiteId;
    }
}
