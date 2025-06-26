package com.example.mailyapp.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey
    public int id;

    public String email;
    public String firstName;
    public String lastName;
    public String gender;
    public String birthdate;
    public String avatar;

    public User(int id, String email, String firstName, String lastName,
                String gender, String birthdate, String avatar) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthdate = birthdate;
        this.avatar = avatar;
    }

}