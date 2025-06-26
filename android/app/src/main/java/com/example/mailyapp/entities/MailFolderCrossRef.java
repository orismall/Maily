package com.example.mailyapp.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "mail_folder",
        primaryKeys = {"mailId", "folder"}
)

public class MailFolderCrossRef {
    @NonNull
    public String mailId;

    @NonNull
    public String folder;

    public MailFolderCrossRef(@NonNull String mailId, @NonNull String folder) {
        this.mailId = mailId;
        this.folder = folder;
    }
}