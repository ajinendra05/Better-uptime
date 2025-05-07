package com.devproject.dpinUptime.controller;

import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupRequest;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupResponse;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateResponse;
import com.devproject.dpinUptime.service.Validatorservice.SolanaServiceImpl;
import com.devproject.dpinUptime.service.Validatorservice.ValidatorService;
import com.devproject.dpinUptime.service.Validatorservice.WebsiteTickService;
import com.devproject.dpinUptime.service.UrlMonitoringService;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateRequest;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;
import java.util.function.Consumer;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import com.devproject.dpinUptime.model.Validator;
import com.devproject.dpinUptime.model.MonitoredUrl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.scheduling.annotation.Scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class HubController {

    private static final Logger logger = LoggerFactory.getLogger(HubController.class);
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

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
        log.info("Received signup request from: " + request.getPublicKey());
        log.info("Session ID: " + headers.getSessionId());
        log.info(null != request.getIp() ? "IP: " + request.getIp() : "IP: null");
        log.info("Message: " + request.getMessage());
        try {
            // Verify signature format first
            if (!isValidSolanaSignature(request.getSignature())) {
                sendError(headers.getSessionId(), "Invalid signature format");
                return;
            }

            // Verify cryptographic signature
            boolean isValid = solanaService.verifySignature(
                    request.getMessage(),
                    request.getPublicKey(),
                    request.getSignature());

            if (isValid) {
                Validator validator = validatorService.getOrCreateValidator(
                        request.getPublicKey(),
                        request.getIp());

                // Store session with expiration
                connectedValidators.put(headers.getSessionId(), validator.getId());

                // Send confirmation
                messagingTemplate.convertAndSendToUser(
                        headers.getSessionId(),
                        "/queue/signup",
                        new SignupResponse(validator.getId(), request.getCallbackId()));
            } else {
                sendError(headers.getSessionId(), "Signature verification failed");
            }
        } catch (Exception e) {
            sendError(headers.getSessionId(), "Authentication error: " + e.getMessage());
        }
    }

    private void sendError(String sessionId, String message) {
        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                Map.of("error", message));
    }

    private boolean isValidSolanaSignature(String signature) {
        try {
            byte[] sigBytes = Base64.getDecoder().decode(signature);
            return sigBytes.length == 64; // Ed25519 signature length
        } catch (Exception e) {
            return false;
        }
    }

    @MessageMapping("/validate")
    public void handleValidation(ValidateResponse response, StompHeaderAccessor headers) {
        String sessionId = headers.getSessionId();
        if (!connectedValidators.containsKey(sessionId)) {
            sendError(sessionId, "Unauthorized validation attempt");
            return;
        }
        if (callbacks.containsKey(response.getCallbackId())) {
            callbacks.get(response.getCallbackId()).accept(response);
            callbacks.remove(response.getCallbackId());
        }
    }

    // @Scheduled(fixedRate = 180_000)
    // public void distributeUrls() {
    // ArrayList<MonitoredUrl> websites = new
    // ArrayList<>(websiteService.getActiveUrl());
    // ArrayList<Long> validatorIds = new ArrayList<>(connectedValidators.values());

    // if (validatorIds.isEmpty() || websites.isEmpty())
    // return;

    // int batchSize = Math.max(websites.size() / validatorIds.size(), 1);

    // for (int i = 0; i < validatorIds.size(); i++) {
    // final Long validatorId = validatorIds.get(i);
    // int fromIndex = i * batchSize;
    // int toIndex = Math.min((i + 1) * batchSize, websites.size());

    // websites.subList(fromIndex, toIndex).forEach(website -> {
    // String callbackId = UUID.randomUUID().toString();

    // callbacks.put(callbackId, response -> {
    // websiteTickService.saveTick(
    // website.getId(),
    // validatorId,
    // response.getStatus(),
    // response.getLatency());
    // validatorService.addCredits(validatorId, 100);
    // });

    // messagingTemplate.convertAndSendToUser(
    // validatorId.toString(),
    // "/queue/validate",
    // new ValidateRequest(
    // website.getUrl(),
    // callbackId,
    // website.getId()));
    // });
    // }
    // }
    @Scheduled(fixedRate = 180_000)
    public void distributeUrls() {
        ArrayList<MonitoredUrl> websites = new ArrayList<>(websiteService.getActiveUrl());

        // Get all active session IDs
        ArrayList<String> sessionIds = new ArrayList<>(connectedValidators.keySet());

        if (sessionIds.isEmpty() || websites.isEmpty())
            return;

        int batchSize = Math.max(websites.size() / sessionIds.size(), 1);

        for (int i = 0; i < sessionIds.size(); i++) {
            String sessionId = sessionIds.get(i);
            Long validatorId = connectedValidators.get(sessionId);

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

                // Send directly to session ID
                messagingTemplate.convertAndSendToUser(
                        sessionId,
                        "/queue/validate",
                        new ValidateRequest(
                                website.getUrl(),
                                callbackId,
                                website.getId()));
            });
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        connectedValidators.remove(sessionId);
    }
}