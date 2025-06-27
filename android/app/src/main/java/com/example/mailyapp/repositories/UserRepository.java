package com.example.mailyapp.repositories;

import android.content.Context;

import com.example.mailyapp.models.LoginRequest;
import com.example.mailyapp.models.LoginResponse;
import com.example.mailyapp.models.User;
import com.example.mailyapp.webservices.RetrofitClient;
import com.example.mailyapp.webservices.UserAPI;

import retrofit2.Callback;

public class UserRepository {

    private final UserAPI userAPI;

    public UserRepository(Context context) {
        this.userAPI = RetrofitClient.getUserAPI(context);
    }

    public void registerUser(User user, Callback<Void> callback) {
        userAPI.registerUser(user).enqueue(callback);
    }

    public void loginUser(LoginRequest request, Callback<LoginResponse> callback) {
        userAPI.loginUser(request).enqueue(callback);
    }
    public void getUserById(String userId, Callback<User> callback) {
        userAPI.getUserById(userId).enqueue(callback);
    }
}