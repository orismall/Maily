package com.example.maily;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v ->{
            Intent i = new Intent(this, SignupActivity.class );
            startActivity(i);
        });
    }
}