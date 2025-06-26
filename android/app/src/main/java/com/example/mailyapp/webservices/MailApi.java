package com.example.mailyapp.webservices;

import com.example.mailyapp.models.Mail;
import com.example.mailyapp.models.MailFlagUpdate;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MailApi {

    // Get all inbox mails (paginated)
    @GET("inbox")
    Call<List<Mail>> getInboxMails(@Query("page") int page);

    // Get starred mails
    @GET("starred")
    Call<List<Mail>> getStarredMails(@Query("page") int page);

    // Get sent mails
    @GET("sent")
    Call<List<Mail>> getSentMails(@Query("page") int page);

    // Get drafts
    @GET("drafts")
    Call<List<Mail>> getDrafts(@Query("page") int page);

    // Get spam mails
    @GET("spam")
    Call<List<Mail>> getSpamMails(@Query("page") int page);

    // Get trash mails
    @GET("trash")
    Call<List<Mail>> getTrashMails(@Query("page") int page);

    // Get mail by ID
    @GET("mails/{id}")
    Call<Mail> getMailById(@Path("id") String id);

    // Send a new mail
    @POST("mails")
    Call<Mail> sendMail(@Body Mail mail);

    @POST("/api/drafts")
    Call<Mail> createDraft(@Body Mail draft);

    @POST("/api/drafts/{id}/send")
    Call<Mail> sendDraftAsMail(@Path("id") String draftId);

    @POST("/api/drafts/{id}/send")
    Call<Mail> sendDraftAsMailWithResponse(@Path("id") String draftId);

    @PATCH("/api/drafts/{id}")
    Call<Mail> updateDraft(@Path("id") String id, @Body Mail draft);

    // Update mail flags (read/starred)
    @PATCH("mails/{id}")
    Call<Void> updateMailFlags(@Path("id") String mailId, @Body Mail updateData);

    // Delete a mail (move to trash)
    @DELETE("mails/{id}")
    Call<Void> deleteMail(@Path("id") String mailId);

    // Search mails
    @GET("mails/search/{query}")
    Call<List<Mail>> searchMails(@Path("query") String query);

    // Mark mail as spam
    @PATCH("spam/{id}/mark-as-spam")
    Call<Void> markAsSpam(@Path("id") String mailId);

    // Mark mail as not spam
    @PATCH("spam/{id}/mark-as-not-spam")
    Call<Mail> markAsNotSpam(@Path("id") String mailId);

    // Permanently delete mail from spam
    @DELETE("spam/{id}")
    Call<Void> deleteSpamMail(@Path("id") String mailId);

    // Permanently delete mail from trash
    @DELETE("trash/{id}")
    Call<Void> deleteTrashMail(@Path("id") String mailId);

    // Restore mail from trash
    @POST("trash/{id}/restore")
    Call<Void> restoreFromTrash(@Path("id") String mailId);

    // Add mail to label
    @POST("labels/{labelId}/mails/{mailId}")
    Call<Void> addMailToLabel(@Path("labelId") String labelId, @Path("mailId") String mailId);

    // Remove mail from label
    @DELETE("labels/{labelId}/mails/{mailId}")
    Call<Void> removeMailFromLabel(@Path("labelId") String labelId, @Path("mailId") String mailId);

    // Get mails for a label
    @GET("labels/{labelId}/mails")
    Call<List<Mail>> getMailsByLabel(@Path("labelId") String labelId, @Query("page") int page);

    @PATCH("mails/{id}")
    Call<Void> updateMailFlags(@Path("id") String mailId, @Body MailFlagUpdate update);



}