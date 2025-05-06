package com.devproject.dpinUptime.DTO.ValidatorsDTO;

public class ValidateResponse {
    private String status;
    private int latency;
    private String callbackId;


    // Getters, setters, constructor
        public ValidateResponse(String status, int latency, String callbackId) {
            this.status = status;
            this.latency = latency;
            this.callbackId = callbackId;
        }
        public String getCallbackId() {
            return callbackId;
        }
        public void setCallbackId(String callbackId) {
            this.callbackId = callbackId;
        }
    
        
        public ValidateResponse(String status, int latency) {
            this.status = status;
            this.latency = latency;
        }
    
        public String getStatus() {
            return status;
        }
    
        public void setStatus(String status) {
            this.status = status;
        }
    
        public int getLatency() {
            return latency;
        }
    
        public void setLatency(int latency) {
            this.latency = latency;
        }
}