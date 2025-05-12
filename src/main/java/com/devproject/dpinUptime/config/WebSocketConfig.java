package com.devproject.dpinUptime.config;

// import statement removed as it is incorrect
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        // registry.addEndpoint("/ws-endpoint").setAllowedOrigins("*").withSockJS();

        // registry.addEndpoint("/ws")
        // .setAllowedOriginPatterns("http://localhost:*") // Specific origins
        // .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // registry.enableSimpleBroker("/queue", "/topic");
        registry.enableSimpleBroker("/queue", "/topic");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        // logger.info("WebSocket message broker configured with user destination
        // prefix: /user");
        // logger.info(registry.getUserDestinationPrefix());
        // registry.setApplicationDestinationPrefixes("/app");
        // registry.enableSimpleBroker("/topic");
        // registry.enableSimpleBroker("/queue"); // <- No /user here
        // registry.setUserDestinationPrefix("/user");
    }

    // @Override
    // public void configureClientInboundChannel(ChannelRegistration registration) {
    // registration.interceptors(new ChannelInterceptor() {
    // @Override
    // public Message<?> preSend(Message<?> message, MessageChannel channel) {
    // StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    // if (StompCommand.CONNECT.equals(accessor.getCommand())) {
    // String sessionId = accessor.getSessionId();

    // accessor.setUser(new WebSocketPrincipal(sessionId));

    // logger.info("WebSocket connected: " + sessionId);
    // logger.info("WebSocket connected: " + accessor.getUser().getName());
    // logger.info("user name: " + accessor.getUser().getName());
    // }
    // return message;
    // }
    // });
    // }
    // @Override
    // public void configureClientInboundChannel(ChannelRegistration registration) {
    // registration.interceptors(new ChannelInterceptor() {
    // @Override
    // public Message<?> preSend(Message<?> message, MessageChannel channel) {
    // StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    // if (StompCommand.CONNECT.equals(accessor.getCommand())) {
    // String sessionId = accessor.getSessionId();
    // String user = accessor.getFirstNativeHeader("user");
    // List<GrantedAuthority> authorities = new ArrayList<>();
    // authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    // Authentication auth = new UsernamePasswordAuthenticationToken(user, user,
    // authorities);
    // SecurityContextHolder.getContext().setAuthentication(auth);
    // accessor.setUser(new WebSocketPrincipal(sessionId));

    // logger.info("WebSocket connected: " + sessionId);
    // logger.info("WebSocket connected: " + accessor.getUser().getName());
    // logger.info("user name: " + accessor.getUser().getName());
    // }
    // return message;
    // }
    // });
    // }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {

                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                    // List<GrantedAuthority> authorities = new ArrayList<>();
                    // authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    Authentication auth = new UsernamePasswordAuthenticationToken(accessor.getSessionId(),
                            accessor.getSessionId(),
                            AuthorityUtils.createAuthorityList("ROLE_USER"));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.info("User: {}" + auth.getName());
                    // logger.info("Session ID: {}", accessor.getSessionId());
                    // logger.info("User: {}", accessor.getUser());
                    // logger.info("User name: {}", accessor.getUser().getName());
                    // logger.info("Authorities: {}", auth.getAuthorities());
                    accessor.setUser(auth);

                }

                return message;
            }
        });
    }

}