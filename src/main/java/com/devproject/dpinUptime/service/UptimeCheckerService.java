// package com.devproject.dpinUptime.service;

// import java.time.LocalDateTime;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.web.client.RestTemplate;
// import org.springframework.stereotype.Service;
// import com.devproject.dpinUptime.repository.UrlRepository;
// import com.devproject.dpinUptime.model.MonitoredUrl;
// import com.devproject.dpinUptime.model.UrlStatus;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.web.client.RestClientException;
// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// public class UptimeCheckerService {
//     @Autowired
//     private UrlRepository urlRepository;
//     @Autowired
//     private AlertService alertService;
//     @Autowired
//     private RestTemplate restTemplate;

//     private static final Logger log = LoggerFactory.getLogger(UptimeCheckerService.class);

//     // @Scheduled(fixedRateString = "180000") // Every 3 minutes
//     // public void checkAllUrls() {
//     //     log.info("Starting uptime checks...");
//     //     List<MonitoredUrl> updatedUrls = urlRepository.findAll().stream()
//     //             .map(url -> {
//     //                 boolean isUp = checkUrl(url.getUrl());
//     //                 UrlStatus newStatus = isUp ? UrlStatus.UP : UrlStatus.DOWN;

//     //                 // Alert only on state change (UP → DOWN)
//     //                 if (url.getStatus() == UrlStatus.UP && newStatus == UrlStatus.DOWN) {
//     //                     alertService.triggerAlert(url);
//     //                 }

//     //                 url.setStatus(newStatus);
//     //                 url.setLastChecked(LocalDateTime.now());
//     //                 return url;
//     //             })
//     //             .collect(Collectors.toList());

//     //     urlRepository.saveAll(updatedUrls);
//     //     log.info("Completed checks for {} URLs", updatedUrls.size());
//     // }

//     // private boolean checkUrl(String url) {
//     //     try {
//     //         ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//     //         return response.getStatusCode().is2xxSuccessful();
//     //     } catch (RestClientException e) {
//     //         log.error("HTTP check failed for URL: {}", url, e);
//     //         return false;
//     //     }
//     // }
// }
// // @Service
// // public class UptimeCheckerService {
// // @Autowired
// // private UrlRepository urlRepository;
// // @Autowired
// // private AlertService alertService;

// // @Scheduled(fixedRate = 60000) // Every 60 seconds
// // public void checkAllUrls() {
// // urlRepository.findAll().forEach(url -> {
// // boolean isUp = checkUrl(url.getUrl()); // HTTP call
// // url.setUp(isUp);
// // url.setLastChecked(LocalDateTime.now());
// // urlRepository.save(url);
// // if (!isUp) alertService.triggerAlert(url);
// // });
// // }

// // private boolean checkUrl(String url) {
// // try {
// // ResponseEntity<String> response = new RestTemplate().getForEntity(url,
// // String.class);
// // return response.getStatusCode().is2xxSuccessful();
// // } catch (Exception e) {
// // return false;
// // }
// // }
// // }
