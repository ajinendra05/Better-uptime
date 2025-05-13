// package com.devproject.dpinUptime.controller;

// import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupRequest;
// import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupResponse;
// import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateResponse;
// import com.devproject.dpinUptime.service.Validatorservice.SolanaServiceImpl;
// import com.devproject.dpinUptime.service.Validatorservice.ValidatorService;
// import com.devproject.dpinUptime.service.Validatorservice.WebsiteTickService;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.JsonNode;
// import com.devproject.dpinUptime.service.UrlMonitoringService;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.socket.handler.TextWebSocketHandler;
// import org.springframework.web.socket.TextMessage;
// import org.springframework.web.socket.WebSocketSession;
// import org.springframework.web.socket.messaging.SessionDisconnectEvent;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
// import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateRequest;

// import java.io.IOException;
// import java.security.Principal;
// import java.util.ArrayList;
// import java.util.Map;
// import java.util.UUID;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.Base64;
// import java.util.function.Consumer;
// import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
// import com.devproject.dpinUptime.model.Validator;
// import com.devproject.dpinUptime.model.MonitoredUrl;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.event.EventListener;
// import org.springframework.messaging.handler.annotation.MessageMapping;
// import org.springframework.scheduling.annotation.Scheduled;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ObjectNode;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.bind.annotation.RequestBody;
// import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidatorSession;

// @Controller
// public class HubController {
    
//     private final Map<String, ValidatorSession> activeValidators = new ConcurrentHashMap<>();
//     private final Map<String, Consumer<ValidationResult>> callbacks = new ConcurrentHashMap<>();
    
//     private final ValidatorRepository validatorRepository;
//     private final WebsiteRepository websiteRepository;
//     private final WebsiteTickRepository websiteTickRepository;
//     private final SimpMessagingTemplate messagingTemplate;
//     private final SolanaService solanaService;

//     @Autowired
//     public HubController(ValidatorRepository validatorRepository,
//                         WebsiteRepository websiteRepository,
//                         WebsiteTickRepository websiteTickRepository,
//                         SimpMessagingTemplate messagingTemplate,
//                         SolanaService solanaService) {
//         this.validatorRepository = validatorRepository;
//         this.websiteRepository = websiteRepository;
//         this.websiteTickRepository = websiteTickRepository;
//         this.messagingTemplate = messagingTemplate;
//         this.solanaService = solanaService;
//     }

//     @MessageMapping("/signup")
//     public void handleSignup(SignupRequest request, StompHeaderAccessor headers) {
//         String message = String.format("Signed message for %s, %s", 
//             request.callbackId(), request.publicKey());
        
//         if (!solanaService.verifySignature(message, request.publicKey(), request.signature())) {
//             sendError(headers.getSessionId(), "Invalid signature");
//             return;
//         }

//         validatorRepository.findByPublicKey(request.publicKey())
//             .ifPresentOrElse(
//                 validator -> handleExistingValidator(validator, headers, request.callbackId()),
//                 () -> handleNewValidator(request, headers)
//             );
//     }

//     @MessageMapping("/validate")
//     @Transactional
//     public void handleValidation(ValidationResponse response) {
//         if (!callbacks.containsKey(response.callbackId())) return;
        
//         callbacks.get(response.callbackId()).accept(response);
//         callbacks.remove(response.callbackId());
//     }

//     @Scheduled(fixedRate = 60_000)
//     public void distributeValidationTasks() {
//         List<Website> websites = websiteRepository.findByDisabledFalse();
//         List<ValidatorSession> validators = new ArrayList<>(activeValidators.values());

//         if (websites.isEmpty() || validators.isEmpty()) return;

//         int batchSize = Math.max(websites.size() / validators.size(), 1);

//         for (int i = 0; i < validators.size(); i++) {
//             ValidatorSession validator = validators.get(i);
//             int start = i * batchSize;
//             int end = Math.min(start + batchSize, websites.size());

//             websites.subList(start, end).forEach(website -> 
//                 sendValidationTask(website, validator)
//             );
//         }
//     }

//     private void sendValidationTask(Website website, ValidatorSession validator) {
//         String callbackId = UUID.randomUUID().toString();
        
//         callbacks.put(callbackId, response -> {
//             if (!solanaService.verifySignature(
//                 "Replying to " + callbackId,
//                 validator.publicKey(),
//                 response.signature()
//             )) return;

//             websiteTickRepository.save(new WebsiteTick(
//                 website.getId(),
//                 validator.id(),
//                 response.status(),
//                 response.latency(),
//                 Instant.now()
//             ));

//             validatorRepository.updatePendingPayouts(validator.id(), 100);
//         });

//         messagingTemplate.convertAndSendToUser(
//             validator.sessionId(),
//             "/queue/validate",
//             new ValidationTask(
//                 website.getUrl(),
//                 callbackId,
//                 website.getId()
//             )
//         );
//     }

//     private void handleExistingValidator(Validator validator, 
//                                        StompHeaderAccessor headers,
//                                        String callbackId) {
//         activeValidators.put(headers.getSessionId(), new ValidatorSession(
//             validator.getId(),
//             headers.getSessionId(),
//             validator.getPublicKey()
//         ));

//         messagingTemplate.convertAndSendToUser(
//             headers.getSessionId(),
//             "/queue/signup",
//             new SignupResponse(validator.getId(), callbackId)
//         );
//     }

//     private void handleNewValidator(SignupRequest request, StompHeaderAccessor headers) {
//         Validator newValidator = validatorRepository.save(new Validator(
//             request.publicKey(),
//             request.ip(),
//             "unknown"
//         ));

//         activeValidators.put(headers.getSessionId(), new ValidatorSession(
//             newValidator.getId(),
//             headers.getSessionId(),
//             newValidator.getPublicKey()
//         ));

//         messagingTemplate.convertAndSendToUser(
//             headers.getSessionId(),
//             "/queue/signup",
//             new SignupResponse(newValidator.getId(), request.callbackId())
//         );
//     }

//     private void sendError(String sessionId, String message) {
//         messagingTemplate.convertAndSendToUser(
//             sessionId,
//             "/queue/errors",
//             Map.of("error", message)
//         );
//     }
// }