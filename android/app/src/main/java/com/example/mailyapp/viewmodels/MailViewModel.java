package com.example.mailyapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mailyapp.entities.MailEntity;
import com.example.mailyapp.models.Mail;
import com.example.mailyapp.repositories.MailRepository;

import java.util.List;

public class MailViewModel extends AndroidViewModel {

    private final MailRepository repository;
    private final LiveData<List<MailEntity>> allMails;

    private final MutableLiveData<List<Mail>> remoteMails = new MutableLiveData<>();

    public MailViewModel(@NonNull Application application) {
        super(application);
        repository = new MailRepository(application);
        allMails = repository.getAllMails();
    }

    public void fetchFolder(String folderName, int page) {
        repository.fetchMailsByFolder(folderName, page, remoteMails);
    }

    // Room methods
    public LiveData<List<MailEntity>> getAllMails() {
        return allMails;
    }

    public void insert(MailEntity mail) {
        repository.insert(mail);
    }

    public void insertAll(List<MailEntity> mails) {
        repository.insertAll(mails);
    }

    public void deleteById(String mailId) {
        repository.deleteById(mailId);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public LiveData<MailEntity> getMailById(String id) {
        return repository.getMailById(id);
    }

    // Retrofit methods
    public LiveData<List<Mail>> getRemoteMails() {
        return remoteMails;
    }

    public void updateStarredFlag(String mailId, boolean isStarred) {
        repository.updateStarredFlag(mailId, isStarred);
    }
    public LiveData<List<MailEntity>> getLocalMailsByFolder(String folder) {
        return repository.getMailsByFolder(folder);
    }

}