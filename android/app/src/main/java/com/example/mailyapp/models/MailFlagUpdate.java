package com.example.mailyapp.models;

import java.util.List;

public class MailFlagUpdate {
    private Boolean isStarred;
    private Boolean isRead;

    private List<String> labels;

    public MailFlagUpdate(Boolean isStarred, Boolean isRead) {
        this.isStarred = isStarred;
        this.isRead = isRead;
    }

    // ✅ New constructor for labels
    public MailFlagUpdate(List<String> labels) {
        this.labels = labels;
    }

    public Boolean getIsStarred() { return isStarred; }
    public Boolean getIsRead() { return isRead; }
}