package com.devproject.dpinUptime.controller;

import java.security.Principal;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;
import com.devproject.dpinUptime.service.UrlMonitoringService;
// Adjusted package name if it is 'models' instead of 'model'
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
public class DashboardController {
    @Autowired
    private UrlMonitoringService monitoringService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        // List<Url> urls = monitoringService.getAllMonitoredUrls();

        // Map<String, Integer> stats = new HashMap<>();
        // stats.put("upCount", (int) urls.stream().filter(Url::isUp).count());
        // stats.put("downCount", urls.size() - stats.get("upCount"));

        String email = principal.getName(); // Get logged-in user's email
        model.addAttribute("user", email);
        model.addAttribute("urls", monitoringService.getUrlsForUser(email));
        return "dashboard";
    }
}
