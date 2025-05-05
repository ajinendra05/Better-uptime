package com.devproject.dpinUptime.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.devproject.dpinUptime.model.MonitoredUrl;
import com.devproject.dpinUptime.model.UrlStatus;
import com.devproject.dpinUptime.service.UrlMonitoringService;
import org.springframework.stereotype.Controller;
import java.util.Collections;

import javax.management.monitor.Monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class DashboardController {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    // @Autowired
    private final UrlMonitoringService monitoringService;

    @Autowired
    public DashboardController(UrlMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        // List<Url> urls = monitoringService.getAllMonitoredUrls();

        // Map<String, Integer> stats = new HashMap<>();
        // stats.put("upCount", (int) urls.stream().filter(Url::isUp).count());
        // stats.put("downCount", urls.size() - stats.get("upCount"));

        // String email = principal.getName(); // Get logged-in user's email
        // model.addAttribute("user", email);
        // model.addAttribute("urls", monitoringService.getUrlsForUser(email));
        // return "dashboard";
        // Get URLs and calculate stats
        String email = principal.getName();
        log.info("Dashboard accessed by: {}", email);

        var urls = monitoringService.getUrlsForUser(email);
        if (urls == null) {
            urls = Collections.emptyList(); // Ensure never null
        }
        int upCount = (int) urls.stream().filter(url -> url.getStatus().isUp()).count();
        int downCount = urls.size() - upCount;

        // Add attributes for Thymeleaf template
        model.addAttribute("user", email);
        model.addAttribute("urls", urls);
        model.addAttribute("upCount", upCount);
        model.addAttribute("downCount", downCount);
        model.addAttribute("totalUrls", urls.size());

        return "dashboard";
    }

    @PostMapping("/monitors")
    public String createMonitor(
            @RequestParam String name,
            @RequestParam String url,
            Principal principal) {

        // Get logged-in user's email
        String userEmail = principal.getName();

        // Create new monitored URL
        MonitoredUrl newUrl = new MonitoredUrl();
        newUrl.setName(name);
        newUrl.setUrl(url);
        newUrl.setUserEmail(userEmail);
        newUrl.setStatus(UrlStatus.DOWN); // Initial status
        newUrl.setLastChecked(LocalDateTime.now());

        // Save through service (you might need to create this method)
        monitoringService.addUrl(newUrl);

        return "redirect:/dashboard";
    }
}
