package com.example.mailyapp.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.mailyapp.data.AppDatabase;
import com.example.mailyapp.data.MailDao;
import com.example.mailyapp.entities.MailEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailRepository {

    private final MailDao mailDao;
    private final LiveData<List<MailEntity>> allMails;

    // Executor runs DB operations on background thread
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public MailRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        mailDao = db.mailDao();
        allMails = mailDao.getAllMails();
    }

    public LiveData<List<MailEntity>> getAllMails() {
        return allMails;
    }

    public void insert(MailEntity mail) {
        executorService.execute(() -> mailDao.insert(mail));
    }

    public void insertAll(List<MailEntity> mails) {
        executorService.execute(() -> mailDao.insertAll(mails));
    }

    public void deleteById(int mailId) {
        executorService.execute(() -> mailDao.deleteById(mailId));
    }

    public void deleteAll() {
        executorService.execute(mailDao::deleteAll);
    }

    public LiveData<MailEntity> getMailById(int id) {
        return mailDao.getMailById(id);
    }
}
