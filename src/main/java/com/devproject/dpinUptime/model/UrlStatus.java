package com.devproject.dpinUptime.model;

public enum UrlStatus {
    UP,
    DOWN,
    PENDING; // Optional state for checks in progress

    public boolean isUp() {
        return this == UP;
    }
}