package com.example.mailyapp.activities;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mailyapp.R;
import com.example.mailyapp.entities.LabelEntity;
import com.example.mailyapp.models.Mail;
import com.example.mailyapp.viewmodels.LabelViewModel;
import com.example.mailyapp.viewmodels.MailViewModel;
import com.google.android.material.button.MaterialButton;

import android.widget.Toast;

public class MailViewActivity extends AppCompatActivity {

    private MailViewModel mailViewModel;
    private LabelViewModel labelViewModel;
    private Mail currentMail;

    private TextView subjectTextView, fromTextView, bodyTextView;
    private ImageButton backButton, trashButton, spamButton, readUnreadButton, moreOptionsButtonTop;
    private ImageButton replyInlineButton, moreOptionsButtonInline, starButton;
    private MaterialButton replyButton, forwardButton;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_view);
        labelViewModel = new ViewModelProvider(this).get(LabelViewModel.class);
        mailViewModel = new ViewModelProvider(this).get(MailViewModel.class);

        currentMail = (Mail) getIntent().getSerializableExtra("mail");

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
        if (currentMail != null) {
            subjectTextView.setText(currentMail.getSubject());
            fromTextView.setText(currentMail.getSender());
            bodyTextView.setText(currentMail.getContent());
        }

        backButton.setOnClickListener(v -> finish());
        moreOptionsButtonTop.setOnClickListener(v -> showLabelPopup());

        moreOptionsButtonInline.setOnClickListener(v -> {
            View popupView = getLayoutInflater().inflate(R.layout.popup_inline_menu, null);
            PopupWindow popupWindow = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            popupWindow.setBackgroundDrawable(null);
            popupWindow.setElevation(20f);

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

            popupWindow.showAsDropDown(moreOptionsButtonInline, -30, 0);
        });

        trashButton.setOnClickListener(v -> {
            mailViewModel.moveToTrash(currentMail.getId(),
                    () -> runOnUiThread(() -> {
                        Toast.makeText(this, "Moved to trash", Toast.LENGTH_SHORT).show();
                        finish();
                    }),
                    error -> runOnUiThread(() -> {
                        Toast.makeText(this, "Failed to move to trash", Toast.LENGTH_SHORT).show();
                    })
            );
        });
    }

    private void showLabelPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.mail_more_menu, null);
        LinearLayout container = popupView.findViewById(R.id.label_container);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        labelViewModel.getAllLabels().observe(this, labelEntities -> {
            container.removeAllViews();

            for (LabelEntity label : labelEntities) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(label.getName());
                checkBox.setChecked(label.getMailIds().contains(currentMail.getId()));

                checkBox.setTextColor(Color.BLACK);
                checkBox.setButtonTintList(ColorStateList.valueOf(Color.DKGRAY));
                checkBox.setPadding(24, 12, 24, 12);
                checkBox.setTextSize(16);
                checkBox.setTypeface(Typeface.DEFAULT_BOLD);
                checkBox.setBackgroundResource(android.R.drawable.list_selector_background);

                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        labelViewModel.addMailToLabel(currentMail.getId(), label.getId(), mailViewModel,
                                () -> runOnUiThread(() -> Toast.makeText(this, "Labeled: " + label.getName(), Toast.LENGTH_SHORT).show()),
                                err -> runOnUiThread(() -> Toast.makeText(this, "Failed to label", Toast.LENGTH_SHORT).show())
                        );
                    } else {
                        labelViewModel.removeMailFromLabel(currentMail.getId(), label.getId(), mailViewModel,
                                () -> runOnUiThread(() -> Toast.makeText(this, "Unlabeled: " + label.getName(), Toast.LENGTH_SHORT).show()),
                                err -> runOnUiThread(() -> Toast.makeText(this, "Failed to remove label", Toast.LENGTH_SHORT).show())
                        );
                    }
                });

                container.addView(checkBox);
            }
        });

        popupWindow.showAsDropDown(moreOptionsButtonTop, -16, 0);
    }
}
