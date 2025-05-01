package com.devproject.dpinUptime.service;
import com.devproject.dpinUptime.model.MonitoredUrl;
import com.devproject.dpinUptime.repository.UrlRepository;
import com.devproject.dpinUptime.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Service
public class UrlMonitoringService {
    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private AlertService alertService;

    // Check a single URL's status
    public boolean checkUrlStatus(String url) {
        try {
            ResponseEntity<String> response = new RestTemplate()
                .getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
        /*
         * int retries = 3;
    while (retries > 0) {
        try {
            ResponseEntity<String> response = new RestTemplate()
                .getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            retries--;
        }
    }
    return false;
         */
    }

    // Check all URLs (called by the scheduler)
    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void monitorAllUrls() {
        List<MonitoredUrl> urls = urlRepository.findAll();
        urls.forEach(url -> {
            boolean isUp = checkUrlStatus(url.getUrl());
            url.setUp(isUp);
            url.setLastChecked(LocalDateTime.now());
            urlRepository.save(url);
            
            if (!isUp) {
                alertService.triggerAlert(url); // Send email/Slack alert
            }
        });
    }

    // Get URLs for a specific user (for the dashboard)
    public List<MonitoredUrl> getUrlsForUser(String userEmail) {
        return urlRepository.findByUserEmail(userEmail);
    }
}