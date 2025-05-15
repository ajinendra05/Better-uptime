
package com.devproject.dpinUptime.controller;

import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupRequest;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateResponse;
import org.springframework.stereotype.Component;

import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;

import org.springframework.web.socket.messaging.WebSocketStompClient;
import jakarta.annotation.PostConstruct;

import java.lang.reflect.Type;
import java.util.UUID;

import org.springframework.web.client.RestTemplate;
import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateRequest;

import com.devproject.dpinUptime.model.SolanaKeyPair;
import com.devproject.dpinUptime.model.Status;
import com.devproject.dpinUptime.service.UrlMonitoringService;
import com.devproject.dpinUptime.service.Validatorservice.SolanaServiceImpl;
import com.devproject.dpinUptime.service.Validatorservice.ValidatorService;
import com.devproject.dpinUptime.service.Validatorservice.WebsiteTickService;

public class ValidatorClient {
    private final WebSocketStompClient stompClient;
    private final String publicKey;
    private final String ip;
    private final SimpMessagingTemplate messagingTemplate;
    private final ValidatorService validatorService;

    private StompSession stompSession;

    public ValidatorClient(String publicKey,
            String ip,
            SimpMessagingTemplate messagingTemplate,
            ValidatorService validatorService) {
        this.publicKey = publicKey;
        this.ip = ip;
        this.messagingTemplate = messagingTemplate;
        this.validatorService = validatorService;
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
    }

