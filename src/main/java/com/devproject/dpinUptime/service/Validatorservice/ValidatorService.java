package com.devproject.dpinUptime.service.Validatorservice;

import com.devproject.dpinUptime.model.Validator;

public interface ValidatorService {
    Validator getOrCreateValidator(String publicKey, String ip);

    void addCredits(Long validatorId, long credits);
}