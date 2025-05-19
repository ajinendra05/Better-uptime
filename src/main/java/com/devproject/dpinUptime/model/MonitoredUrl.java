package com.devproject.dpinUptime.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.devproject.dpinUptime.DTO.UptimeHistoryEntry;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;

@Entity
public class MonitoredUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String userEmail;
    private String url;
    private boolean isUp;
    private LocalDateTime lastChecked;
    private long responseTime;
    @Enumerated(EnumType.STRING)
    private UrlStatus status;
    @Column(nullable = false)
    private boolean disabled = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "uptime_history", joinColumns = @JoinColumn(name = "url_id"))
    @OrderColumn(name = "entry_order")
    public List<UptimeHistoryEntry> uptimeHistory = new ArrayList<>();

    public void addHistoryEntry(UptimeHistoryEntry entry) {
        if (uptimeHistory.size() >= 20) {
            uptimeHistory.remove(0);
        }
        uptimeHistory.add(entry);
    }

    public List<UptimeHistoryEntry> getUptimeHistory() {
        return uptimeHistory;
    }

    public MonitoredUrl() {
        // JPA requires this
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isUp() {
        return isUp;
    }

    public void setUp(boolean isUp) {
        this.isUp = isUp;
    }

    public LocalDateTime getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }

    public UrlStatus getStatus() {
        return status;
    }

    public void setStatus(UrlStatus status) {
        this.status = status;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

}
