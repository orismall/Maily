package com.example.mailyapp.viewmodels;

import static com.example.mailyapp.MyApplication.context;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mailyapp.entities.LabelEntity;
import com.example.mailyapp.models.Label;
import com.example.mailyapp.models.User;
import com.example.mailyapp.repositories.LabelRepository;

import java.util.List;

public class LabelViewModel extends AndroidViewModel {

    private final LabelRepository repository;
    private final LiveData<List<LabelEntity>> allLabels;
    private final MutableLiveData<List<Label>> remoteLabels = new MutableLiveData<>();

    public LabelViewModel(@NonNull Application application) {
        super(application);
        repository = new LabelRepository(application);
        allLabels = repository.getAllLabels();
    }

    public void createLabel(Label label, java.util.function.Consumer<Label> callback) {
        repository.createLabel(label, callback);
    }

    public LiveData<List<LabelEntity>> getAllLabels() {
        return allLabels;
    }

    public LiveData<LabelEntity> getLabelById(String id) {
        return repository.getLabelById(id);
    }

    public void insert(LabelEntity label) {
        repository.insert(label);
    }

    public void insertAll(List<LabelEntity> labels) {
        repository.insertAll(labels);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public LiveData<List<Label>> getRemoteLabels() {
        return remoteLabels;
    }

    public void refreshFromApi() {
        fetchAllLabels();
    }

    public void updateLabel(String id, Label updatedLabel, java.util.function.Consumer<Label> callback) {
        repository.updateLabel(id, updatedLabel, callback);
    }


    public void fetchAllLabels() {
        repository.syncLabelsFromApi(
                () -> {
                    // Optional: callback when complete
                },
                labelEntities -> {
                    // Success: Already inserted into Room, LiveData will auto-update
                },
                error -> {
                    // Optional: log or show error
                    Log.e("LabelViewModel", "Failed to fetch labels", error);
                }
        );
    }


}
