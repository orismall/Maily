package com.example.mailyapp.webservices;

import static com.example.mailyapp.MyApplication.context;

import android.content.Context;

import com.example.mailyapp.R;
import com.example.mailyapp.models.Label;
import com.example.mailyapp.models.LabelDeserializer;
import com.example.mailyapp.models.Mail;
import com.example.mailyapp.models.MailDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Mail.class, new MailDeserializer())
                    .registerTypeAdapter(Label.class, new LabelDeserializer())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(context.getString(R.string.BaseUrl))
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static UserAPI getUserAPI(Context context) {
        return getInstance(context).create(UserAPI.class);
    }
}