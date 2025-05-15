package com.devproject.dpinUptime.service.Validatorservice;

import com.devproject.dpinUptime.model.Status;

public interface WebsiteTickService {
    void saveTick(Long websiteId, Long validatorId, Status status, int latency);
}