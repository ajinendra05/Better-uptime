package com.devproject.dpinUptime.DTO.ValidatorsDTO;
public class SignupResponse {
    private Long validatorId;
    private String callbackId;

    // Getters, setters, constructor
    public SignupResponse(Long validatorId, String callbackId) {
        this.validatorId = validatorId;
        this.callbackId = callbackId;
    }
   

    public Long getValidatorId() {
        return validatorId;
    }

    public void setValidatorId(Long validatorId) {
        this.validatorId = validatorId;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }
}