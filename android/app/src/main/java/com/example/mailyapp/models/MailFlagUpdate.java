package com.example.mailyapp.models;

public class MailFlagUpdate {
    private Boolean isStarred;
    private Boolean isRead;

    public MailFlagUpdate(Boolean isStarred, Boolean isRead) {
        this.isStarred = isStarred;
        this.isRead = isRead;
    }

    public Boolean getIsStarred() { return isStarred; }
    public Boolean getIsRead() { return isRead; }
}