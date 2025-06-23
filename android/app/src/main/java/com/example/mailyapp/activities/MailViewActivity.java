package com.example.mailyapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mailyapp.R;
import com.example.mailyapp.models.Mail;
import com.google.android.material.button.MaterialButton;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

public class MailViewActivity extends AppCompatActivity {

    private TextView subjectTextView, fromTextView, bodyTextView;
    private ImageButton backButton, trashButton, spamButton, readUnreadButton, moreOptionsButtonTop;
    private ImageButton replyInlineButton, moreOptionsButtonInline, starButton;
    private MaterialButton replyButton, forwardButton;

    @SuppressLint("NonConstantResourceId")
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
        // Basic back button behavior
        backButton.setOnClickListener(v -> finish());

// Popup menu on top-right 3-dots button
        moreOptionsButtonTop.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MailViewActivity.this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.mail_more_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                String label = item.getTitle().toString(); // Get the label text dynamically
                Toast.makeText(this, "Added to " + label, Toast.LENGTH_SHORT).show();

                // Optional: Save or apply the label to the mail object here

                return true;
            });


            popup.show();
        });

        moreOptionsButtonInline.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MailViewActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.mail_inline_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_reply) {
                    Toast.makeText(this, "Reply selected", Toast.LENGTH_SHORT).show();
                    // Optionally call: replyToMail();
                    return true;
                } else if (id == R.id.action_forward) {
                    Toast.makeText(this, "Forward selected", Toast.LENGTH_SHORT).show();
                    // Optionally call: forwardMail();
                    return true;
                }
                return false;
            });

            popup.show();
        });


    }
}
