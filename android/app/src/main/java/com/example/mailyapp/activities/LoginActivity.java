package com.example.mailyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;

import com.example.mailyapp.R;
import com.example.mailyapp.models.LoginRequest;
import com.example.mailyapp.viewmodels.UserViewModel;

public class LoginActivity extends BaseActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        TextView registerLink = findViewById(R.id.registerLink);

        boolean showToast = getIntent().getBooleanExtra("showSuccessToast", false);
        if (showToast) {
            showSuccessToast("Registration successful!");
        }

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.btnLogin);
        registerLink = findViewById(R.id.registerLink);

        // Set up ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Observe successful login response
        userViewModel.getLoginResponse().observe(this, response -> {
            getSharedPreferences("session", MODE_PRIVATE)
                    .edit()
                    .putString("email", emailInput.getText().toString().trim())
                    .apply();

            Intent i = new Intent(this, InboxActivity.class);
            i.putExtra("token", response.getToken());
            i.putExtra("userId", response.getUserId());
            startActivity(i);
            finish();
        });

        // Observe error messages
        userViewModel.getErrorMessage().observe(this, this::showErrorToast);

        // Set login button click listener
        loginButton.setOnClickListener(v -> handleLogin());

        // Go to signup activity when clicking register link
        registerLink.setOnClickListener(v -> {
            Intent i = new Intent(this, SignupActivity.class);
            startActivity(i);
        });
    }

    private void handleLogin() {
        // Get input values
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Create request and call ViewModel
        LoginRequest request = new LoginRequest(email, password);
        userViewModel.loginUser( request);

    }
}