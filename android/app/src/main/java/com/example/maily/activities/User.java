package com.example.maily.activities;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.room.Entity;
import androidx.room.PrimaryKey;



@Entity
public class User {
    @PrimaryKey(autoGenerate=true)
    private int id;
    private String email;
    private String password;
    private String confirmPassword;
    private String firstName;
    private String lastName;
    private String gender;
    private Date birthdate;
    private String avatar;

    private List<Mail> inbox;
    private List<Mail> sent;
    private List<Mail> drafts;
    private List<Mail> trash;
    private List<Mail> spam;

    public User(int id, String email, String password, String confirmPassword,
                String firstName, String lastName, String gender,
                Date birthdate, String avatar) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthdate = birthdate;
        this.avatar = avatar;

        this.inbox = new ArrayList<>();
        this.sent = new ArrayList<>();
        this.drafts = new ArrayList<>();
        this.trash = new ArrayList<>();
        this.spam = new ArrayList<>();
    }

    // Getters and setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Date getBirthdate() { return birthdate; }
    public void setBirthdate(Date birthdate) { this.birthdate = birthdate; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public List<Mail> getInbox() { return inbox; }
    public List<Mail> getSent() { return sent; }
    public List<Mail> getDrafts() { return drafts; }
    public List<Mail> getTrash() { return trash; }
    public List<Mail> getSpam() { return spam; }

}
