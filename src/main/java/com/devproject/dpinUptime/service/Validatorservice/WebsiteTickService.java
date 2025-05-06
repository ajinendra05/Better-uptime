package com.devproject.dpinUptime.service.Validatorservice;

public interface WebsiteTickService {
    void saveTick(Long websiteId, Long validatorId, String status, int latency);
}