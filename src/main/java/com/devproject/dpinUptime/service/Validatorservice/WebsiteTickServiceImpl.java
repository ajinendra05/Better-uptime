package com.devproject.dpinUptime.service.Validatorservice;

import com.devproject.dpinUptime.model.WebsiteTick;
import com.devproject.dpinUptime.repository.WebsiteTickRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devproject.dpinUptime.DTO.UptimeHistoryEntry;
import com.devproject.dpinUptime.model.MonitoredUrl;
import com.devproject.dpinUptime.model.Status;
import com.devproject.dpinUptime.model.UrlStatus;

import java.time.LocalDateTime;
import com.devproject.dpinUptime.repository.UrlRepository;
import jakarta.transaction.Transactional;
@Service
@Transactional
public class WebsiteTickServiceImpl implements WebsiteTickService {

    private static final Logger logger = LoggerFactory.getLogger(WebsiteTickServiceImpl.class);

    private final WebsiteTickRepository websiteTickRepository;
    private final UrlRepository monitoredUrlRepository;

    @Autowired
    public WebsiteTickServiceImpl(WebsiteTickRepository websiteTickRepository, UrlRepository monitoredUrlRepository) {
        this.websiteTickRepository = websiteTickRepository;
        this.monitoredUrlRepository = monitoredUrlRepository;
    }

    @Override
    @Transactional
    public void saveTick(Long websiteId, Long validatorId, Status status, int latency) {
        WebsiteTick tick = new WebsiteTick();
        tick.setWebsiteId(websiteId);
        tick.setValidatorId(validatorId);
        tick.setStatus(status);
        tick.setLatency(latency);
        tick.setCreatedAt(LocalDateTime.now());
        websiteTickRepository.save(tick);

        // Update the monitored URL status and history
        logger.info("Saving tick: {}", tick);
        logger.info("Website ID: {}", websiteId);
        MonitoredUrl url = monitoredUrlRepository.findByIdWithHistory(websiteId)
                .orElseThrow(() -> new RuntimeException("Website not found"));

        logger.info("Website tick saved: {}", tick);
        logger.info("Website tick saved: {}", tick.getStatus());
        logger.info("Website to update: {}", url);
        logger.info("Website to update: {}", url.getStatus());
        // logger.info("Website to update: {}", url.getUptimeHistory());
        // logger.info("Website to update: {}", url.getUptimeHistory().size());
        logger.info("tick status: {}", tick.getStatus());
        // logger.info("tick status: {}", tick.getStatus().name());

        UrlStatus urlStatus = status == Status.SUCCESS ? UrlStatus.UP : UrlStatus.DOWN;
        logger.info("testing log 1");
        UptimeHistoryEntry historyEntry = new UptimeHistoryEntry(status, latency);
        // historyEntry.setTimestamp(LocalDateTime.now());
        // historyEntry.setStatus(urlStatus);
        // historyEntry.setResponseTime(latency);
        logger.info("testing log 2");

        url.addHistoryEntry(historyEntry);
        url.setStatus(urlStatus);
        url.setLastChecked(LocalDateTime.now());
        url.setResponseTime(latency);
        logger.info("testing log 3");
        monitoredUrlRepository.save(url);
        logger.info(" updated website comletttttttttttttttt: {}", url);

    }
}