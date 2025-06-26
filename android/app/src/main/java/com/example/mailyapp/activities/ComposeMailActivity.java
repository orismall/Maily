package com.example.mailyapp.activities;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mailyapp.R;
import com.example.mailyapp.entities.MailEntity;
import com.example.mailyapp.models.Mail;
import com.example.mailyapp.repositories.MailRepository;
import com.example.mailyapp.webservices.MailApi;
import com.example.mailyapp.webservices.RetrofitClient;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComposeMailActivity extends AppCompatActivity {

    private EditText etTo, etSubject, etBody;
    private View rootView;
    private boolean isDraft = false;
    private String draftId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        rootView = findViewById(android.R.id.content);

        ImageButton btnClose = findViewById(R.id.btnClose);
        ImageButton btnSend = findViewById(R.id.btnSend);
        etTo = findViewById(R.id.etTo);
        etSubject = findViewById(R.id.etSubject);
        etBody = findViewById(R.id.etBody);

        EditText fromEmail = findViewById(R.id.fromEmail);
        String userEmail = getSharedPreferences("session", MODE_PRIVATE).getString("email", "");
        fromEmail.setText(userEmail);
        fromEmail.setEnabled(false);
        fromEmail.setFocusable(false);

        Intent intent = getIntent();
        isDraft = intent.getBooleanExtra("isDraft", false);
        if (isDraft) {
            draftId = intent.getStringExtra("draftId");
            etTo.setText(intent.getStringExtra("to"));
            etSubject.setText(intent.getStringExtra("subject"));
            etBody.setText(intent.getStringExtra("body"));
        }

        btnClose.setOnClickListener(v -> saveDraft());

        btnSend.setOnClickListener(v -> {
            String toInput = etTo.getText().toString().trim();
            String subject = etSubject.getText().toString().trim();
            String body = etBody.getText().toString().trim();

            if (toInput.isEmpty()) {
                Snackbar.make(rootView, "No recipient found. Please enter a valid email address.", Snackbar.LENGTH_LONG).show();
                return;
            }

            List<String> recipients = Arrays.stream(toInput.split(","))
                    .map(String::trim)
                    .filter(email -> !email.isEmpty())
                    .collect(Collectors.toList());

            if (recipients.isEmpty()) {
                Snackbar.make(rootView, "No recipient found. Please enter a valid email address.", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (subject.isEmpty() && body.isEmpty()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Send empty message?")
                        .setMessage("Send this message without a subject or text?")
                        .setPositiveButton("Send", (dialog, which) -> sendMail(recipients, subject, body))
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                if (isDraft && draftId != null) {
                    MailApi api = RetrofitClient.getInstance(this).create(MailApi.class);
                    Mail updated = new Mail();
                    updated.setReceiver(recipients);
                    updated.setSubject(subject.isEmpty() ? "(No subject)" : subject);
                    updated.setContent(body);
                    updated.setType("draft");

                    Call<Mail> updateCall = api.updateDraft(draftId, updated);
                    updateCall.enqueue(new Callback<Mail>() {
                        @Override
                        public void onResponse(Call<Mail> call, Response<Mail> response) {
                            if (response.isSuccessful()) {
                                sendDraftAsMail(draftId);
                            } else {
                                Snackbar.make(rootView, "Failed to update draft before sending", Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Mail> call, Throwable t) {
                            Snackbar.make(rootView, "Error updating draft: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                } else {
                    sendMail(recipients, subject, body);
                }
            }
        });
    }

    private void sendMail(List<String> recipients, String subject, String body) {
        Mail mail = new Mail();
        mail.setReceiver(recipients);
        mail.setSubject(subject.isEmpty() ? "(No subject)" : subject);
        mail.setContent(body);

        MailApi api = RetrofitClient.getInstance(this).create(MailApi.class);
        Call<Mail> call = api.sendMail(mail);

        call.enqueue(new Callback<Mail>() {
            @Override
            public void onResponse(Call<Mail> call, Response<Mail> response) {
                if (response.isSuccessful()) {
                    Mail sentMail = response.body();
                    MailEntity entity = new MailEntity(
                            sentMail.getId(),
                            sentMail.getSender(),
                            sentMail.getReceiver(),
                            sentMail.getSubject(),
                            sentMail.getContent(),
                            sentMail.getDate(),
                            sentMail.getLabels(),
                            sentMail.getType(),
                            sentMail.isRead(),
                            sentMail.isStarred()
                    );
                    MailRepository repo = new MailRepository(getApplication());
                    repo.insert(entity);

                    Snackbar.make(rootView, "Mail sent successfully", Snackbar.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 404) {
                    Snackbar.make(rootView, "One or more recipients not found", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(rootView, "Failed to send mail (" + response.code() + ")", Snackbar.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<Mail> call, Throwable t) {
                Snackbar.make(rootView, "Error: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void sendDraftAsMail(String draftId) {
        MailApi api = RetrofitClient.getInstance(this).create(MailApi.class);
        Call<Mail> call = api.sendDraftAsMailWithResponse(draftId);

        call.enqueue(new Callback<Mail>() {
            @Override
            public void onResponse(Call<Mail> call, Response<Mail> response) {
                if (response.isSuccessful()) {
                    Mail sentMail = response.body();

                    MailEntity entity = new MailEntity(
                            sentMail.getId(),
                            sentMail.getSender(),
                            sentMail.getReceiver(),
                            sentMail.getSubject(),
                            sentMail.getContent(),
                            sentMail.getDate(),
                            sentMail.getLabels(),
                            sentMail.getType(),
                            sentMail.isRead(),
                            sentMail.isStarred()
                    );

                    MailRepository repo = new MailRepository(getApplication());

                    repo.removeMailFromAllFolders(draftId);

                    repo.insert(entity);
                    repo.insertFolderRef(sentMail.getId(), "inbox");
                    repo.insertFolderRef(sentMail.getId(), "sent");

                    Snackbar.make(rootView, "Draft sent successfully", Snackbar.LENGTH_SHORT).show();
                    finish();
                } else {
                    Snackbar.make(rootView, "Failed to send draft (" + response.code() + ")", Snackbar.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Mail> call, Throwable t) {
                Snackbar.make(rootView, "Error: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void saveDraft() {
        String toInput = etTo.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();
        String body = etBody.getText().toString().trim();

        if (toInput.isEmpty() && subject.isEmpty() && body.isEmpty()) {
            finish();
            return;
        }

        List<String> recipients = Arrays.stream(toInput.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .collect(Collectors.toList());

        Mail draft = new Mail();
        draft.setReceiver(recipients);
        draft.setSubject(subject.isEmpty() ? "(No subject)" : subject);
        draft.setContent(body);
        draft.setType("draft");

        MailApi api = RetrofitClient.getInstance(this).create(MailApi.class);

        if (isDraft && draftId != null) {
            Call<Mail> call = api.updateDraft(draftId, draft);
            call.enqueue(new Callback<Mail>() {
                @Override
                public void onResponse(Call<Mail> call, Response<Mail> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Mail updated = response.body();

                        MailEntity entity = new MailEntity(
                                updated.getId(),
                                updated.getSender(),
                                updated.getReceiver(),
                                updated.getSubject(),
                                updated.getContent(),
                                updated.getDate(),
                                updated.getLabels(),
                                updated.getType(),
                                updated.isRead(),
                                updated.isStarred()
                        );

                        MailRepository repo = new MailRepository(getApplication());
                        repo.insert(entity);

                        Snackbar.make(rootView, "Draft updated", Snackbar.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Snackbar.make(rootView, "Failed to update draft (" + response.code() + ")", Snackbar.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<Mail> call, Throwable t) {
                    Snackbar.make(rootView, "Error: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                    finish();
                }
            });
        } else {
            Call<Mail> call = api.createDraft(draft);
            call.enqueue(new Callback<Mail>() {
                @Override
                public void onResponse(Call<Mail> call, Response<Mail> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Mail savedDraft = response.body();
                        draftId = savedDraft.getId(); // עדכון ה־draftId האמיתי
                        isDraft = true;

                        MailEntity entity = new MailEntity(
                                savedDraft.getId(),
                                savedDraft.getSender(),
                                savedDraft.getReceiver(),
                                savedDraft.getSubject(),
                                savedDraft.getContent(),
                                savedDraft.getDate(),
                                savedDraft.getLabels(),
                                savedDraft.getType(),
                                savedDraft.isRead(),
                                savedDraft.isStarred()
                        );
                        MailRepository repo = new MailRepository(getApplication());
                        repo.insert(entity);
                        repo.insertFolderRef(savedDraft.getId(), "drafts");

                        Snackbar.make(rootView, "Draft saved", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(rootView, "Failed to save draft (" + response.code() + ")", Snackbar.LENGTH_LONG).show();
                    }
                    finish();
                }


                @Override
                public void onFailure(Call<Mail> call, Throwable t) {
                    Snackbar.make(rootView, "Error: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                    finish();
                }
            });
        }
    }
}


