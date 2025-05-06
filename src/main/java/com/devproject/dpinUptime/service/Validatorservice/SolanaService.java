package com.devproject.dpinUptime.service.Validatorservice;


public interface SolanaService {
    boolean verifySignature(String message, String publicKey, String signature);
}

