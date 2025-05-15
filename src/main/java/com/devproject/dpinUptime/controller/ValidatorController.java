package com.devproject.dpinUptime.controller;

import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupRequest;

import com.devproject.dpinUptime.service.Validatorservice.ValidatorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import java.util.Map;

@RestController
@RequestMapping("/api/validators")
public class ValidatorController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ValidatorServiceImpl validatorService;

    @Autowired
    public ValidatorController(
                              SimpMessagingTemplate messagingTemplate,
                              ValidatorServiceImpl validatorService) {
        // this.keyPairService = keyPairService;
        this.messagingTemplate = messagingTemplate;
        this.validatorService = validatorService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> handleValidatorLogin(@RequestBody SignupRequest request) {
        // Validate input
        if (!isValidPublicKey(request.getPublicKey())) {
            return ResponseEntity.badRequest().body("Invalid public key format");
        }

        // solanaKeyPair keyPair = new solanaKeyPair(
        //     request.getPublicKey(),
        //     request.getSignature()
        // );
        // Create validator client
        ValidatorClient client = new ValidatorClient(
            request.getPublicKey(),
            request.getIp(),
            // keyPairService,
            messagingTemplate,
            validatorService
        );

        // Initiate WebSocket connection
        client.connect();

        return ResponseEntity.accepted().body(
            Map.of("message", "Validator client initialization started")
        );
    }

    private boolean isValidPublicKey(String publicKey) {
        // Add validation logic for Solana public key format
        return publicKey != null && publicKey.length() == 44;
    }
}