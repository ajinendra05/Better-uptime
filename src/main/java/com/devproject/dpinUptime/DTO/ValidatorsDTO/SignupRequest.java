package com.devproject.dpinUptime.DTO.ValidatorsDTO;

public class SignupRequest {
    private String publicKey;
    private String signature;
    private String ip;
    private String callbackId;

    // Getters, setters

    public SignupRequest(String publicKey, String signature, String ip, String callbackId) {
        this.publicKey = publicKey;
        this.signature = signature;
        this.ip = ip;
        this.callbackId = callbackId;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }
 

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getIp() {
        return ip;
    }
}