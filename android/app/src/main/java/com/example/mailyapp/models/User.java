package com.example.mailyapp.models;

public class User {
    public String email;
    public String password;
    public String confirmPassword;
    public String firstName;
    public String lastName;
    public String gender;
    public String birthdate;
    public String avatar;

    // Constructor
    public User(String firstName, String lastName, String email, String password,String confirmPassword, String gender, String birthDate, String image) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.gender = gender;
        this.birthdate = birthDate;
        this.avatar = image;
    }
}