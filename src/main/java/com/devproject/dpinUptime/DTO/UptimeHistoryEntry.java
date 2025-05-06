package com.devproject.dpinUptime.DTO;

import java.time.LocalDateTime;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class UptimeHistoryEntry {
    private final LocalDateTime timestamp;
    private final String status;
    private final long responseTime;

    public UptimeHistoryEntry() {
        this.timestamp = LocalDateTime.now();
        this.status = "UNKNOWN";
        this.responseTime = 0;
    }

    public UptimeHistoryEntry(LocalDateTime timestamp, String status, long responseTime) {
        this.timestamp = timestamp;
        this.status = status;
        this.responseTime = responseTime;
    }

    // Getters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public long getResponseTime() {
        return responseTime;
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