package com.devproject.dpinUptime.model;
import java.time.LocalDateTime;
import jakarta.persistence.Embeddable;
@Embeddable
public class UpTimeRecord {
    private LocalDateTime timestamp;
    private String status;
    private int responseTime;
    
    // Getters and setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }   
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public int getResponseTime() {
        return responseTime;
    }
    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }
}