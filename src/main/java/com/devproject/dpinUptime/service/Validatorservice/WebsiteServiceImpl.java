// package com.devproject.dpinUptime.service.Validatorservice;
// import com.devproject.dpinUptime.model.MonitoredUrl;
// import com.devproject.dpinUptime.repository.WebsiteRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
// import org.springframework.stereotype.Service;
// import java.util.List;

// @Service
// public class WebsiteServiceImpl implements UrlMonitoringService {
//     private final WebsiteRepository websiteRepository;

//     public WebsiteServiceImpl(WebsiteRepository websiteRepository) {
//         this.websiteRepository = websiteRepository;
//     }

//     @Override
//     public List<MonitoredUrl> getActiveWebsites() {
//         return websiteRepository.findByDisabledFalse();
//     }
// }