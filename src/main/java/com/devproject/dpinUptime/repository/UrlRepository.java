package com.devproject.dpinUptime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.devproject.dpinUptime.model.MonitoredUrl;
import java.util.List;

public interface UrlRepository extends JpaRepository<MonitoredUrl, Long> {
    List<MonitoredUrl> findByUserEmail(String userEmail);
}