    public void connect() {
        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders headers) {
                stompSession = session;
                handleWebSocketConnection(session);
            }
        };

        stompClient.connect("ws://localhost:8080/ws", handler);
    }

    private void handleWebSocketConnection(StompSession session) {
        String callbackId = UUID.randomUUID().toString();
        String message = String.format("Signed message for %s, %s", callbackId, publicKey);
        String signature = keyPairService.signMessage(message);

        // Send signup request via WebSocket
        session.send("/app/validator/signup", new SignupRequest(
                publicKey,
                signature,
                ip,
                callbackId));

        // Subscribe to validation tasks
        session.subscribe("/user/queue/validate", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ValidateRequest.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                ValidateRequest request = (ValidateRequest) payload;
                validateWebsite(request);
            }
        });
    }

    private void validateWebsite(ValidateRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            var status = restTemplate.getForEntity(request.getUrl(), String.class)
                    .getStatusCode();

            long latency = System.currentTimeMillis() - startTime;
            String signature = keyPair.sign("Replying to " + request.getCallbackId());

            if (stompSession != null && stompSession.isConnected()) {
                stompSession.send("/app/validate", new ValidateResponse(
                        status.is2xxSuccessful() ? Status.SUCCESS : Status.FAILURE,
                        (int) latency,
                        request.getCallbackId(),
                        signature,
                        request.getWebsiteId().toString()));
            }
        } catch (Exception e) {
            if (stompSession != null && stompSession.isConnected()) {
                stompSession.send("/app/validate", new ValidateResponse(
                        Status.FAILURE,
                        1000,
                        request.getCallbackId(),
                        keyPair.sign("Replying to " + request.getCallbackId()),
                        request.getWebsiteId().toString()));
            }
        }
    }
}
/*
 * @Component
 * public class ValidatorClient {
 * private final WebSocketStompClient stompClient;
 * private final SolanaKeyPair keyPair;
 * private final ValidatorService validatorService;
 * private final SolanaServiceImpl solanaService;
 * private final UrlMonitoringService websiteService;
 * private final WebsiteTickService websiteTickService;
 * private final SimpMessagingTemplate messagingTemplate;
 * private final RestTemplate restTemplate = new RestTemplate();
 * 
 * private StompSession stompSession;
 * 
 * public ValidatorClient(
 * SolanaKeyPair keyPair,
 * ValidatorService validatorService,
 * SolanaServiceImpl solanaService,
 * UrlMonitoringService websiteService,
 * WebsiteTickService websiteTickService,
 * SimpMessagingTemplate messagingTemplate) {
 * this.keyPair = keyPair;
 * this.validatorService = validatorService;
 * this.solanaService = solanaService;
 * this.websiteService = websiteService;
 * this.websiteTickService = websiteTickService;
 * this.messagingTemplate = messagingTemplate;
 * this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
 * }
 * 
 * public void connect(String publicKey) {
 * StompSessionHandler handler = new StompSessionHandlerAdapter() {
 * 
 * @Override
 * public void afterConnected(StompSession session, StompHeaders headers) {
 * stompSession = session;
 * String callbackId = UUID.randomUUID().toString();
 * String message = String.format("Signed message for %s", callbackId);
 * String signature = keyPair.sign(message);
 * 
 * session.send("/app/validator/register", new SignupRequest(
 * publicKey,
 * signature,
 * callbackId));
 * 
 * session.subscribe("/user/queue/validate", new StompFrameHandler() {
 * 
 * @Override
 * public Type getPayloadType(StompHeaders headers) {
 * return ValidateRequest.class;
 * }
 * 
 * @Override
 * public void handleFrame(StompHeaders headers, Object payload) {
 * ValidateRequest request = (ValidateRequest) payload;
 * validateWebsite(request);
 * }
 * });
 * }
 * };
 * 
 * stompClient.connect("ws://localhost:8080/ws", handler);
 * }
 * 
 * private void validateWebsite(ValidateRequest request) {
 * long startTime = System.currentTimeMillis();
 * 
 * try {
 * var status = restTemplate.getForEntity(request.getUrl(), String.class)
 * .getStatusCode();
 * 
 * long latency = System.currentTimeMillis() - startTime;
 * String signature = keyPair.sign("Replying to " + request.getCallbackId());
 * 
 * if (stompSession != null && stompSession.isConnected()) {
 * stompSession.send("/app/validate", new ValidateResponse(
 * status.is2xxSuccessful() ? Status.SUCCESS : Status.FAILURE,
 * (int) latency,
 * request.getCallbackId(),
 * signature,
 * keyPair.getPublicKey(),
 * request.getWebsiteId().toString()));
 * }
 * } catch (Exception e) {
 * if (stompSession != null && stompSession.isConnected()) {
 * stompSession.send("/app/validate", new ValidateResponse(
 * Status.FAILURE,
 * 1000,
 * request.getCallbackId(),
 * keyPair.sign("Replying to " + request.getCallbackId()),
 * keyPair.getPublicKey(),
 * request.getWebsiteId().toString()));
 * }
 * }
 * }
 * }
 * 
 * 
 */

// @Component
// public class ValidatorClient implements WebSocketHandler {

// private final WebSocketClient webSocketClient;
// private final SolanaKeyPair keyPair;

// public ValidatorClient(SolanaKeyPair keyPair) {
// this.keyPair = keyPair;
// this.webSocketClient = new StandardWebSocketClient();
// }

// @PostConstruct
// public void connect() {
// WebSocketConnectionManager manager = new WebSocketConnectionManager(
// webSocketClient,
// this,
// "ws://localhost:8080/ws");
// manager.start();
// }

// @Override
// public void afterConnectionEstablished(WebSocketSession session) {
// String callbackId = UUID.randomUUID().toString();
// String message = String.format("Signed message for %s, %s",
// callbackId, keyPair.getPublicKey());

// String signature = keyPair.sign(message);

// session.sendMessage(new TextMessage(
// new SignupRequest(
// callbackId,
// keyPair.getPublicKey(),
// signature,
// "127.0.0.1").toJson()));
// }

// @Override
// protected void handleTextMessage(WebSocketSession session, TextMessage
// message) {
// // Handle validation tasks and responses
// // Similar to Node.js implementation
// }

// // Other required WebSocketHandler methods
// }