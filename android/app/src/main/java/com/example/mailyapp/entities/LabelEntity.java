package com.example.mailyapp.entities;

import androidx.annotation.NonNull;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mailyapp.models.Label;
import com.example.mailyapp.models.User;
import com.example.mailyapp.utils.Converters;


import java.util.ArrayList;
import java.util.List;

@Entity
public class LabelEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String color;
    @TypeConverters(Converters.class)
    private List<String> mailIds;

    public LabelEntity(String name) {
        this.name = name;
        this.color = "#000000";
        this.mailIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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

    public static LabelEntity fromLabel(Label label) {
        LabelEntity entity = new LabelEntity(label.getName());
        entity.setId(label.getId());
        entity.setColor(label.getColor());
        entity.setMailIds(label.getMailIds());
        return entity;
    }

}
