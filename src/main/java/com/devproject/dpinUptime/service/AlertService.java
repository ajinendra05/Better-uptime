package com.devproject.dpinUptime.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;
import com.devproject.dpinUptime.model.MonitoredUrl;
import com.devproject.dpinUptime.DTO.SlackMessage; // Ensure this class is defined
@Service
public class AlertService {
    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.to}") private String toEmail;

    public void triggerAlert(MonitoredUrl url) {
        sendEmailAlert(url);
        sendSlackAlert(url);
    }

    private void sendEmailAlert(MonitoredUrl url) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[ALERT] URL Down: " + url.getName());
        message.setText("URL: " + url.getUrl() + "\nTime: " + LocalDateTime.now());
        mailSender.send(message);
    }

    private void sendSlackAlert(MonitoredUrl url) {
        WebClient.create("https://slack.com/api/chat.postMessage")
            .post()
            .bodyValue(new SlackMessage("URL Down: " + url.getUrl())) // Ensure SlackMessage class is defined
            .retrieve()
            .bodyToMono(String.class)
            .subscribe();
    }
}
