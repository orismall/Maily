package com.example.mailyapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        Mail mail = mailList.get(position);

        holder.senderTextView.setText(mail.getSender());
        holder.subjectTextView.setText(mail.getSubject());
        String snippet = mail.getContent();
        if (snippet.length() > 60) {
            snippet = snippet.substring(0, 60) + "...";
        }
        holder.snippetTextView.setText(snippet);
        holder.dateTextView.setText(mail.getDate());

        // Make subject bold if mail is considered unread (label -1)
        // Later replace with mail.isRead()
        if (mail.getLabels() != null && mail.getLabels().contains(-1)) {
            holder.subjectTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            holder.subjectTextView.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        holder.itemView.setOnClickListener(v -> listener.onMailClick(mail));
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

        public MailViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.mailSender);
            subjectTextView = itemView.findViewById(R.id.mailSubject);
            snippetTextView = itemView.findViewById(R.id.mailSnippet);
            dateTextView = itemView.findViewById(R.id.mailDate);
        }
    }
}
