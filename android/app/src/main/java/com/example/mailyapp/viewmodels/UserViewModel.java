package com.example.mailyapp.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mailyapp.models.LoginRequest;
import com.example.mailyapp.models.LoginResponse;
import com.example.mailyapp.models.User;
import com.example.mailyapp.webservices.RetrofitClient;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends ViewModel {

    private MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    private MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getRegistrationSuccess() {
        return registrationSuccess;
    }

    public LiveData<LoginResponse> getLoginResponse() {
        return loginResponse;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void registerUser(Context context, User user) {
        RetrofitClient.getUserAPI(context).registerUser(user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    registrationSuccess.setValue(true);
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject errorJson = new JSONObject(errorBody);
                        String message = errorJson.optString("error", "Registration failed");
                        errorMessage.setValue(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        errorMessage.setValue("Registration failed: Unknown error");
                    }
                    registrationSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                errorMessage.setValue("Registration error: " + t.getMessage());
                registrationSuccess.setValue(false);
            }
        });
    }


    public void loginUser(Context context, LoginRequest request) {
        RetrofitClient.getUserAPI(context).loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loginResponse.setValue(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject errorJson = new JSONObject(errorBody);
                        String message = errorJson.optString("error", "Login failed");
                        errorMessage.setValue(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        errorMessage.setValue("Login failed: Unknown error");
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                errorMessage.setValue("Login error: " + t.getMessage());
            }
        });
    }

}