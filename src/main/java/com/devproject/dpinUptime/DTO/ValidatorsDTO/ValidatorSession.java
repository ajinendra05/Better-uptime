package com.devproject.dpinUptime.DTO.ValidatorsDTO;

public class ValidatorSession{
    Long id;
    String sessionId;
    String publicKey;

    public ValidatorSession(Long id, String sessionId, String publicKey) {
        this.id = id;
        this.sessionId = sessionId;
        this.publicKey = publicKey;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getPublicKey() {
        return publicKey;
    }
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
}

