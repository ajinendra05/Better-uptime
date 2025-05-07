package com.devproject.dpinUptime.controller;

import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupRequest;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupResponse;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateResponse;
import com.devproject.dpinUptime.service.Validatorservice.SolanaServiceImpl;
import com.devproject.dpinUptime.service.Validatorservice.ValidatorService;
import com.devproject.dpinUptime.service.Validatorservice.WebsiteTickService;
import com.devproject.dpinUptime.service.UrlMonitoringService;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateRequest;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import com.devproject.dpinUptime.model.Validator; // Ensure this is the correct package for the Validator class
import com.devproject.dpinUptime.model.MonitoredUrl; // Ensure this is the correct package for the MonitoredUrl class
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.scheduling.annotation.Scheduled;

@Controller
public class HubController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ValidatorService validatorService;
    private final UrlMonitoringService websiteService;
    private final WebsiteTickService websiteTickService;
    private final SolanaServiceImpl solanaService;

    private final Map<String, Long> connectedValidators = new ConcurrentHashMap<>();
    private final Map<String, Consumer<ValidateResponse>> callbacks = new ConcurrentHashMap<>();

    @Autowired
    public HubController(
            SimpMessagingTemplate messagingTemplate,
            ValidatorService validatorService,
            UrlMonitoringService websiteService,
            WebsiteTickService websiteTickService,
            SolanaServiceImpl solanaService) {
        this.messagingTemplate = messagingTemplate;
        this.validatorService = validatorService;
        this.websiteService = websiteService;
        this.websiteTickService = websiteTickService;
        this.solanaService = solanaService;
    }

    @MessageMapping("/validator/login")
    public void handleSignup(SignupRequest request, StompHeaderAccessor headers) {
        if (solanaService.verifySignature(
                request.getMessage(),
                request.getPublicKey(),
                request.getSignature())) {
            Validator validator = validatorService.getOrCreateValidator(
                    request.getPublicKey(),
                    request.getIp());
            connectedValidators.put(headers.getSessionId(), validator.getId());

            messagingTemplate.convertAndSendToUser(
                    headers.getSessionId(),
                    "/queue/signup",
                    new SignupResponse(validator.getId(), request.getCallbackId()));
        }
    }

    @MessageMapping("/validate")
    public void handleValidation(ValidateResponse response) {
        if (callbacks.containsKey(response.getCallbackId())) {
            callbacks.get(response.getCallbackId()).accept(response);
            callbacks.remove(response.getCallbackId());
        }
    }

    @Scheduled(fixedRate = 180_000)
    public void distributeUrls() {
        ArrayList<MonitoredUrl> websites = new ArrayList<>(websiteService.getActiveUrl());
        ArrayList<Long> validatorIds = new ArrayList<>(connectedValidators.values());

        if (validatorIds.isEmpty() || websites.isEmpty())
            return;

        int batchSize = Math.max(websites.size() / validatorIds.size(), 1);

        for (int i = 0; i < validatorIds.size(); i++) {
            final Long validatorId = validatorIds.get(i);
            int fromIndex = i * batchSize;
            int toIndex = Math.min((i + 1) * batchSize, websites.size());

            websites.subList(fromIndex, toIndex).forEach(website -> {
                String callbackId = UUID.randomUUID().toString();

                callbacks.put(callbackId, response -> {
                    websiteTickService.saveTick(
                            website.getId(),
                            validatorId,
                            response.getStatus(),
                            response.getLatency());
                    validatorService.addCredits(validatorId, 100);
                });

                messagingTemplate.convertAndSendToUser(
                        validatorId.toString(),
                        "/queue/validate",
                        new ValidateRequest(
                                website.getUrl(),
                                callbackId,
                                website.getId()));
            });
        }
    }
}