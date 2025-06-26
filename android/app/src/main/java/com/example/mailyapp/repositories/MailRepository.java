package com.example.mailyapp.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.mailyapp.data.AppDatabase;
import com.example.mailyapp.data.LabelDao;
import com.example.mailyapp.data.MailDao;
import com.example.mailyapp.entities.LabelEntity;
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
import java.util.function.Consumer;

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
        executorService.execute(() -> {
            MailDao mailDao = AppDatabase.getInstance(application).mailDao();
            LabelDao labelDao = AppDatabase.getInstance(application).labelDao();

            MailEntity mail = mailDao.getNow(mailId);
            if (mail != null && mail.getLabels() != null) {
                List<String> labelIds = new ArrayList<>(mail.getLabels());

                for (String labelId : labelIds) {
                    LabelEntity label = labelDao.getNow(labelId);
                    if (label != null && label.getMailIds() != null && label.getMailIds().contains(mailId)) {
                        List<String> updatedMailIds = new ArrayList<>(label.getMailIds());
                        updatedMailIds.remove(mailId);
                        label.setMailIds(updatedMailIds);
                        labelDao.update(label); // ✅ Use update instead of insert
                    }
                }
            }

            // 💥 Delete mail from Room
            mailDao.deleteById(mailId);
        });
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
                liveData.postValue(null);
                return;
        }

        call.enqueue(new Callback<List<Mail>>() {
            @Override
            public void onResponse(Call<List<Mail>> call, Response<List<Mail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Mail> mailList = response.body();
                    Log.d("MailRepository", "Fetched " + folder + ": " + mailList.size() + " mails");

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

                    executorService.execute(() -> {
                        // Clear all mappings for this folder (better than per-mail deletes)
                        mailDao.removeAllMappingsForFolder(folder);

                        mailDao.insertAll(entityList);       // Upsert mails
                        mailDao.insertFolderRefs(refList);   // Insert new folder mappings
                    });


                    liveData.postValue(mailList);
                } else {
                    Log.e("MailRepository", "API error for " + folder + ": " + response.code());
                    liveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<Mail>> call, Throwable t) {
                Log.e("MailRepository", "Network error: " + t.getMessage(), t);
                liveData.postValue(null);
            }
        });
    }



    public void refreshAllMailsFromApi(Runnable onComplete) {
        List<String> folders = Arrays.asList("inbox", "sent", "starred", "drafts", "spam", "trash");
        AtomicInteger remaining = new AtomicInteger(folders.size());

        // 💥 Optional: wipe all mail and folder data before sync
        executorService.execute(() -> {
            mailDao.deleteAll(); // Clear mails and folder mappings
        });

        for (String folder : folders) {
            MutableLiveData<List<Mail>> tempLiveData = new MutableLiveData<>();

            tempLiveData.observeForever(new Observer<List<Mail>>() {
                @Override
                public void onChanged(List<Mail> mails) {
                    tempLiveData.removeObserver(this);
                    if (remaining.decrementAndGet() == 0) {
                        onComplete.run(); // All folders processed
                    }
                }
            });

            fetchMailsByFolder(folder, 1, tempLiveData); // Will reinsert updated mails + folders
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

    public void moveToTrash(String mailId, Runnable onSuccess, Consumer<Throwable> onFailure) {
        MailApi api = RetrofitClient.getInstance(application).create(MailApi.class);
        api.deleteMail(mailId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    executorService.execute(() -> {
                        MailEntity mail = mailDao.getNow(mailId);
                        if (mail != null && mail.getLabels() != null) {
                            List<String> labelIds = new ArrayList<>(mail.getLabels());
                            LabelDao labelDao = AppDatabase.getInstance(application).labelDao();

                            // 1. Remove mailId from each label
                            for (String labelId : labelIds) {
                                LabelEntity label = labelDao.getNow(labelId);
                                if (label != null && label.getMailIds() != null && label.getMailIds().contains(mailId)) {
                                    List<String> updatedMailIds = new ArrayList<>(label.getMailIds());
                                    updatedMailIds.remove(mailId);
                                    label.setMailIds(updatedMailIds);
                                    labelDao.insert(label); // assuming REPLACE behavior here
                                }
                            }

                            // 2. Remove all labelIds from the mail
                            mail.setLabels(new ArrayList<>());
                            mailDao.insert(mail); // update the mail with empty labels
                        }

                        onSuccess.run();
                    });
                } else {
                    onFailure.accept(new Exception("Trash failed with code: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                onFailure.accept(t);
            }
        });
    }


    public void addLabelToMailLocally(String mailId, String labelId) {
        executorService.execute(() -> {
            MailEntity mail = mailDao.getNow(mailId);
            if (mail != null) {
                List<String> labels = new ArrayList<>(mail.getLabels() != null ? mail.getLabels() : new ArrayList<>());
                if (!labels.contains(labelId)) {
                    labels.add(labelId);
                    mail.setLabels(labels);
                    mailDao.insert(mail); // REPLACE will update existing row
                }
            }
        });
    }

    public void removeLabelFromMailLocally(String mailId, String labelId) {
        executorService.execute(() -> {
            MailEntity mail = mailDao.getNow(mailId);
            if (mail != null && mail.getLabels() != null) {
                List<String> labels = new ArrayList<>(mail.getLabels());
                if (labels.contains(labelId)) {
                    labels.remove(labelId);
                    mail.setLabels(labels);
                    mailDao.insert(mail);
                }
            }
        });
    }

    public LiveData<List<MailEntity>> getMailsByLabel(String labelId) {
        return mailDao.getMailsByLabel(labelId);
    }

    public LiveData<List<MailEntity>> searchMails(String query) {
        return mailDao.searchMails(query);
    }

}