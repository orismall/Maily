package com.example.mailyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mailyapp.R;
import com.example.mailyapp.entities.MailEntity;
import com.example.mailyapp.models.Mail;
import com.example.mailyapp.viewmodels.MailViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComposeMailActivity extends AppCompatActivity {

    private EditText etTo, etSubject, etBody;
    private View rootView;
    private boolean isDraft = false;
    private String draftId = null;
    private MailViewModel mailViewModel;
    private String currentFolder = "inbox"; // default fallback

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        rootView = findViewById(android.R.id.content);
        mailViewModel = new ViewModelProvider(this).get(MailViewModel.class);

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
        currentFolder = intent.getStringExtra("folder"); // <-- fetch folder from intent
        boolean isReply = intent.getBooleanExtra("isReply", false);
        boolean isForward = intent.getBooleanExtra("isForward", false);
        isDraft = intent.getBooleanExtra("isDraft", false);

        if (isReply) {
            String originalSender = intent.getStringExtra("originalSender");
            String originalSubject = intent.getStringExtra("originalSubject");
            String originalBody = intent.getStringExtra("originalBody");

            etTo.setText(originalSender != null ? originalSender : "");
            etSubject.setText(originalSubject != null && originalSubject.startsWith("Re:") ? originalSubject : "Re: " + originalSubject);
            etBody.setText("\n\n--- Original message ---\n" + (originalBody != null ? originalBody : ""));
        } else if (isForward) {
            String originalSubject = intent.getStringExtra("originalSubject");
            String originalBody = intent.getStringExtra("originalBody");

            etSubject.setText(originalSubject != null && originalSubject.startsWith("Fwd:") ? originalSubject : "Fwd: " + originalSubject);
            etBody.setText("\n\n--- Forwarded message ---\n" + (originalBody != null ? originalBody : ""));
        } else if (isDraft) {
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
                    Mail updated = new Mail();
                    updated.setReceiver(recipients);
                    updated.setSubject(subject.isEmpty() ? "(No subject)" : subject);
                    updated.setContent(body);
                    updated.setType("draft");

                    mailViewModel.updateDraft(draftId, updated, new Callback<Mail>() {
                        @Override
                        public void onResponse(Call<Mail> call, Response<Mail> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Mail updated = response.body();
                                mailViewModel.insert(toEntity(updated));
                                sendDraftAsMail(updated, updated.getId());
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

        mailViewModel.sendMail(mail, new Callback<Mail>() {
            @Override
            public void onResponse(Call<Mail> call, Response<Mail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Mail sentMail = response.body();
                    mailViewModel.insert(toEntity(sentMail));

                    mailViewModel.fetchFolder("sent", 1);
                    if ("spam".equalsIgnoreCase(sentMail.getType())) {
                        mailViewModel.fetchFolder("spam", 1);
                    } else {
                        mailViewModel.fetchFolder("inbox", 1);
                    }
                  
                    Snackbar.make(rootView, "Mail sent successfully", Snackbar.LENGTH_SHORT).show();
                    goBackToInboxActivity();
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

    private void sendDraftAsMail(Mail mail, String draftId) {
        mailViewModel.sendMail(mail, new Callback<Mail>() {
            @Override
            public void onResponse(Call<Mail> call, Response<Mail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Mail sentMail = response.body();
                    mailViewModel.insert(toEntity(sentMail));
                    mailViewModel.removeMailFromAllFolders(draftId);
                    Snackbar.make(rootView, "Draft sent successfully", Snackbar.LENGTH_SHORT).show();
                  
                    mailViewModel.fetchFolder("sent", 1);
                    if ("spam".equalsIgnoreCase(sentMail.getType())) {
                        mailViewModel.fetchFolder("spam", 1);
                    } else {
                        mailViewModel.fetchFolder("inbox", 1);
                    }
                    goBackToInboxActivity();

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
            goBackToInboxActivity();
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

        if (isDraft && draftId != null) {
            mailViewModel.updateDraft(draftId, draft, new Callback<Mail>() {
                @Override
                public void onResponse(Call<Mail> call, Response<Mail> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        mailViewModel.insert(toEntity(response.body()));
                        Snackbar.make(rootView, "Draft updated", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(rootView, "Failed to update draft (" + response.code() + ")", Snackbar.LENGTH_LONG).show();
                    }
                    goBackToInboxActivity();
                }

                @Override
                public void onFailure(Call<Mail> call, Throwable t) {
                    Snackbar.make(rootView, "Error: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                    goBackToInboxActivity();
                }
            });
        } else {
            mailViewModel.createDraft(draft, new Callback<Mail>() {
                @Override
                public void onResponse(Call<Mail> call, Response<Mail> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        draftId = response.body().getId();
                        isDraft = true;
                        mailViewModel.insert(toEntity(response.body()));
                        mailViewModel.insertFolderRef(draftId, "drafts");

                        Snackbar.make(rootView, "Draft saved", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(rootView, "Failed to save draft (" + response.code() + ")", Snackbar.LENGTH_LONG).show();
                    }
                    goBackToInboxActivity();
                }

                @Override
                public void onFailure(Call<Mail> call, Throwable t) {
                    Snackbar.make(rootView, "Error: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
                    goBackToInboxActivity();
                }
            });
        }
    }

    private void goBackToInboxActivity() {
        Intent backIntent = new Intent(ComposeMailActivity.this, InboxActivity.class);
        backIntent.putExtra("folder", currentFolder);
        backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(backIntent);
        finish();
    }

    private MailEntity toEntity(Mail mail) {
        return new MailEntity(
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
        );
    }
}
