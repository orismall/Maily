package com.example.mailyapp.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mailyapp.utils.Converters;

import java.util.List;

@Entity(tableName = "mails")
@TypeConverters(Converters.class)  // מסמן להשתמש בממיר כדי לשמור רשימות
public class MailEntity {

    @PrimaryKey
    private int id;

    private String sender;
    private List<String> receiver;
    private String subject;
    private String content;
    private String date;
    private List<Integer> labels;
    private String type;

    public MailEntity(int id, String sender, List<String> receiver, String subject, String content, String date, List<Integer> labels, String type) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.date = date;
        this.labels = labels;
        this.type = type;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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

    public List<Integer> getLabels() { return labels; }
    public void setLabels(List<Integer> labels) { this.labels = labels; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
