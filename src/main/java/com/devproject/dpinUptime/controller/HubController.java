package com.devproject.dpinUptime.controller;

import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupRequest;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupResponse;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateResponse;
import com.devproject.dpinUptime.service.Validatorservice.SolanaServiceImpl;
import com.devproject.dpinUptime.service.Validatorservice.ValidatorServiceImpl;
import com.devproject.dpinUptime.service.Validatorservice.WebsiteTickServiceImpl;
import com.devproject.dpinUptime.service.UrlMonitoringService;
import org.springframework.stereotype.Controller;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import com.devproject.dpinUptime.model.Validator;
import com.devproject.dpinUptime.model.MonitoredUrl;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.scheduling.annotation.Scheduled;

import com.devproject.dpinUptime.model.Status;

import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidatorSession;

@Controller
public class HubController {
    private final Map<String, ValidatorSession> activeValidators = new ConcurrentHashMap<>();
    private final Map<String, Consumer<ValidateResponse>> callbacks = new ConcurrentHashMap<>();

    private final ValidatorServiceImpl validatorService;
    private final UrlMonitoringService websiteService;
    private final WebsiteTickServiceImpl websiteTickService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SolanaServiceImpl solanaService;

    @Autowired
    public HubController(ValidatorServiceImpl validatorService,
            UrlMonitoringService websiteService,
            WebsiteTickServiceImpl websiteTickService,
            SimpMessagingTemplate messagingTemplate,
            SolanaServiceImpl solanaService) {
        this.validatorService = validatorService;
        this.websiteService = websiteService;
        this.websiteTickService = websiteTickService;
        this.messagingTemplate = messagingTemplate;
        this.solanaService = solanaService;
    }

    @MessageMapping("/validator/login")
    public void handleSignup(SignupRequest request, StompHeaderAccessor headers) {
        try {
            String expectedMessage = String.format("Signed message for %s, %s",
                    request.getCallbackId(),
                    request.getPublicKey());

            if (!solanaService.verifySignature(expectedMessage,
                    request.getPublicKey(),
                    request.getSignature())) {
                sendError(headers.getSessionId(), "Invalid signature");
                return;
            }

            Validator validator = validatorService.getOrCreateValidator(
                    request.getPublicKey(),
                    request.getIp());

            ValidatorSession session = new ValidatorSession(
                    validator.getId(),
                    headers.getSessionId(),
                    request.getPublicKey());

            activeValidators.put(headers.getSessionId(), session);


            messagingTemplate.convertAndSendToUser(
                    session.getSessionId(),
                    "/queue/signup",
                    new SignupResponse(validator.getId(), request.getCallbackId()));

        } catch (Exception e) {
            sendError(headers.getSessionId(), "Authentication failed");
        }
    }

    @MessageMapping("/validate")
    public void handleValidation(ValidateResponse response) {
        try {
            // String expectedMessage = String.format("Replying to %s",
            // response.getCallbackId());

            if (callbacks.containsKey(response.getCallbackId())) {
                callbacks.get(response.getCallbackId()).accept(response);
                callbacks.remove(response.getCallbackId());
            }
        } catch (Exception e) {
            sendError(response.getCallbackId(), "Validation failed");
        }

    }

    @Scheduled(fixedRate = 60_000)
    public void distributeValidationTasks() {
        List<MonitoredUrl> websites = websiteService.getActiveUrl();
        List<ValidatorSession> validators = new ArrayList<>(activeValidators.values());

        if (websites.isEmpty() || validators.isEmpty())
            return;

        validators.forEach(validator -> {
            websites.forEach(website -> {

                String callbackId = UUID.randomUUID().toString();

                callbacks.put(callbackId, response -> {
                    if (!verifyResponseSignature(response, validator))
                        return;
                    if (response.getStatus() == Status.SUCCESS) {
                    }
                    else {
                    }

                    websiteTickService.saveTick(
                            website.getId(),
                            validator.getId(),
                            response.getStatus(),
                            response.getLatency());
                    validatorService.addCredits(validator.getId(), 100);
                });

                messagingTemplate.convertAndSendToUser(
                        validator.getSessionId(),
                        "/queue/validate",
                        new ValidateRequest(
                                website.getUrl(),
                                callbackId,
                                website.getId()));
            });
        });
    }

    private boolean verifyResponseSignature(ValidateResponse response, ValidatorSession validator) {
        String expectedMessage = String.format("Replying to %s", response.getCallbackId());
        return solanaService.verifySignature(
                expectedMessage,
                validator.getPublicKey(),
                response.getSignature());
    }

    private void sendError(String sessionId, String message) {
        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                Map.of("error", message));
    }
}
