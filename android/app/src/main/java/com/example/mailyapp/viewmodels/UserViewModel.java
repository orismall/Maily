package com.example.mailyapp.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mailyapp.data.AppDatabase;
import com.example.mailyapp.entities.UserEntity;
import com.example.mailyapp.models.LoginRequest;
import com.example.mailyapp.models.LoginResponse;
import com.example.mailyapp.models.User;
import com.example.mailyapp.repositories.UserRepository;
import com.example.mailyapp.utils.ModelMapper;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final AppDatabase db;

    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    private final MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public UserViewModel(@NonNull Application application) {
        super(application);
        this.repository = new UserRepository(application.getApplicationContext());
        this.db = AppDatabase.getInstance(application.getApplicationContext());
    }

    public LiveData<Boolean> getRegistrationSuccess() {
        return registrationSuccess;
    }

    public LiveData<LoginResponse> getLoginResponse() {
        return loginResponse;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void registerUser(User user) {
        repository.registerUser(user, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    registrationSuccess.setValue(true);
                } else {
                    handleError(response, "Registration failed");
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

    public void loginUser(LoginRequest request) {
        repository.loginUser(request, new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse resp = response.body();
                    loginResponse.setValue(resp);

                    SharedPreferences prefs = getApplication()
                            .getSharedPreferences("session", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString("token", resp.getToken())
                            .putString("user_id", String.valueOf(resp.getUserId()))
                            .apply();

                    String userId = resp.getUserId();
                    fetchUserProfile(userId);

                } else {
                    handleError(response, "Login failed");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                errorMessage.setValue("Login error: " + t.getMessage());
            }
        });
    }

    private void handleError(Response<?> response, String defaultMsg) {
        try {
            String errorBody = response.errorBody().string();
            JSONObject json = new JSONObject(errorBody);
            String message = json.optString("error", defaultMsg);
            errorMessage.setValue(message);
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage.setValue(defaultMsg + ": Unknown error");
        }
    }
    public void fetchUserProfile(String userId) {
        repository.getUserById(userId, new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    UserEntity entity = ModelMapper.toEntity(user);

                    new Thread(() -> {
                        db.userDao().deleteAll();
                        db.userDao().insertUser(entity);
                        userLiveData.postValue(user);
                    }).start();
                } else {
                    errorMessage.setValue("Failed to load user profile");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                errorMessage.setValue("Error: " + t.getMessage());
            }
        });
    }
    public LiveData<UserEntity> getLoggedInUserLive() {
        return db.userDao().getLoggedInUserLive();
    }
}