package com.example.mailyapp.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mailyapp.models.Mail;
import com.example.mailyapp.utils.Converters;
import java.util.List;

@Entity(tableName = "mails")
@TypeConverters(Converters.class)
public class MailEntity {

    @NonNull
    @PrimaryKey
    private String id;
    private String sender;
    private List<String> receiver;
    private String subject;
    private String content;
    private String date;
    private List<String> labels;
    private String type;
    private boolean isRead;
    private boolean isStarred;

    public MailEntity(String id, String sender, List<String> receiver, String subject,
                      String content, String date, List<String> labels, String type, boolean isRead,
                      boolean isStarred) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.date = date;
        this.labels = labels;
        this.type = type;
        this.isRead = isRead;
        this.isStarred = isStarred;
    }

    public MailEntity() {}

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public List<String> getReceiver() { return receiver; }
    public void setReceiver(List<String> receiver) { this.receiver = receiver; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }

    public void setRead(boolean read) { isRead = read; }

    public boolean isStarred() { return isStarred; }

    public void setStarred(boolean starred) { isStarred = starred; }
    public Mail toModel() {
        Mail mail = new Mail();
        mail.setId(id);
        mail.setSender(sender);
        mail.setReceiver(receiver);
        mail.setSubject(subject);
        mail.setContent(content);
        mail.setDate(date);
        mail.setLabels(labels);
        mail.setType(type);
        mail.setRead(isRead);
        mail.setStarred(isStarred);
        return mail;
    }
}