package com.devproject.dpinUptime.DTO;

import java.util.List;
import com.devproject.dpinUptime.model.MonitoredUrl; // Ensure this class exists in the specified package

import com.devproject.dpinUptime.model.UrlStatus; // Adjust the package path if necessary

public class StatusFilter {
    private final UrlStatus status;
    private final String label;
    private int count;
    private List<MonitoredUrl> urls;

    public StatusFilter(UrlStatus status, String label) {
        this.status = status;
        this.label = label;
        this.count = 0;
    }

    public StatusFilter(UrlStatus status, String label, int count) {
        this.status = status;
        this.label = label;
        this.count = count;
    }

    public StatusFilter(UrlStatus status, String label, int count, List<MonitoredUrl> urls) {
        this.status = status;
        this.label = label;
        this.count = count;
        this.urls = urls;
    }

    // Used in the stream processing
    public StatusFilter withCount(int count) {
        this.count = count;
        return this;
    }

    public StatusFilter withUrls(List<MonitoredUrl> urls) {
        this.urls = urls;
        return this;
    }

    // Getters
    public UrlStatus getStatus() {
        return status;
    }

    public String getLabel() {
        return label;
    }

    public int getCount() {
        return count;
    }

    public List<MonitoredUrl> getUrls() {
        return urls;
    }
}