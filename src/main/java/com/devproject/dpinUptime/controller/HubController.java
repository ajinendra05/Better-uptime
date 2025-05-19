package com.devproject.dpinUptime.controller;

import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupRequest;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupResponse;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateResponse;
import com.devproject.dpinUptime.service.Validatorservice.SolanaServiceImpl;
import com.devproject.dpinUptime.service.Validatorservice.ValidatorServiceImpl;
import com.devproject.dpinUptime.service.Validatorservice.WebsiteTickService;
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
// import org.springframework.scheduling.config.TaskExecutionOutcome.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.devproject.dpinUptime.model.Status;

import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidatorSession;

@Controller
public class HubController {
    private static final Logger logger = LoggerFactory.getLogger(HubController.class);
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
                logger.error("Invalid signature for public key: {}", request.getPublicKey());
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

            logger.info("Validator {} signed up with session ID {}", validator.getId(), headers.getSessionId());

            messagingTemplate.convertAndSendToUser(
                    session.getSessionId(),
                    "/queue/signup",
                    new SignupResponse(validator.getId(), request.getCallbackId()));

        } catch (Exception e) {
            logger.error("Signup error: {}", e.getMessage());
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
            logger.error("Validation error: {}", e.getMessage());
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
                        logger.warn("Validator {} reported website {} as UP", validator.getId(), website.getUrl());
                    }
                    else {
                        logger.warn("Validator {} reported website {} as DOWN", validator.getId(), website.getUrl());
                    }

                    logger.info("Saving tick: {} for website {}", response.getStatus(), website.getUrl());
                    websiteTickService.saveTick(
                            website.getId(),
                            validator.getId(),
                            response.getStatus(),
                            response.getLatency());
                    logger.info("Website tick saved: {} for validator {}", response.getStatus(), validator.getId());
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

/*
 * 
 * @Controller
 * public class HubController {
 * private static final Logger logger =
 * LoggerFactory.getLogger(HubController.class);
 * private final Map<String, ValidatorSession> activeValidators = new
 * ConcurrentHashMap<>();
 * private final Map<String, Consumer<ValidateResponse>> callbacks = new
 * ConcurrentHashMap<>();
 * 
 * private final ValidatorServiceImpl validatorService;
 * private final WebsiteServiceImpl websiteService;
 * private final WebsiteTickServiceImpl websiteTickService;
 * private final SimpMessagingTemplate messagingTemplate;
 * private final SolanaServiceImpl solanaService;
 * 
 * @Autowired
 * public HubController(ValidatorServiceImpl validatorService,
 * WebsiteServiceImpl websiteService,
 * WebsiteTickServiceImpl websiteTickService,
 * SimpMessagingTemplate messagingTemplate,
 * SolanaServiceImpl solanaService) {
 * this.validatorService = validatorService;
 * this.websiteService = websiteService;
 * this.websiteTickService = websiteTickService;
 * this.messagingTemplate = messagingTemplate;
 * this.solanaService = solanaService;
 * }
 * 
 * @MessageMapping("/validator/signup")
 * public void handleSignup(SignupRequest request, StompHeaderAccessor headers)
 * {
 * try {
 * String expectedMessage = String.format("Signed message for %s, %s",
 * request.getCallbackId(),
 * request.getPublicKey());
 * 
 * if (!solanaService.verifySignature(expectedMessage,
 * request.getPublicKey(),
 * request.getSignature())) {
 * sendError(headers.getSessionId(), "Invalid signature");
 * return;
 * }
 * 
 * Validator validator = validatorService.getOrCreateValidator(
 * request.getPublicKey(),
 * request.getIp());
 * 
 * ValidatorSession session = new ValidatorSession(
 * validator.getId(),
 * headers.getSessionId(),
 * request.getPublicKey());
 * 
 * activeValidators.put(headers.getSessionId(), session);
 * 
 * messagingTemplate.convertAndSendToUser(
 * session.getSessionId(),
 * "/queue/signup",
 * new SignupResponse(validator.getId(), request.getCallbackId()));
 * 
 * } catch (Exception e) {
 * logger.error("Signup error: {}", e.getMessage());
 * sendError(headers.getSessionId(), "Authentication failed");
 * }
 * }
 * 
 * @Scheduled(fixedRate = 60_000)
 * public void distributeValidationTasks() {
 * List<MonitoredUrl> websites = websiteService.getActiveWebsites();
 * List<ValidatorSession> validators = new
 * ArrayList<>(activeValidators.values());
 * 
 * if (websites.isEmpty() || validators.isEmpty())
 * return;
 * 
 * websites.forEach(website -> {
 * validators.forEach(validator -> {
 * String callbackId = UUID.randomUUID().toString();
 * 
 * callbacks.put(callbackId, response -> {
 * if (!verifyResponseSignature(response, validator))
 * return;
 * 
 * websiteTickService.saveTick(
 * website.getId(),
 * validator.getId(),
 * response.getStatus(),
 * response.getLatency());
 * 
 * validatorService.addCredits(validator.getId(), 100);
 * });
 * 
 * messagingTemplate.convertAndSendToUser(
 * validator.getSessionId(),
 * "/queue/validate",
 * new ValidateRequest(
 * website.getUrl(),
 * callbackId,
 * website.getId()));
 * });
 * });
 * }
 * 
 * private boolean verifyResponseSignature(ValidateResponse response,
 * ValidatorSession validator) {
 * String expectedMessage = String.format("Replying to %s",
 * response.getCallbackId());
 * return solanaService.verifySignature(
 * expectedMessage,
 * validator.getPublicKey(),
 * response.getSignature());
 * }
 * 
 * private void sendError(String sessionId, String message) {
 * messagingTemplate.convertAndSendToUser(
 * sessionId,
 * "/queue/errors",
 * Map.of("error", message));
 * }
 * }
 * 
 */

