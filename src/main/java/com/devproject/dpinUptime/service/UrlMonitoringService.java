package com.devproject.dpinUptime.service;

import com.devproject.dpinUptime.model.MonitoredUrl;
import com.devproject.dpinUptime.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.List;
import java.net.URL;
import java.io.IOException;

@Service
public class UrlMonitoringService {
    private static final int TIMEOUT = 5000;
    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private AlertService alertService;

    // Check a single URL's status
    public boolean checkUrlStatus(MonitoredUrl url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url.getUrl()).openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            url.setResponseTime(measureResponseTime(connection)); // Measure response time
            url.setResponseTime(measureResponseTime(connection));
            url.setLastChecked(LocalDateTime.now());
            url.setUp(connection.getResponseCode() == HttpURLConnection.HTTP_OK);

        } catch (SocketTimeoutException e) {
            url.setUp(false);
            url.setLastChecked(LocalDateTime.now());
            return url.isUp();
        } catch (IOException e) {
            url.setUp(false);
            url.setLastChecked(LocalDateTime.now());
            return false;
        }
        return false;
        // ResponseEntity<String> response = new RestTemplate()
        // .getForEntity(url, String.class);
        // return response.getStatusCode().is2xxSuccessful();
        // } catch (Exception e) {
        // return false;
        // }
        /*
         * int retries = 3;
         * while (retries > 0) {
         * try {
         * ResponseEntity<String> response = new RestTemplate()
         * .getForEntity(url, String.class);
         * return response.getStatusCode().is2xxSuccessful();
         * } catch (Exception e) {
         * retries--;
         * }
         * }
         * return false;
         */
    }

    // Measure the response time of a URL
    private long measureResponseTime(HttpURLConnection connection) throws IOException {
        long startTime = System.currentTimeMillis();
        connection.connect();
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    // Check all URLs (called by the scheduler)
    // Check all URLs (called by the scheduler)
    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void monitorAllUrls() {
        List<MonitoredUrl> urls = urlRepository.findAll();
        urls.forEach(url -> {
            boolean isUp = checkUrlStatus(url);
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