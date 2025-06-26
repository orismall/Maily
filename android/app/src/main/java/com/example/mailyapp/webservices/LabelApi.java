package com.example.mailyapp.webservices;

import com.example.mailyapp.models.Label;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface LabelApi {

    @GET("labels")
    Call<List<Label>> getAllLabels();

    @GET("labels/{id}")
    Call<Label> getLabelById(@Path("id") String id);

    @POST("labels")
    Call<Label> createLabel(@Body Label label);

    @PATCH("labels/{id}")
    Call<Label> updateLabel(@Path("id") String id, @Body Label label);

    @DELETE("labels/{id}")
    Call<Void> deleteLabel(@Path("id") String id);
}
