package com.devproject.dpinUptime.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import com.devproject.dpinUptime.repository.UrlRepository;
import com.devproject.dpinUptime.model.MonitoredUrl;

@Service
public class UptimeCheckerService  {
    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private AlertService alertService;

    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void checkAllUrls() {
        urlRepository.findAll().forEach(url -> {
            boolean isUp = checkUrl(url.getUrl()); // HTTP call
            url.setUp(isUp);
            url.setLastChecked(LocalDateTime.now());
            urlRepository.save(url);
            if (!isUp) alertService.triggerAlert(url);
        });
    }

    private boolean checkUrl(String url) {
        try {
            ResponseEntity<String> response = new RestTemplate().getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
