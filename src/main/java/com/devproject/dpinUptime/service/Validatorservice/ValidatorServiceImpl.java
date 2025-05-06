package com.devproject.dpinUptime.service.Validatorservice;


import com.devproject.dpinUptime.model.Validator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devproject.dpinUptime.repository.ValidatorRepository;

@Service
public class ValidatorServiceImpl implements ValidatorService {
    private final ValidatorRepository validatorRepository;

    public ValidatorServiceImpl(ValidatorRepository validatorRepository) {
        this.validatorRepository = validatorRepository;
    }

    @Override
    @Transactional
    public Validator getOrCreateValidator(String publicKey, String ip) {
        return validatorRepository.findByPublicKey(publicKey)
            .orElseGet(() -> validatorRepository.save(
                new Validator(publicKey, ip)
            ));
    }

    @Override
    @Transactional
    public void addCredits(Long validatorId, long credits) {
        validatorRepository.findById(validatorId.toString()).ifPresent(validator -> {
            validator.setPendingPayout(validator.getPendingPayout() + credits);
            validatorRepository.save(validator);
        });
    }
}