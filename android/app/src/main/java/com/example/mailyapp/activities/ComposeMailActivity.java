package com.example.mailyapp.activities;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mailyapp.R;

public class ComposeMailActivity extends AppCompatActivity {

    private EditText etSubject, etBody;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        ImageButton btnClose = findViewById(R.id.btnClose);
        ImageButton btnSend = findViewById(R.id.btnSend);
        etSubject = findViewById(R.id.etSubject);
        etBody = findViewById(R.id.etBody);

        btnClose.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String subject = etSubject.getText().toString().trim();
            String body = etBody.getText().toString().trim();

            // Here you would handle sending the email (e.g., API call or intent)
            Toast.makeText(this, "Sending...\nSubject: " + subject + "\nBody: " + body, Toast.LENGTH_SHORT).show();
        });
    }
}
