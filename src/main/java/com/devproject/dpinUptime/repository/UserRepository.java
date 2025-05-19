package com.devproject.dpinUptime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.devproject.dpinUptime.model.User;
import java.util.Optional;



public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    // boolean existsByUsername(String username);
}