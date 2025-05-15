package com.devproject.dpinUptime.DTO.ValidatorsDTO;

import com.devproject.dpinUptime.model.Status;

public class ValidateResponse {
    private Status status;
    private int latency;
    private String callbackId;
    private String Signature;
    // private String publicKey;
    private String websiteID;

    // Getters, setters, constructor
    public ValidateResponse(Status status, int latency, String callbackId, String signature,
            String websiteID) {
        this.status = status;
        this.latency = latency;
        this.callbackId = callbackId;
        Signature = signature;
        // this.publicKey = publicKey;
        this.websiteID = websiteID;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public String getSignature() {
        return Signature;
    }

    public void setSignature(String signature) {
        Signature = signature;
    }

    // public String getpublicKey() {
    // return publicKey;
    // }

    // public void setpublicKey(String publicKey) {
    // this.publicKey = publicKey;
    // }

    public String getWebsiteID() {
        return websiteID;
    }

    public void setWebsiteID(String websiteID) {
        this.websiteID = websiteID;
    }

}