/*
 * 
 * @Controller
 * public class HubController {
 * 
 * private static final Logger logger =
 * LoggerFactory.getLogger(HubController.class);
 * 
 * private final Map<String, ValidatorSession> activeValidators = new
 * ConcurrentHashMap<>();
 * private final Map<String, Consumer<ValidateResponse>> callbacks = new
 * ConcurrentHashMap<>();
 * 
 * private final ValidatorServiceImpl validatorService;
 * private final UrlMonitoringService websiteService;
 * private final WebsiteTickService websiteTickService;
 * private final SimpMessagingTemplate messagingTemplate;
 * private final SolanaServiceImpl solanaService;
 * 
 * @Autowired
 * public HubController(
 * ValidatorServiceImpl validatorService,
 * UrlMonitoringService websiteService,
 * WebsiteTickService websiteTickService,
 * SimpMessagingTemplate messagingTemplate,
 * SolanaServiceImpl solanaService) {
 * this.validatorService = validatorService;
 * this.websiteService = websiteService;
 * this.websiteTickService = websiteTickService;
 * this.messagingTemplate = messagingTemplate;
 * this.solanaService = solanaService;
 * }
 * 
 * @PostMapping("/signup")
 * public SignupResponse signupValidator(SignupRequest request) {
 * try {
 * String expectedMessage = String.format("Signed message for %s, %s",
 * request.getCallbackId(),
 * request.getPublicKey());
 * 
 * boolean isValid = solanaService.verifySignature(
 * expectedMessage,
 * request.getPublicKey(),
 * request.getSignature()
 * );
 * 
 * if (!isValid) {
 * throw new IllegalArgumentException("Invalid signature");
 * }
 * 
 * // Create Solana keypair and Validator client (pseudo-code, adapt as needed)
 * Object solanaKeypair = solanaService.createKeypair(request.getPublicKey());
 * Object validatorClient =
 * validatorService.createValidatorClient(solanaKeypair);
 * 
 * Validator validator = validatorService.getOrCreateValidator(
 * request.getPublicKey(),
 * request.getIp()
 * );
 * 
 * return new SignupResponse(validator.getId(), request.getCallbackId());
 * } catch (Exception e) {
 * logger.error("Signup error: {}", e.getMessage());
 * throw new RuntimeException("Signup failed: " + e.getMessage());
 * }
 * }
 * 
 * 
 * 
 * 
 * 
 * @PostMapping("/validator/login")
 * public ResponseEntity<?> handleValidatorLogin(@RequestBody SignupRequest
 * request) {
 * try {
 * String expectedMessage = String.format("Signed message for %s, %s",
 * request.getCallbackId(),
 * request.getPublicKey());
 * 
 * if (!solanaService.verifySignature(expectedMessage,
 * request.getPublicKey(),
 * request.getSignature())) {
 * return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
 * }
 * 
 * Validator validator = validatorService.getOrCreateValidator(
 * request.getPublicKey(),
 * request.getIp());
 * 
 * // Create and start validator client
 * // SolanaKeyPair solanaKeyPair =
 * solanaService.getKeyPairForPublicKey(request.getPublicKey());
 * // ValidatorClient validatorClient = new ValidatorClient(
 * // solanaKeyPair,
 * // validatorService,
 * // solanaService,
 * // websiteService,
 * // websiteTickService,
 * // messagingTemplate);
 * // validatorClient.connect();
 * 
 * return ResponseEntity.ok(new SignupResponse(
 * validator.getId(),
 * request.getCallbackId()));
 * } catch (Exception e) {
 * logger.error("Login error: {}", e.getMessage());
 * return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
 * }
 * }
 * 
 * @Scheduled(fixedRate = 60_000)
 * public void distributeValidationTasks() {
 * List<MonitoredUrl> websites = websiteService.getActiveUrl();
 * List<ValidatorSession> validators = new
 * ArrayList<>(activeValidators.values());
 * 
 * if (websites.isEmpty() || validators.isEmpty())
 * return;
 * 
 * websites.forEach(website -> {
 * validators.forEach(validator -> {
 * String callbackId = UUID.randomUUID().toString();
 * 
 * callbacks.put(callbackId, response -> {
 * if (!verifyResponseSignature(response, validator))
 * return;
 * 
 * websiteTickService.saveTick(
 * website.getId(),
 * validator.getId(),
 * response.getStatus(),
 * response.getLatency());
 * 
 * validatorService.addCredits(validator.getId(), 100);
 * });
 * 
 * messagingTemplate.convertAndSendToUser(
 * validator.getSessionId(),
 * "/queue/validate",
 * new ValidateRequest(
 * website.getUrl(),
 * callbackId,
 * website.getId()));
 * });
 * });
 * }
 * 
 * @MessageMapping("/validator/login")
 * public void handleSignup(SignupRequest request, StompHeaderAccessor headers)
 * {
 * try {
 * String expectedMessage = String.format("Signed message for %s, %s",
 * request.getCallbackId(),
 * request.getPublicKey());
 * 
 * if (!solanaService.verifySignature(
 * expectedMessage,
 * request.getPublicKey(),
 * request.getSignature())) {
 * sendError(headers.getSessionId(), "Invalid signature");
 * return;
 * }
 * 
 * Validator validator = validatorService.getOrCreateValidator(
 * request.getPublicKey(),
 * request.getIp());
 * 
 * ValidatorSession session = new ValidatorSession(
 * validator.getId(),
 * headers.getSessionId(),
 * request.getPublicKey());
 * 
 * activeValidators.put(headers.getSessionId(), session);
 * 
 * messagingTemplate.convertAndSendToUser(
 * session.getSessionId(),
 * "/queue/signup",
 * new SignupResponse(validator.getId(), request.getCallbackId()));
 * } catch (Exception e) {
 * logger.error("Signup error: {}", e.getMessage());
 * sendError(headers.getSessionId(), "Authentication failed");
 * }
 * }
 * 
 * @MessageMapping("/validate")
 * public void handleValidation(ValidateResponse response) {
 * if (callbacks.containsKey(response.getCallbackId())) {
 * callbacks.get(response.getCallbackId()).accept(response);
 * callbacks.remove(response.getCallbackId());
 * }
 * }
 * 
 * private boolean verifyResponseSignature(ValidateResponse response,
 * ValidatorSession validator) {
 * String expectedMessage = String.format("Replying to %s",
 * response.getCallbackId());
 * return solanaService.verifySignature(
 * expectedMessage,
 * validator.getPublicKey(),
 * response.getSignature());
 * }
 * 
 * private void sendError(String sessionId, String message) {
 * messagingTemplate.convertAndSendToUser(
 * sessionId,
 * "/queue/errors",
 * Map.of("error", message));
 * }
 * }
 * 
 */

