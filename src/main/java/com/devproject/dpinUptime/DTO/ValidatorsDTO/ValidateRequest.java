package com.devproject.dpinUptime.DTO.ValidatorsDTO;

public class ValidateRequest {
    private String url;
    private String callbackId;
    private Long websiteId;

    // Getters, setters, constructor
 
    // Constructor to match the instantiation in HubController
    public ValidateRequest(String url, String callbackId, Long id) {
        this.url = url;
        this.callbackId = callbackId;
        this.websiteId = id;
    }
    // public ValidateRequest(String url, String callbackId, Long websiteId) {
    // this.url = url;
    // this.callbackId = callbackId;
    // this.websiteId = websiteId;
    // }

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
