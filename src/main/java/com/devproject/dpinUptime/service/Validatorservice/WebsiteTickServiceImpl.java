package com.devproject.dpinUptime.service.Validatorservice;
import com.devproject.dpinUptime.model.WebsiteTick;
import com.devproject.dpinUptime.repository.WebsiteTickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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