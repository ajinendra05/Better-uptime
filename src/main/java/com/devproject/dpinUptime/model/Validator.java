package com.devproject.dpinUptime.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Validator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String publicKey;
    private String location;
    private String ip;
    private Long pendingPayout;
    private final String role = "VALIDATOR";

    public String getRole() {
        return role;
    }
    public Validator() {
    }

    public Validator(String publicKey, String ip) {
        this.publicKey = publicKey;
        this.ip = ip;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Long getPendingPayout() {
        return pendingPayout;
    }

    public void setPendingPayout(Long pendingPayout) {
        this.pendingPayout = pendingPayout;
    }

}
