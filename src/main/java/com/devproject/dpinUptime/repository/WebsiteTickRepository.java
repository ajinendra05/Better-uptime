package com.devproject.dpinUptime.repository;

import com.devproject.dpinUptime.model.WebsiteTick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebsiteTickRepository extends JpaRepository<WebsiteTick, String> {

}