package com.example.mailyapp.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mailyapp.R;
import com.example.mailyapp.models.Mail;

import java.util.List;


public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MailViewHolder> {

    private List<Mail> mailList;
    private OnMailClickListener listener;

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
        holder.senderTextView.setText(sender != null ? sender : "(No sender)");

        String subject = mail.getSubject();
        holder.subjectTextView.setText(subject != null ? subject : "(No subject)");

        String snippet = mail.getContent();
        if (snippet != null && snippet.length() > 60) {
            snippet = snippet.substring(0, 60) + "...";
        }
        holder.snippetTextView.setText(snippet != null ? snippet : "");

        String date = mail.getDate();
        holder.dateTextView.setText(date != null ? date : "");

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
            boolean newState = !mail.isStarred();
            mail.setStarred(newState);
            listener.onToggleStar(mail.getId(), newState);
            notifyItemChanged(holder.getAdapterPosition());
        });

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
}