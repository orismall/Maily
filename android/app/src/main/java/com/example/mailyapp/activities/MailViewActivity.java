package com.example.mailyapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
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
    private ImageButton backButton, trashButton, restoreButton, spamButton, readUnreadButton, moreOptionsButtonTop;
    private ImageButton replyInlineButton, moreOptionsButtonInline, starButton;
    private MaterialButton replyButton, forwardButton;
    private Mail mail;
    private MailViewModel viewModel;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_view);
        labelViewModel = new ViewModelProvider(this).get(LabelViewModel.class);
        mailViewModel = new ViewModelProvider(this).get(MailViewModel.class);

        currentMail = (Mail) getIntent().getSerializableExtra("mail");

        viewModel = new ViewModelProvider(this).get(MailViewModel.class);

        // Bind views
        backButton = findViewById(R.id.btnBack);
        trashButton = findViewById(R.id.btnTrash);
        restoreButton = findViewById(R.id.btnRestore);
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
        boolean isTrash = mail != null && "trash".equalsIgnoreCase(mail.getType());

        if (mail != null) {
            subjectTextView.setText(mail.getSubject());
            fromTextView.setText(mail.getSender());
            bodyTextView.setText(mail.getContent());

            if (!mail.isRead()) {
                mail.setRead(true);
                viewModel.updateReadFlag(mail.getId(), true);
            }

            if (mail.isStarred()) {
                starButton.setImageResource(R.drawable.ic_star_filled);
            } else {
                starButton.setImageResource(R.drawable.ic_star);
            }

        }

        if (isTrash) {
            restoreButton.setVisibility(View.VISIBLE);
            restoreButton.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Restore Mail")
                        .setMessage("Are you sure you want to restore this mail?")
                        .setPositiveButton("Restore", (dialog, which) -> {
                            mailViewModel.restoreFromTrash(mail.getId(),
                                    () -> runOnUiThread(() -> {
                                        Toast.makeText(this, "Mail restored", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }),
                                    error -> runOnUiThread(() -> {
                                        Toast.makeText(this, "Failed to restore mail", Toast.LENGTH_SHORT).show();
                                    })
                            );
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

        } else {
            restoreButton.setVisibility(View.GONE);
        }


        spamButton.setOnClickListener(v -> {
            if (mail == null) return;
            if (isTrash) {
                Toast.makeText(this, "Cannot spam a trashed mail.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Handle spam ------------------ //
        });

        starButton.setOnClickListener(v -> {
            if (mail == null) return;

            if ("trash".equalsIgnoreCase(mail.getType())) {
                Toast.makeText(this, "Cannot star a trashed mail.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean newState = !mail.isStarred();
            mail.setStarred(newState);
            mailViewModel.updateStarredFlag(mail.getId(), newState);

            starButton.setImageResource(newState ? R.drawable.ic_star_filled : R.drawable.ic_star);
        });


        replyButton.setOnClickListener(v -> {
            if (mail == null) return;
            if (isTrash) {
                Toast.makeText(this, "Cannot reply to a trashed mail.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MailViewActivity.this, ComposeMailActivity.class);
            intent.putExtra("isReply", true);
            intent.putExtra("originalSender", mail.getSender());
            intent.putExtra("originalSubject", mail.getSubject());
            intent.putExtra("originalBody", mail.getContent());
            startActivity(intent);
        });

        replyInlineButton.setOnClickListener(v -> {
            if (mail == null) return;
            if (isTrash) {
                Toast.makeText(this, "Cannot reply to a trashed mail.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MailViewActivity.this, ComposeMailActivity.class);
            intent.putExtra("isReply", true);
            intent.putExtra("originalSender", mail.getSender());
            intent.putExtra("originalSubject", mail.getSubject());
            intent.putExtra("originalBody", mail.getContent());
            startActivity(intent);
        });


        forwardButton.setOnClickListener(view -> {
            if (mail == null) return;
            if (isTrash) {
                Toast.makeText(this, "Cannot forward a trashed mail.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MailViewActivity.this, ComposeMailActivity.class);
            intent.putExtra("isForward", true);
            intent.putExtra("originalSubject", mail.getSubject());
            intent.putExtra("originalBody", mail.getContent());
            startActivity(intent);
        });

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

            popupWindow.setBackgroundDrawable(null);
            popupWindow.setElevation(20f);

            TextView replyOption = popupView.findViewById(R.id.optionReply);
            TextView forwardOption = popupView.findViewById(R.id.optionForward);

            replyOption.setOnClickListener(view -> {
                if (mail == null) return;
                if (isTrash) {
                    Toast.makeText(this, "Cannot reply to a trashed mail.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MailViewActivity.this, ComposeMailActivity.class);
                intent.putExtra("isReply", true);
                intent.putExtra("originalSender", mail.getSender());
                intent.putExtra("originalSubject", mail.getSubject());
                intent.putExtra("originalBody", mail.getContent());
                startActivity(intent);
                popupWindow.dismiss();
            });


            forwardOption.setOnClickListener(view -> {
                if (mail == null) return;
                if (isTrash) {
                    Toast.makeText(this, "Cannot forward a trashed mail.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MailViewActivity.this, ComposeMailActivity.class);
                intent.putExtra("isForward", true);
                intent.putExtra("originalSubject", mail.getSubject());
                intent.putExtra("originalBody", mail.getContent());
                startActivity(intent);
                popupWindow.dismiss();
            });


            popupWindow.showAsDropDown(moreOptionsButtonInline, -30, 0);
        });

        trashButton.setOnClickListener(v -> {
            if ("trash".equalsIgnoreCase(currentMail.getType())) {
                // 🗑 Permanently delete
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Delete Permanently")
                        .setMessage("Are you sure you want to permanently delete this mail?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            mailViewModel.permanentlyDeleteFromTrash(currentMail.getId(),
                                    () -> runOnUiThread(() -> {
                                        Toast.makeText(this, "Mail permanently deleted", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }),
                                    error -> runOnUiThread(() -> {
                                        Toast.makeText(this, "Failed to delete mail", Toast.LENGTH_SHORT).show();
                                    })
                            );
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // 🗃 Move to trash
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Move to Trash")
                        .setMessage("Are you sure you want to move this mail to trash?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            mailViewModel.moveToTrash(currentMail.getId(),
                                    () -> runOnUiThread(() -> {
                                        Toast.makeText(this, "Moved to trash", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }),
                                    error -> runOnUiThread(() -> {
                                        Toast.makeText(this, "Failed to move to trash", Toast.LENGTH_SHORT).show();
                                    })
                            );
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

    }

    private void showLabelPopup() {
        if ("trash".equalsIgnoreCase(currentMail.getType())) {
            Toast.makeText(this, "Cannot label a trashed mail.", Toast.LENGTH_SHORT).show();
            return;
        }
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
