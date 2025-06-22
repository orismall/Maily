package com.example.mailyapp.models;

import java.util.List;

public class Mail {
    private int id;
    private String sender;
    private List<String> receiver;
    private String subject;
    private String content;
    private String date;
    private List<Integer> labels;
    private String type;

    public Mail(int id, String sender, List<String> receiver, String subject, String content, String date, List<Integer> labels) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.date = date;
        this.labels = labels;
        this.type = "mail";
    }

    // Getters
    public int getId() { return id; }
    public String getSender() { return sender; }
    public List<String> getReceiver() { return receiver; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public List<Integer> getLabels() { return labels; }
    public String getType() { return type; }

    public void setId(int id) { this.id = id; }
    public void setSender(String sender) { this.sender = sender; }
    public void setReceiver(List<String> receiver) { this.receiver = receiver; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setContent(String content) { this.content = content; }
    public void setDate(String date) { this.date = date; }
    public void setLabels(List<Integer> labels) { this.labels = labels; }
}




