package com.example.mailyapp.webservices;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        SharedPreferences prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String userId = prefs.getString("user_id", null);

        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (userId != null) {
            builder.header("user-id", userId);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }
}
