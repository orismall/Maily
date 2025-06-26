package com.example.mailyapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mailyapp.R;
import com.example.mailyapp.models.Mail;
import com.example.mailyapp.viewmodels.MailViewModel;
import com.google.android.material.button.MaterialButton;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class MailViewActivity extends AppCompatActivity {
    private final Set<Integer> selectedLabelIds = new HashSet<>();

    private TextView subjectTextView, fromTextView, bodyTextView;
    private ImageButton backButton, trashButton, spamButton, readUnreadButton, moreOptionsButtonTop;
    private ImageButton replyInlineButton, moreOptionsButtonInline, starButton;
    private MaterialButton replyButton, forwardButton;
    private Mail mail;
    private MailViewModel viewModel;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_view);

        viewModel = new ViewModelProvider(this).get(MailViewModel.class);

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
        mail = (Mail) getIntent().getSerializableExtra("mail");
        if (mail != null) {
            subjectTextView.setText(mail.getSubject());
            fromTextView.setText(mail.getSender());
            bodyTextView.setText(mail.getContent());

            if (!mail.isRead()) {
                mail.setRead(true);
                viewModel.updateReadFlag(mail.getId(), true);
            }
        }

        readUnreadButton.setOnClickListener(v -> {
            if (mail == null) return;

            boolean newReadStatus = !mail.isRead();
            mail.setRead(newReadStatus);
            viewModel.updateReadFlag(mail.getId(), newReadStatus);

            if (!newReadStatus) {
                Toast.makeText(this, "Mark as unread", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Mark as read", Toast.LENGTH_SHORT).show();
            }
        });

        // Basic back button behavior
        backButton.setOnClickListener(v -> finish());

        moreOptionsButtonTop.setOnClickListener(v -> showLabelPopup());

        moreOptionsButtonInline.setOnClickListener(v -> {
            View popupView = getLayoutInflater().inflate(R.layout.popup_inline_menu, null);
            PopupWindow popupWindow = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            // Optional: Background for touch outside to dismiss
            popupWindow.setBackgroundDrawable(null);
            popupWindow.setElevation(20f); // or 8dp

            // Find options
            TextView replyOption = popupView.findViewById(R.id.optionReply);
            TextView forwardOption = popupView.findViewById(R.id.optionForward);

            replyOption.setOnClickListener(view -> {
                Toast.makeText(this, "Reply selected", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });

            forwardOption.setOnClickListener(view -> {
                Toast.makeText(this, "Forward selected", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });

            // Show anchored to the 3-dots button
            popupWindow.showAsDropDown(moreOptionsButtonInline, -30, 0);
        });


    }

    private void showLabelPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.menu_label_popup, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true); // focusable = true so it dismisses when clicking outside

        // Find checkboxes
        CheckBox workCheck = popupView.findViewById(R.id.label_work);
        CheckBox personalCheck = popupView.findViewById(R.id.label_personal);
        CheckBox importantCheck = popupView.findViewById(R.id.label_important);

        // Restore current states
        workCheck.setChecked(selectedLabelIds.contains(R.id.label_work));
        personalCheck.setChecked(selectedLabelIds.contains(R.id.label_personal));
        importantCheck.setChecked(selectedLabelIds.contains(R.id.label_important));

        // Handle toggle
        View.OnClickListener listener = v -> {
            CheckBox cb = (CheckBox) v;
            int id = cb.getId();
            if (cb.isChecked()) {
                selectedLabelIds.add(id);
                Toast.makeText(this, "Labeled: " + cb.getText(), Toast.LENGTH_SHORT).show();
            } else {
                selectedLabelIds.remove(id);
                Toast.makeText(this, "Unlabeled: " + cb.getText(), Toast.LENGTH_SHORT).show();
            }
        };

        workCheck.setOnClickListener(listener);
        personalCheck.setOnClickListener(listener);
        importantCheck.setOnClickListener(listener);

        // Show anchored to 3-dots button
        popupWindow.setElevation(8);
        popupWindow.showAsDropDown(moreOptionsButtonTop, -16, 0);
    }

}
