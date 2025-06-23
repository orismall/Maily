package com.example.mailyapp.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mailyapp.R;
import com.example.mailyapp.models.Mail;
import com.google.android.material.button.MaterialButton;

public class MailViewActivity extends AppCompatActivity {

    private TextView subjectTextView, fromTextView, bodyTextView;
    private ImageButton backButton, trashButton, spamButton, readUnreadButton, moreOptionsButtonTop;
    private ImageButton replyInlineButton, moreOptionsButtonInline, starButton;
    private MaterialButton replyButton, forwardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_view);

        // Bind views
        backButton = findViewById(R.id.btnBack);
        trashButton = findViewById(R.id.btnTrash);
        spamButton = findViewById(R.id.btnSpam);
        readUnreadButton = findViewById(R.id.btnReadUnread);
        moreOptionsButtonTop = findViewById(R.id.btnMoreTop);
        subjectTextView = findViewById(R.id.mailSubject);
        starButton = findViewById(R.id.btnStar);
        fromTextView = findViewById(R.id.mailFrom);
        replyInlineButton = findViewById(R.id.btnReplyInline);
        moreOptionsButtonInline = findViewById(R.id.btnMoreInline);
        bodyTextView = findViewById(R.id.mailBody);
        replyButton = findViewById(R.id.btnReply);
        forwardButton = findViewById(R.id.btnForward);

        // Load mail data
        Mail mail = (Mail) getIntent().getSerializableExtra("mail");
        if (mail != null) {
            subjectTextView.setText(mail.getSubject());
            fromTextView.setText(mail.getSender());
            bodyTextView.setText(mail.getContent());
        }

        // Basic back button behavior
        backButton.setOnClickListener(v -> finish());
    }
}
