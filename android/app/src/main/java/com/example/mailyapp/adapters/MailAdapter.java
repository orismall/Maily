package com.example.mailyapp.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;
import com.example.mailyapp.R;
import com.example.mailyapp.models.Mail;

import java.util.List;


public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    private List<Mail> mailList;
    private OnMailClickListener listener;
    private String searchQuery = null;

    // Interface to handle mail click events
    public interface OnMailClickListener {
        void onMailClick(Mail mail);

        void onToggleStar(String mailId, boolean isStarred);
    }

    // Constructor for MailAdapter
    public MailAdapter(List<Mail> mailList, OnMailClickListener listener) {
        this.mailList = mailList;
        this.listener = listener;
    }

    // Creates a new ViewHolder by inflating the item layout
    @NonNull
    @Override
    public MailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mail, parent, false);
        return new MailViewHolder(itemView);
    }

    // Binds data from a Mail object to the views in the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull MailViewHolder holder, int position) {
        if (mailList == null || position >= mailList.size()) {
            holder.senderTextView.setText("(Error)");
            return;
        }

        Mail mail = mailList.get(position);

        String sender = mail.getSender();
        holder.senderTextView.setText(highlightQuery(sender != null ? sender : "(No sender)"));

        String subject = mail.getSubject();
        holder.subjectTextView.setText(highlightQuery(subject != null ? subject : "(No subject)"));

        String snippet = mail.getContent();
        if (snippet != null && snippet.length() > 60) {
            snippet = snippet.substring(0, 60) + "...";
        }
        holder.snippetTextView.setText(highlightQuery(snippet != null ? snippet : ""));

        String date = mail.getDate();
        if (date != null) {
            try {
                SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                serverFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                Date parsedDate = serverFormat.parse(date);

                SimpleDateFormat israeliFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", new Locale("he", "IL"));
                israeliFormat.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Jerusalem"));
                String formattedDate = israeliFormat.format(parsedDate);

                holder.dateTextView.setText(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
                holder.dateTextView.setText(date);
            }
        } else {
            holder.dateTextView.setText("");
        }
        if (!mail.isRead()) {
            holder.subjectTextView.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.snippetTextView.setTypeface(null, Typeface.BOLD);
            holder.itemView.setBackgroundColor(Color.parseColor("#ECEFF1"));
            holder.subjectTextView.setTextSize(15);
        } else {
            holder.subjectTextView.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.snippetTextView.setTypeface(null, Typeface.NORMAL);
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.subjectTextView.setTextSize(14);
        }
        holder.itemView.setOnClickListener(v -> listener.onMailClick(mail));

        if (mail.isStarred()) {
            holder.starIcon.setImageResource(R.drawable.ic_star_filled);
        } else {
            holder.starIcon.setImageResource(R.drawable.ic_star);
        }

        holder.starIcon.setOnClickListener(v -> {
            if ("trash".equalsIgnoreCase(mail.getType())) {
                listener.onToggleStar(mail.getId(), mail.isStarred()); // notify activity to show toast
                return;
            }
            boolean newState = !mail.isStarred();
            mail.setStarred(newState);
            listener.onToggleStar(mail.getId(), newState);
            notifyItemChanged(holder.getAdapterPosition());
        });


    }

    public Mail getMailById(String mailId) {
        if (mailList == null) return null;
        for (Mail mail : mailList) {
            if (mail.getId().equals(mailId)) {
                return mail;
            }
        }
        return null;
    }


    // Returns the total number of mails
    @Override
    public int getItemCount() {
        return mailList != null ? mailList.size() : 0;
    }

    // Allows updating the mail list dynamically
    public void updateMailList(List<Mail> updatedList) {
        this.mailList = updatedList;
        notifyDataSetChanged();
    }

    // ViewHolder class holds references to the views of a single mail item
    public static class MailViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, subjectTextView, snippetTextView, dateTextView;
        ImageView starIcon;

        public MailViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.mailSender);
            subjectTextView = itemView.findViewById(R.id.mailSubject);
            snippetTextView = itemView.findViewById(R.id.mailSnippet);
            dateTextView = itemView.findViewById(R.id.mailDate);
            starIcon = itemView.findViewById(R.id.starIcon);
        }
    }

    public void updateData(List<Mail> newMails) {
        this.mailList = newMails;
        notifyDataSetChanged();
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query != null ? query.toLowerCase() : null;
        notifyDataSetChanged();
    }

    private CharSequence highlightQuery(String originalText) {
        if (searchQuery == null || searchQuery.isEmpty() || originalText == null) {
            return originalText;
        }

        String lowerText = originalText.toLowerCase();
        SpannableString spannable = new SpannableString(originalText);

        int index = lowerText.indexOf(searchQuery);
        while (index >= 0) {
            BackgroundColorSpan yellowHighlight = new BackgroundColorSpan(Color.YELLOW);
            spannable.setSpan(yellowHighlight, index, index + searchQuery.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            index = lowerText.indexOf(searchQuery, index + searchQuery.length());
        }

        return spannable;
    }
}