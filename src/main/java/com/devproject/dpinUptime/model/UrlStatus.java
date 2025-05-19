package com.devproject.dpinUptime.model;
// this status is used to show the status of the url in the frontend
// it is not the same as the status of the website tick
public enum UrlStatus {
    ALL("All Services"),
    UP("Online Services"),
    SLOW("Degraded Services"),
    DOWN("Offline Services");
    private final String displayName;

    UrlStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isUp() {
        return this == UP;
    }
    

}