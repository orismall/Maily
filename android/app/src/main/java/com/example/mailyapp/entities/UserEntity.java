package com.example.mailyapp.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @NonNull
    @PrimaryKey
    public String email;
    public String firstName;
    public String lastName;
    public String gender;
    public String birthdate;
    public String avatar;

    public UserEntity(String email, String firstName, String lastName,
                      String gender, String birthdate, String avatar) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthdate = birthdate;
        this.avatar = avatar;
    }
    @Ignore
    public UserEntity(String email, String firstName, String lastName, String avatar) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = null;
        this.birthdate = null;
        this.avatar = avatar;
    }
}