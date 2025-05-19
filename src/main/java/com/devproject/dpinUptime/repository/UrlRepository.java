package com.devproject.dpinUptime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devproject.dpinUptime.model.MonitoredUrl;
import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<MonitoredUrl, Long> {
    List<MonitoredUrl> findByUserEmail(String userEmail);

    // Optional<MonitoredUrl> findById(Long id);

    @Query("SELECT u FROM MonitoredUrl u LEFT JOIN FETCH u.uptimeHistory WHERE u.id = :id")
    Optional<MonitoredUrl> findByIdWithHistory(@Param("id") Long id);
}