// @Controller
// public class HubController {

// private final Map<String, ValidatorSession> activeValidators = new
// ConcurrentHashMap<>();
// private final Map<String, Consumer<ValidationResult>> callbacks = new
// ConcurrentHashMap<>();

// private final ValidatorRepository validatorRepository;
// private final WebsiteRepository websiteRepository;
// private final WebsiteTickRepository websiteTickRepository;
// private final SimpMessagingTemplate messagingTemplate;
// private final SolanaService solanaService;

// @Autowired
// public HubController(ValidatorRepository validatorRepository,
// WebsiteRepository websiteRepository,
// WebsiteTickRepository websiteTickRepository,
// SimpMessagingTemplate messagingTemplate,
// SolanaService solanaService) {
// this.validatorRepository = validatorRepository;
// this.websiteRepository = websiteRepository;
// this.websiteTickRepository = websiteTickRepository;
// this.messagingTemplate = messagingTemplate;
// this.solanaService = solanaService;
// }

// @MessageMapping("/signup")
// public void handleSignup(SignupRequest request, StompHeaderAccessor headers)
// {
// String message = String.format("Signed message for %s, %s",
// request.callbackId(), request.publicKey());

// if (!solanaService.verifySignature(message, request.publicKey(),
// request.signature())) {
// sendError(headers.getSessionId(), "Invalid signature");
// return;
// }

