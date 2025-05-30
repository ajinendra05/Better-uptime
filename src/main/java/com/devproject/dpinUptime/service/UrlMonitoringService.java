package com.devproject.dpinUptime.service;

import com.devproject.dpinUptime.model.MonitoredUrl;
import com.devproject.dpinUptime.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import java.util.Optional;

@Service
public class UrlMonitoringService {

    @Autowired
    private UrlRepository urlRepository;
    // @Autowired
    // private AlertService alertService;
    // @Value("${monitoring.timeout}")
    // private int timeout;
    // @Value("${monitoring.interval}")
    // private long checkInterval;

    // @Value("${monitoring.retries}")
    // private int retries;

    // @Async
    // @Scheduled(fixedRateString = "1800000")
    // public void monitorAllUrls() {
    // log.info("Starting URL monitoring...");
    // List<MonitoredUrl> urls = urlRepository.findAll();
    // urls.forEach(url -> {
    // UrlStatus previousStatus = url.getStatus();
    // boolean isUp = checkUrlStatus(url);
    // url.setLastChecked(LocalDateTime.now());
    // urlRepository.save(url);

    // if (previousStatus == UrlStatus.UP && !isUp) {
    // alertService.triggerAlert(url);
    // }
    // });
    // }

    public void addUrl(MonitoredUrl url) {
        urlRepository.save(url);
    }

    // private boolean checkUrlStatus(MonitoredUrl url) {
    // try {
    // HttpURLConnection connection = (HttpURLConnection) new
    // URL(url.getUrl()).openConnection();
    // connection.setConnectTimeout(timeout);
    // connection.setReadTimeout(timeout);
    // connection.setInstanceFollowRedirects(false);

    // int responseCode = connection.getResponseCode();
    // boolean isUp = (responseCode >= 200 && responseCode < 300);
    // url.setStatus(isUp ? UrlStatus.UP : UrlStatus.DOWN);
    // url.setResponseTime(measureResponseTime(connection));
    // return isUp;
    // } catch (IOException e) {
    // url.setStatus(UrlStatus.DOWN);
    // return false;
    // }
    // }

    // private long measureResponseTime(HttpURLConnection connection) throws
    // IOException {
    // long start = System.currentTimeMillis();
    // connection.connect();
    // return System.currentTimeMillis() - start;
    // }

    public List<MonitoredUrl> getUrlsForUser(String userEmail) {
        return urlRepository.findByUserEmail(userEmail); // Fixed method name
    }

    public List<MonitoredUrl> getActiveUrl() {
        return urlRepository.findAll(); // Added method to get all URLs
    }

    public void deleteUrl(Long id, String userEmail) {
        // Ensure user owns the URL before deleting
        Optional<MonitoredUrl> url = urlRepository.findByIdAndUserEmail(id, userEmail);
        url.ifPresent(urlRepository::delete);
    }

}
// @Service
// public class UrlMonitoringService {
// private static final int TIMEOUT = 5000;
// @Autowired
// private UrlRepository urlRepository;
// @Autowired
// private AlertService alertService;

// // Check a single URL's status
// public boolean checkUrlStatus(MonitoredUrl url) {
// try {
// HttpURLConnection connection = (HttpURLConnection) new
// URL(url.getUrl()).openConnection();
// connection.setConnectTimeout(TIMEOUT);
// int responseCode = connection.getResponseCode();
// url.setStatus((responseCode >= 200 && responseCode < 300) ? UrlStatus.UP :
// UrlStatus.DOWN);
// connection.setReadTimeout(TIMEOUT);
// url.setResponseTime(measureResponseTime(connection)); // Measure response
// time
// url.setResponseTime(measureResponseTime(connection));
// url.setLastChecked(LocalDateTime.now());
// url.setUp(connection.getResponseCode() == HttpURLConnection.HTTP_OK);

// } catch (SocketTimeoutException e) {
// url.setStatus(UrlStatus.DOWN);
// url.setUp(false);
// url.setLastChecked(LocalDateTime.now());
// return url.isUp();
// } catch (IOException e) {
// url.setUp(false);
// url.setLastChecked(LocalDateTime.now());
// return false;
// }
// url.setLastChecked(LocalDateTime.now());
// urlRepository.save(url);
// return false;
// // ResponseEntity<String> response = new RestTemplate()
// // .getForEntity(url, String.class);
// // return response.getStatusCode().is2xxSuccessful();
// // } catch (Exception e) {
// // return false;
// // }
// /*
// * int retries = 3;
// * while (retries > 0) {
// * try {
// * ResponseEntity<String> response = new RestTemplate()
// * .getForEntity(url, String.class);
// * return response.getStatusCode().is2xxSuccessful();
// * } catch (Exception e) {
// * retries--;
// * }
// * }
// * return false;
// */
// }

// // Measure the response time of a URL
// private long measureResponseTime(HttpURLConnection connection) throws
// IOException {
// long startTime = System.currentTimeMillis();
// connection.connect();
// long endTime = System.currentTimeMillis();
// return endTime - startTime;
// }

// // Check all URLs (called by the scheduler)
// // Check all URLs (called by the scheduler)
// @Scheduled(fixedRate = 60000) // Runs every 60 seconds
// public void monitorAllUrls() {
// List<MonitoredUrl> urls = urlRepository.findAll();
// urls.forEach(url -> {
// boolean isUp = checkUrlStatus(url);
// url.setUp(isUp);
// url.setLastChecked(LocalDateTime.now());
// urlRepository.save(url);

// if (url.getStatus() == UrlStatus.UP && !isUp) {
// url.setStatus(UrlStatus.DOWN);
// alertService.triggerAlert(url); // Send email/Slack alert
// }
// });
// }

// // Get URLs for a specific user (for the dashboard)
// public List<MonitoredUrl> getUrlsForUser(String userEmail) {
// return urlRepository.findByUserEmail(userEmail);
// }
// }