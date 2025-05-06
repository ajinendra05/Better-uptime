package com.devproject.dpinUptime.repository;


import com.devproject.dpinUptime.model.Validator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ValidatorRepository extends JpaRepository<Validator, String> {
    Optional<Validator> findByPublicKey(String publicKey);
}