package com.example.mailyapp.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.mailyapp.data.AppDatabase;
import com.example.mailyapp.data.DraftDao;
import com.example.mailyapp.entities.DraftEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DraftRepository {
    private final DraftDao draftDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DraftRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        draftDao = db.draftDao();
    }

    public LiveData<List<DraftEntity>> getAllDrafts() {
        return draftDao.getAllDrafts();
    }

    public void insert(DraftEntity draft) {
        executorService.execute(() -> draftDao.insert(draft));
    }

    public void deleteById(String id) {
        executorService.execute(() -> draftDao.deleteById(id));
    }
}

