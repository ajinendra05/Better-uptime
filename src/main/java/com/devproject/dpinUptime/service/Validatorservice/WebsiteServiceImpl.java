package com.devproject.dpinUptime.service.Validatorservice;
import com.devproject.dpinUptime.model.MonitoredUrl;
import com.devproject.dpinUptime.repository.WebsiteRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WebsiteServiceImpl  {
    private final WebsiteRepository websiteRepository;

    public WebsiteServiceImpl(WebsiteRepository websiteRepository) {
        this.websiteRepository = websiteRepository;
    }

    public List<MonitoredUrl> getActiveWebsites() {
        return websiteRepository.findByDisabledFalse();
    }
}