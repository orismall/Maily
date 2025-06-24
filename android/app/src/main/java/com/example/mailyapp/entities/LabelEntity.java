package com.example.mailyapp.entities;

import androidx.annotation.NonNull;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mailyapp.utils.Converters;


import java.util.ArrayList;
import java.util.List;

@Entity
public class LabelEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String user;
    private String name;
    private String color;
    @TypeConverters(Converters.class)
    private List<String> mailIds;

    public LabelEntity(String name) {
        this.name = name;
        this.user = "you@example.com"; // Dummy or current user
        this.color = "#FFB74D";        // Default orange shade
        this.mailIds = new ArrayList<>();
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public List<String> getMailIds() {
        return mailIds;
    }
    public void setMailIds(List<String> mailIds) {
        this.mailIds = mailIds;
    }
    public String toString() {
        return "" + name;
    }
}
