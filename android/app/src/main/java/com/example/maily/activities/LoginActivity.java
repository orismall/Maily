package com.example.maily.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.maily.R;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        TextView registerLink = findViewById(R.id.registerLink);
        registerLink.setOnClickListener(v -> {
            Intent i = new Intent(this, SignupActivity.class);
            startActivity(i);
        });
    }
}