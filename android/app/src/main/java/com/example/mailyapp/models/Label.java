package com.example.mailyapp.models;

import com.example.mailyapp.entities.LabelEntity;

import java.io.Serializable;
import java.util.List;

public class Label implements Serializable {
    private String id;
    private String name;
    private String color;
    private List<String> mailIds;

    public Label (String name) {
        this.name = name;
    }
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public List<String> getMailIds() { return mailIds; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setColor(String color) { this.color = color; }
    public void setMailIds(List<String> mailIds) { this.mailIds = mailIds; }

    public static Label fromEntity(LabelEntity entity) {
        Label label = new Label(entity.getName());
        label.setId(entity.getId());
        label.setColor(entity.getColor());
        label.setMailIds(entity.getMailIds());
        return label;
    }

}
