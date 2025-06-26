package com.example.mailyapp.repositories;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mailyapp.data.AppDatabase;
import com.example.mailyapp.data.LabelDao;
import com.example.mailyapp.entities.LabelEntity;
import com.example.mailyapp.models.Label;
import com.example.mailyapp.webservices.LabelApi;
import com.example.mailyapp.webservices.MailApi;
import com.example.mailyapp.webservices.RetrofitClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LabelRepository {

    private final LabelDao labelDao;
    private final LiveData<List<LabelEntity>> allLabels;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Application application;

    public LabelRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getInstance(application);
        labelDao = db.labelDao();
        allLabels = labelDao.index();
    }

    public LiveData<List<LabelEntity>> getAllLabels() {
        return allLabels;
    }

    public LiveData<LabelEntity> getLabelById(String id) {
        return labelDao.get(id);
    }

    public void insert(LabelEntity label) {
        executorService.execute(() -> labelDao.insert(label));
    }

    public void insertAll(List<LabelEntity> labels) {
        executorService.execute(() -> {
            for (LabelEntity l : labels) {
                labelDao.insert(l);
            }
        });
    }

    public void deleteById(String id) {
        // 1. Delete from Room
        executorService.execute(() -> labelDao.deleteById(id));

        // 2. Delete from MongoDB via LabelApi
        LabelApi labelApi = RetrofitClient.getInstance(application).create(LabelApi.class);
        labelApi.deleteLabel(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.e("LabelRepository", "Failed to delete label from backend: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("LabelRepository", "Error deleting label from backend", t);
            }
        });
    }

    public void updateLabel(String id, Label updatedLabel, Consumer<Label> callback) {
        LabelApi labelApi = RetrofitClient.getInstance(application).create(LabelApi.class);
        labelApi.updateLabel(id, updatedLabel).enqueue(new Callback<Label>() {
            @Override
            public void onResponse(Call<Label> call, Response<Label> response) {
                if (response.isSuccessful()) {
                    Label label = response.body();
                    if (label == null) {
                        label = updatedLabel;
                    }

                    LabelEntity entity = convertToEntity(label);
                    insert(entity); // Save to Room
                    callback.accept(label);
                } else {
                    Log.e("LabelRepository", "Update failed: " + response.code());
                    callback.accept(null);
                }
            }


            @Override
            public void onFailure(Call<Label> call, Throwable t) {
                Log.e("LabelRepository", "Network error while updating label", t);
                callback.accept(null);
            }
        });
    }


    public void deleteAll() {
        executorService.execute(labelDao::deleteAll);
    }

    /**
     * Fetches labels from the API and syncs them into Room.
     */
    public void syncLabelsFromApi(Runnable onComplete, Consumer<List<LabelEntity>> onSuccess, Consumer<Throwable> onError) {
        LabelApi labelApi = RetrofitClient.getInstance(application).create(LabelApi.class);
        labelApi.getAllLabels().enqueue(new Callback<List<Label>>() {
            @Override
            public void onResponse(Call<List<Label>> call, Response<List<Label>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Label> remoteLabels = response.body();
                    List<LabelEntity> entities = new ArrayList<>();

                    for (Label label : remoteLabels) {
                        LabelEntity entity = convertToEntity(label);
                        entities.add(entity);
                    }

                    executorService.execute(() -> {
                        labelDao.deleteAll();  // Wipe outdated local labels
                        labelDao.insert(entities.toArray(new LabelEntity[0]));

                        onSuccess.accept(entities);
                        onComplete.run();
                    });

                } else {
                    onError.accept(new Exception("Label fetch failed: " + response.code()));
                    onComplete.run();
                }
            }

            @Override
            public void onFailure(Call<List<Label>> call, Throwable t) {
                onError.accept(t);
                onComplete.run();
            }
        });
    }

    /**
     * Creates a new label via the API and stores it in Room.
     */
    public void createLabel(Label label, Consumer<Label> callback) {
        LabelApi labelApi = RetrofitClient.getInstance(application).create(LabelApi.class);
        labelApi.createLabel(label).enqueue(new Callback<Label>() {
            @Override
            public void onResponse(Call<Label> call, Response<Label> response) {
                Label createdLabel = response.body();

                if (response.isSuccessful()) {
                    if (createdLabel == null) {
                        Log.w("LabelRepository", "Created label body is null. Fallback to original.");
                        createdLabel = label;
                        createdLabel.setId(label.getName());
                    }

                    insert(convertToEntity(createdLabel));
                    callback.accept(createdLabel);

                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("LabelRepository", "Create failed: " + response.code() + " - " + errorBody);
                    } catch (IOException e) {
                        Log.e("LabelRepository", "Error reading errorBody", e);
                    }
                    callback.accept(null);
                }
            }

            @Override
            public void onFailure(Call<Label> call, Throwable t) {
                Log.e("LabelRepository", "Network error: " + t.getMessage(), t);
                callback.accept(null);
            }
        });
    }


    /**
     * Helper method to convert Label (API model) → LabelEntity (Room).
     */
    private LabelEntity convertToEntity(Label label) {
        LabelEntity entity = new LabelEntity(label.getName());
        entity.setId(label.getId());
        entity.setColor(label.getColor() != null ? label.getColor() : "#000000");
        entity.setMailIds(label.getMailIds() != null ? label.getMailIds() : new ArrayList<>());
        return entity;
    }

    public void addMailToLabel(String mailId, String labelId, Runnable onSuccess, Consumer<Throwable> onFailure) {
        LabelApi api = RetrofitClient.getInstance(application).create(LabelApi.class);
        api.addMailToLabel(mailId, labelId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    executorService.execute(() -> {
                        LabelEntity label = labelDao.getNow(labelId);
                        if (label != null && !label.getMailIds().contains(mailId)) {
                            label.getMailIds().add(mailId);
                            labelDao.insert(label); // Save updated label
                        }
                        onSuccess.run();
                    });
                } else {
                    onFailure.accept(new Exception("Failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                onFailure.accept(t);
            }
        });
    }

    public void removeMailFromLabel(String mailId, String labelId, Runnable onSuccess, Consumer<Throwable> onFailure) {
        LabelApi api = RetrofitClient.getInstance(application).create(LabelApi.class);
        api.removeMailFromLabel(mailId, labelId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    executorService.execute(() -> {
                        LabelEntity label = labelDao.getNow(labelId);
                        if (label != null && label.getMailIds().contains(mailId)) {
                            label.getMailIds().remove(mailId);
                            labelDao.insert(label);
                        }
                        onSuccess.run();
                    });
                } else {
                    onFailure.accept(new Exception("Failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                onFailure.accept(t);
            }
        });
    }

}
