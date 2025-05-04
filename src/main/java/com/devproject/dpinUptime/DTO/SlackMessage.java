package com.devproject.dpinUptime.DTO;
// Ensure this class is defined
public class SlackMessage {
    private String text;
    private String channel;

    public SlackMessage(String text, String channel) {
        this.text = text;
        this.channel = channel;
    }

    // Getters and setters (if needed)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}