package com.example.mailyapp.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.mailyapp.data.AppDatabase;
import com.example.mailyapp.data.MailDao;
import com.example.mailyapp.entities.MailEntity;
import com.example.mailyapp.entities.MailFolderCrossRef;
import com.example.mailyapp.models.Mail;
import com.example.mailyapp.models.MailFlagUpdate;
import com.example.mailyapp.webservices.MailApi;
import com.example.mailyapp.webservices.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MailRepository {

    private final MailDao mailDao;
    private final LiveData<List<MailEntity>> allMails;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Application application;

    public MailRepository(Application application) {
        this.application = application;
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

    public void deleteById(String mailId) {
        executorService.execute(() -> mailDao.deleteById(mailId));
    }

    public void deleteAll() {
        executorService.execute(mailDao::deleteAll);
    }

    public LiveData<MailEntity> getMailById(String id) {
        return mailDao.getMailById(id);
    }

    // === Retrofit ===

    public void fetchMailsByFolder(String folder, int page, MutableLiveData<List<Mail>> liveData) {
        MailApi mailApi = RetrofitClient.getInstance(application).create(MailApi.class);
        Call<List<Mail>> call;

        switch (folder.toLowerCase()) {
            case "inbox":
                call = mailApi.getInboxMails(page);
                break;
            case "sent":
                call = mailApi.getSentMails(page);
                break;
            case "starred":
                call = mailApi.getStarredMails(page);
                break;
            case "drafts":
                call = mailApi.getDrafts(page);
                break;
            case "spam":
                call = mailApi.getSpamMails(page);
                break;
            case "trash":
                call = mailApi.getTrashMails(page);
                break;
            default:
                Log.e("MailRepository", "Unknown folder: " + folder);
                return;
        }

        call.enqueue(new Callback<List<Mail>>() {
            @Override
            public void onResponse(Call<List<Mail>> call, Response<List<Mail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Mail> mailList = response.body();
                    Log.d("MailRepository", "Fetched " + folder + " mails: " + mailList.size());

                    liveData.postValue(mailList);

                    for (Mail mail : mailList) {
                        Log.d("MailRepository", "ID: " + mail.getId() +
                                ", Sender: " + mail.getSender() +
                                ", Subject: " + mail.getSubject() +
                                ", Content: " + mail.getContent());
                    }

                    // Convert to MailEntity
                    List<MailEntity> entityList = new ArrayList<>();
                    List<MailFolderCrossRef> refList = new ArrayList<>();

                    for (Mail mail : mailList) {
                        entityList.add(new MailEntity(
                                mail.getId(),
                                mail.getSender(),
                                mail.getReceiver(),
                                mail.getSubject(),
                                mail.getContent(),
                                mail.getDate(),
                                mail.getLabels(),
                                mail.getType(),
                                mail.isRead(),
                                mail.isStarred()
                        ));
                        refList.add(new MailFolderCrossRef(mail.getId(), folder));
                    }

                    insertAll(entityList);
                    insertFolderRefs(refList);
                } else {
                    Log.e("MailRepository", "Failed to fetch " + folder + ": " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Mail>> call, Throwable t) {
                Log.e("MailRepository", "Network error: " + t.getMessage(), t);
            }
        });


    }

    public void refreshAllMailsFromApi(Runnable onComplete) {
        List<String> folders = Arrays.asList("inbox", "sent", "starred", "drafts", "spam", "trash");
        AtomicInteger remaining = new AtomicInteger(folders.size());

        for (String folder : folders) {
            MutableLiveData<List<Mail>> tempLiveData = new MutableLiveData<>();

            // Observe once, then remove the observer
            tempLiveData.observeForever(new Observer<List<Mail>>() {
                @Override
                public void onChanged(List<Mail> mails) {
                    // When data arrives, mark this folder as done
                    tempLiveData.removeObserver(this);

                    if (remaining.decrementAndGet() == 0) {
                        onComplete.run();
                    }
                }
            });

            fetchMailsByFolder(folder, 1, tempLiveData);
        }
    }



    public void sendMail(Mail mail, Callback<Mail> callback) {
        MailApi mailApi = RetrofitClient.getInstance(application).create(MailApi.class);
        mailApi.sendMail(mail).enqueue(callback);
    }

    public void updateStarredFlag(String mailId, boolean isStarred) {
        executorService.execute(() -> {
            mailDao.updateStarredFlag(mailId, isStarred);

            MailApi mailApi = RetrofitClient.getInstance(application).create(MailApi.class);
            MailFlagUpdate update = new MailFlagUpdate(isStarred, null);
            mailApi.updateMailFlags(mailId, update).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("MailRepository", "isStarred updated on server for " + mailId);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("MailRepository", "Failed to update isStarred on server", t);
                }
            });
        });
    }

    public void updateReadFlag(String mailId, boolean isRead) {
        executorService.execute(() -> {
            mailDao.updateReadFlag(mailId, isRead);

            MailApi mailApi = RetrofitClient.getInstance(application).create(MailApi.class);
            MailFlagUpdate update = new MailFlagUpdate(null, isRead); // null במקום isStarred
            mailApi.updateMailFlags(mailId, update).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("MailRepository", "isRead updated on server for " + mailId);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("MailRepository", "Failed to update isRead on server", t);
                }
            });
        });
    }



    public void insertFolderRef(String mailId, String folder) {
        executorService.execute(() ->
                mailDao.insertFolderRef(new MailFolderCrossRef(mailId, folder))
        );
    }

    public void insertFolderRefs(List<MailFolderCrossRef> refs) {
        executorService.execute(() -> mailDao.insertFolderRefs(refs));
    }

    public void removeMailFromFolder(String mailId, String folder) {
        executorService.execute(() -> mailDao.removeMailFromFolder(mailId, folder));
    }

    public void removeMailFromAllFolders(String mailId) {
        executorService.execute(() -> mailDao.removeMailFromAllFolders(mailId));
    }

    public LiveData<List<MailEntity>> getMailsByFolder(String folder) {
        if (folder.equalsIgnoreCase("starred")) {
            return mailDao.getStarredMails();
        } else {
            return mailDao.getMailsByFolder(folder);
        }
    }

    public LiveData<List<MailEntity>> getStarredMails() {
        return mailDao.getStarredMails();
    }
}