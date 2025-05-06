package com.devproject.dpinUptime.DTO.ValidatorsDTO;

public class SignupRequest {
    private String message;
    private String publicKey;
    private String signature;
    private String ip;
    private String callbackId;

    // Getters, setters
    
        public SignupRequest(String message, String publicKey, String signature, String ip, String callbackId) {
            this.message = message;
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
        public String getMessage() {
            return message;
        }
    
        public void setMessage(String message) {
            this.message = message;
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