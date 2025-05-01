package com.devproject.dpinUptime.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.Model;
import com.devproject.dpinUptime.service.UrlMonitoringService;


@RestController
public class DashboardController {
    @Autowired
    private UrlMonitoringService monitoringService;

      @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String email = principal.getName(); // Get logged-in user's email
        model.addAttribute("user", email);
        model.addAttribute("urls", monitoringService.getUrlsForUser(email));
        return "dashboard";
    }
}
