package com.example.mailyapp.utils;

import com.example.mailyapp.entities.UserEntity;
import com.example.mailyapp.models.User;

public class ModelMapper {

    public static UserEntity toEntity(User user) {
        return new UserEntity(user.email, user.firstName, user.lastName, user.avatar);
    }
}