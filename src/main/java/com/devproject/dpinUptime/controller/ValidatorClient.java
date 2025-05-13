// package com.devproject.dpinUptime.controller;

// import com.devproject.dpinUptime.DTO.ValidatorsDTO.SignupRequest;
// import com.devproject.dpinUptime.DTO.ValidatorsDTO.ValidateResponse;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
// import org.springframework.web.socket.WebSocketHandler;
// import org.springframework.web.socket.WebSocketSession;
// import org.springframework.web.socket.client.WebSocketClient;
// import org.springframework.web.socket.client.standard.StandardWebSocketClient;
// import org.springframework.web.socket.TextMessage;
// import org.springframework.web.socket.handler.TextWebSocketHandler;
// import org.springframework.web.socket.messaging.SessionDisconnectEvent;
// import org.springframework.web.socket.messaging.SessionConnectEvent;

// @Component
// public class ValidatorClient implements WebSocketHandler {

//     private final WebSocketClient webSocketClient;
//     private final SolanaKeyPair keyPair;

//     public ValidatorClient(SolanaKeyPair keyPair) {
//         this.keyPair = keyPair;
//         this.webSocketClient = new StandardWebSocketClient();
//     }

//     @PostConstruct
//     public void connect() {
//         WebSocketConnectionManager manager = new WebSocketConnectionManager(
//                 webSocketClient,
//                 this,
//                 "ws://localhost:8080/ws");
//         manager.start();
//     }

//     @Override
//     public void afterConnectionEstablished(WebSocketSession session) {
//         String callbackId = UUID.randomUUID().toString();
//         String message = String.format("Signed message for %s, %s",
//                 callbackId, keyPair.getPublicKey());

//         String signature = keyPair.sign(message);

//         session.sendMessage(new TextMessage(
//                 new SignupRequest(
//                         callbackId,
//                         keyPair.getPublicKey(),
//                         signature,
//                         "127.0.0.1").toJson()));
//     }

//     @Override
//     protected void handleTextMessage(WebSocketSession session, TextMessage message) {
//         // Handle validation tasks and responses
//         // Similar to Node.js implementation
//     }

//     // Other required WebSocketHandler methods
// }