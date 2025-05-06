package com.devproject.dpinUptime.service.Validatorservice;
import com.devproject.dpinUptime.model.WebsiteTick;
import com.devproject.dpinUptime.repository.WebsiteTickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;



@Service
public class WebsiteTickServiceImpl implements WebsiteTickService {
    
    private final WebsiteTickRepository websiteTickRepository;

    @Autowired
    public WebsiteTickServiceImpl(WebsiteTickRepository websiteTickRepository) {
        this.websiteTickRepository = websiteTickRepository;
    }

    @Override
    public void saveTick(Long websiteId, Long validatorId, String status, int latency) {
        WebsiteTick tick = new WebsiteTick();
        tick.setWebsiteId(websiteId);
        tick.setValidatorId(validatorId);
        tick.setStatus(status);
        tick.setLatency(latency);
        tick.setCreatedAt(LocalDateTime.now());
        websiteTickRepository.save(tick);
    }
}