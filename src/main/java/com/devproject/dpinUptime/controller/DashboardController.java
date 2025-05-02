package com.devproject.dpinUptime.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;
import com.devproject.dpinUptime.service.UrlMonitoringService;
import org.springframework.stereotype.Controller;

@Controller
public class DashboardController {
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
        var urls = monitoringService.getUrlsForUser(email);
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
}
