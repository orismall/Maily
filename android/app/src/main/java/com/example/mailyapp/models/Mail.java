package com.example.mailyapp.models;

import java.util.List;

public class Mail {
    private String id;
    private String sender;
    private List<String> receiver;
    private String subject;
    private String content;
    private String date;
    private List<String> labels;
    private String type;

    // Getters
    public String getId() { return id; }
    public String getSender() { return sender; }
    public List<String> getReceiver() { return receiver; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public List<String> getLabels() { return labels; }
    public String getType() { return type; }

    // Setters (required by Gson)
    public void setId(String id) { this.id = id; }
    public void setSender(String sender) { this.sender = sender; }
    public void setReceiver(List<String> receiver) { this.receiver = receiver; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setContent(String content) { this.content = content; }
    public void setDate(String date) { this.date = date; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public void setType(String type) { this.type = type; }
}
