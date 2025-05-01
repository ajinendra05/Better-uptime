package com.devproject.dpinUptime.DTO;

public class SlackMessage {
    private String text; // The message text sent to Slack

    // Constructor
    public SlackMessage(String text) {
        this.text = text;
    }

    // Getters and setters (required for JSON serialization)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}