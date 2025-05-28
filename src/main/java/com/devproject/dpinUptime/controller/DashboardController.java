package com.devproject.dpinUptime.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import com.devproject.dpinUptime.DTO.StatusFilter;
import com.devproject.dpinUptime.DTO.StatusStat;
import com.devproject.dpinUptime.model.MonitoredUrl;
import com.devproject.dpinUptime.model.UrlStatus;
import com.devproject.dpinUptime.service.UrlMonitoringService;
import org.springframework.stereotype.Controller;
import java.util.Collections;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
        private final Map<String, StatusFilter> FILTERS = Map.of(
                        // "all", new StatusFilter(UrlStatus.values(), "All Services"),
                        "online1", new StatusFilter(UrlStatus.UP, "Online"),
                        "online", new StatusFilter(UrlStatus.UP, "Online"),

                        "degraded", new StatusFilter(UrlStatus.SLOW, "Degraded"),
                        "offline", new StatusFilter(UrlStatus.DOWN, "Offline"));
        // @Autowired
        private final UrlMonitoringService monitoringService;

        @Autowired
        public DashboardController(UrlMonitoringService monitoringService) {
                this.monitoringService = monitoringService;
        }

        @GetMapping("/dashboard")
        public String dashboard(@RequestParam(required = false) String filter, Model model, Principal principal) {

                String email = principal.getName();

                var urls = monitoringService.getUrlsForUser(email);
                if (urls == null) {
                        urls = Collections.emptyList(); // Ensure never null
                }

                model.addAttribute("user", email);
                model.addAttribute("urls", urls);
                model.addAttribute("statusStats", calculateStatusStats(urls));
                model.addAttribute("filters", createFiltersWithCounts(urls));
                model.addAttribute("currentFilter", filter != null ? filter : "ALL");
                return "dashboard";
        }

        private List<StatusStat> calculateStatusStats(List<MonitoredUrl> urls) {
                return List.of(
                                new StatusStat(UrlStatus.ALL, "All Services",
                                                urls.size()),
                                new StatusStat(UrlStatus.UP, "Online Services",
                                                urls.stream().filter(u -> u.getStatus() == UrlStatus.UP).count()),
                                new StatusStat(UrlStatus.DOWN, "Offline Services",
                                                urls.stream().filter(u -> u.getStatus() == UrlStatus.DOWN).count()));
        }

        private List<StatusFilter> createFiltersWithCounts(List<MonitoredUrl> urls) {
                return Arrays.asList(
                                new StatusFilter(UrlStatus.ALL, "All Services", urls.size(), urls),
                                new StatusFilter(UrlStatus.UP, "Online",
                                                (int) urls.stream().filter(u -> u.getStatus() == UrlStatus.UP).count(),
                                                urls.stream().filter(u -> u.getStatus() == UrlStatus.UP)
                                                                .collect(Collectors.toList())),
                                new StatusFilter(UrlStatus.DOWN, "Offline",
                                                (int) urls.stream().filter(u -> u.getStatus() == UrlStatus.DOWN)
                                                                .count(),
                                                urls.stream().filter(u -> u.getStatus() == UrlStatus.DOWN)
                                                                .collect(Collectors.toList())));

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
