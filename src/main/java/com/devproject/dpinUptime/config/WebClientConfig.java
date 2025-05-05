package com.devproject.dpinUptime.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() {
        System.out.println("WebClient bean created!");
        return WebClient.builder().build();
    }
}
