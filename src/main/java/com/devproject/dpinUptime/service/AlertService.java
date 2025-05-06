package com.devproject.dpinUptime.service;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;
import com.devproject.dpinUptime.model.MonitoredUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AlertService {
    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private WebClient webClient;

    // public AlertService(JavaMailSender mailSender,
    // @Value("${spring.mail.username}") String fromEmail) {
    // this.mailSender = mailSender;
    // this.fromEmail = fromEmail;
    // }

    // public void sendEmailAlert(String to, String subject, String body) {
    // SimpleMailMessage message = new SimpleMailMessage();
    // message.setFrom(fromEmail);
    // message.setTo(to);
    // message.setSubject(subject);
    // message.setText(body);
    // mailSender.send(message);
    // }
    // @Value("${spring.mail.to}") private String toEmail;
    // @Value("${spring.mail.from}") private String fromEmail;
    // @Value("${slack.webhook.url}") private String slackWebhookUrl;
    // @Value("${slack.channel}") private String slackChannel;

    public void triggerAlert(MonitoredUrl url) {
        // sendEmailAlert(url);
        sendSlackAlert(url);
    }

    // private void sendEmailAlert(MonitoredUrl url) {
    // try {
    // SimpleMailMessage message = new SimpleMailMessage();
    // message.setFrom(fromEmail);
    // message.setTo(toEmail);
    // message.setSubject("[ALERT] URL Down: " + url.getName());
    // message.setText("URL: " + url.getUrl() + "\nTime: " + LocalDateTime.now());
    // mailSender.send(message);
    // } catch (MailException e) {
    // log.error("Email alert failed: {}", e.getMessage());
    // }
    // }

    private void sendSlackAlert(MonitoredUrl url) {
        // SlackMessage payload = new SlackMessage(
        // "URL Down: " + url.getUrl(),
        // slackChannel
        // );

        // webClient.post()
        // .uri(slackWebhookUrl)
        // .bodyValue(payload)
        // .retrieve()
        // .bodyToMono(String.class)
        // .retry(3)
        // .subscribe(
        // response -> log.info("Slack alert sent"),
        // error -> log.error("Slack alert failed: {}", error.getMessage())
        // );
    }
}
// @Service
// public class AlertService {
// @Autowired
// private JavaMailSender mailSender;
// @Value("${spring.mail.to}") private String toEmail;

// public void triggerAlert(MonitoredUrl url) {
// sendEmailAlert(url);
// sendSlackAlert(url);
// }

// private void sendEmailAlert(MonitoredUrl url) {
// SimpleMailMessage message = new SimpleMailMessage();
// message.setTo(toEmail);
// message.setSubject("[ALERT] URL Down: " + url.getName());
// message.setText("URL: " + url.getUrl() + "\nTime: " + LocalDateTime.now());
// mailSender.send(message);
// }

// private void sendSlackAlert(MonitoredUrl url) {
// WebClient.create("https://slack.com/api/chat.postMessage")
// .post()
// .bodyValue(new SlackMessage("URL Down: " + url.getUrl())) // Ensure
// SlackMessage class is defined
// .retrieve()
// .bodyToMono(String.class)
// .subscribe();
// }
// }
