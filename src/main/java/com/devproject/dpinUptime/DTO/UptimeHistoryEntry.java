package com.devproject.dpinUptime.DTO;

import java.time.LocalDateTime;
import java.util.Objects;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import com.devproject.dpinUptime.model.Status;

@Embeddable
public class UptimeHistoryEntry {
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private Status status;
    private long responseTime;

    public UptimeHistoryEntry() {
    }

    public UptimeHistoryEntry(Status status, long responseTime) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.responseTime = responseTime;
    }

    public UptimeHistoryEntry(LocalDateTime timestamp, Status status, long responseTime) {
        this.timestamp = timestamp;
        this.status = status;
        this.responseTime = responseTime;
    }

    // Getters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public long getResponseTime() {
        return responseTime;
    }
    // Setters
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    // equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UptimeHistoryEntry that = (UptimeHistoryEntry) o;
        return responseTime == that.responseTime &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, status, responseTime);
    }

    // toString()
    @Override
    public String toString() {
        return "UptimeHistoryEntry{" +
                "timestamp=" + timestamp +
                ", status='" + status + '\'' +
                ", responseTime=" + responseTime +
                '}';
    }
}