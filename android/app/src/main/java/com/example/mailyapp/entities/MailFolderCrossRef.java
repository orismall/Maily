package com.example.mailyapp.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mail_folder")
public class MailFolderCrossRef {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String mailId;

    @NonNull
    public String folder;

    public MailFolderCrossRef(@NonNull String mailId, @NonNull String folder) {
        this.mailId = mailId;
        this.folder = folder;
    }
}