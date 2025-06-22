package com.example.mailyapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mailyapp.entities.MailEntity;
import com.example.mailyapp.repositories.MailRepository;

import java.util.List;

public class MailViewModel extends AndroidViewModel {

    private final MailRepository repository;
    private final LiveData<List<MailEntity>> allMails;

    public MailViewModel(@NonNull Application application) {
        super(application);
        repository = new MailRepository(application);
        allMails = repository.getAllMails();
    }

    public LiveData<List<MailEntity>> getAllMails() {
        return allMails;
    }

    public void insert(MailEntity mail) {
        repository.insert(mail);
    }

    public void insertAll(List<MailEntity> mails) {
        repository.insertAll(mails);
    }

    public void deleteById(int mailId) {
        repository.deleteById(mailId);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public LiveData<MailEntity> getMailById(int id) {
        return repository.getMailById(id);
    }
}
