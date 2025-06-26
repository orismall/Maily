package com.example.mailyapp.webservices;

import com.example.mailyapp.models.LoginRequest;
import com.example.mailyapp.models.LoginResponse;
import com.example.mailyapp.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface UserAPI {

    @POST("users")
    Call<Void> registerUser(@Body User user);

    @POST("tokens")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

}