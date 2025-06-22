package com.example.mailyapp.webservices;

import com.example.mailyapp.models.Mail;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MailApi {

    // Get all mails (same as /api/mails)
    @GET("/api/mails")
    Call<List<Mail>> getAllMails();

    // Get a specific mail by ID
    @GET("/api/mails/{id}")
    Call<Mail> getMailById(@Path("id") int id);

    // Send a new mail
    @POST("/api/mails")
    Call<Void> sendMail(@Body Mail mail);
}
