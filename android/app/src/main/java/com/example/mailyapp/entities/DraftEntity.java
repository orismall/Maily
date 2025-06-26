package com.example.mailyapp.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mailyapp.utils.Converters;

import java.util.List;

@Entity(tableName = "drafts")
@TypeConverters(Converters.class)
public class DraftEntity {
    @PrimaryKey
    @NonNull
    public String id;

    public String sender;
    public List<String> receiver;
    public String subject;
    public String content;
    public String date;
    public List<String> labels;
    public String type;
    public boolean isRead;
    public boolean isStarred;

    public DraftEntity(
            @NonNull String id,
            String sender,
            List<String> receiver,
            String subject,
            String content,
            String date,
            List<String> labels,
            String type,
            boolean isRead,
            boolean isStarred
    ) {
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

    public DraftEntity() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<String> getReceiver() {
        return receiver;
    }

    public void setReceiver(List<String> receiver) {
        this.receiver = receiver;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }
}