// validatorRepository.findByPublicKey(request.publicKey())
// .ifPresentOrElse(
// validator -> handleExistingValidator(validator, headers,
// request.callbackId()),
// () -> handleNewValidator(request, headers)
// );
// }

// @MessageMapping("/validate")
// @Transactional
// public void handleValidation(ValidationResponse response) {
// if (!callbacks.containsKey(response.callbackId())) return;

// callbacks.get(response.callbackId()).accept(response);
// callbacks.remove(response.callbackId());
// }

// @Scheduled(fixedRate = 60_000)
// public void distributeValidationTasks() {
// List<Website> websites = websiteRepository.findByDisabledFalse();
// List<ValidatorSession> validators = new
// ArrayList<>(activeValidators.values());

// if (websites.isEmpty() || validators.isEmpty()) return;

// int batchSize = Math.max(websites.size() / validators.size(), 1);

// for (int i = 0; i < validators.size(); i++) {
// ValidatorSession validator = validators.get(i);
// int start = i * batchSize;
// int end = Math.min(start + batchSize, websites.size());

// websites.subList(start, end).forEach(website ->
// sendValidationTask(website, validator)
// );
// }
// }

// private void sendValidationTask(Website website, ValidatorSession validator)
// {
// String callbackId = UUID.randomUUID().toString();

// callbacks.put(callbackId, response -> {
// if (!solanaService.verifySignature(
// "Replying to " + callbackId,
// validator.publicKey(),
// response.signature()
// )) return;

// websiteTickRepository.save(new WebsiteTick(
// website.getId(),
// validator.id(),
// response.status(),
// response.latency(),
// Instant.now()
// ));

// validatorRepository.updatePendingPayouts(validator.id(), 100);
// });

// messagingTemplate.convertAndSendToUser(
// validator.sessionId(),
// "/queue/validate",
// new ValidationTask(
// website.getUrl(),
// callbackId,
// website.getId()
// )
// );
// }

// private void handleExistingValidator(Validator validator,
// StompHeaderAccessor headers,
// String callbackId) {
// activeValidators.put(headers.getSessionId(), new ValidatorSession(
// validator.getId(),
// headers.getSessionId(),
// validator.getPublicKey()
// ));

// messagingTemplate.convertAndSendToUser(
// headers.getSessionId(),
// "/queue/signup",
// new SignupResponse(validator.getId(), callbackId)
// );
// }

// private void handleNewValidator(SignupRequest request, StompHeaderAccessor
// headers) {
// Validator newValidator = validatorRepository.save(new Validator(
// request.publicKey(),
// request.ip(),
// "unknown"
// ));

// activeValidators.put(headers.getSessionId(), new ValidatorSession(
// newValidator.getId(),
// headers.getSessionId(),
// newValidator.getPublicKey()
// ));

// messagingTemplate.convertAndSendToUser(
// headers.getSessionId(),
// "/queue/signup",
// new SignupResponse(newValidator.getId(), request.callbackId())
// );
// }

// private void sendError(String sessionId, String message) {
// messagingTemplate.convertAndSendToUser(
// sessionId,
// "/queue/errors",
// Map.of("error", message)
// );
// }
// }