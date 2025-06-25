package com.example.mailyapp.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.example.mailyapp.R;
import com.example.mailyapp.models.User;
import com.example.mailyapp.viewmodels.UserViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class SignupActivity extends BaseActivity {

    private EditText firstNameInput, lastNameInput, emailInput, passwordInput, confirmPasswordInput, birthDateInput;
    private Spinner genderSpinner;
    private Button selectImageButton, registerButton;
    private ImageView selectedImageView;

    private Uri selectedImageUri = null;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        // Find all UI elements by ID
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        birthDateInput = findViewById(R.id.birthDateInput);
        genderSpinner = findViewById(R.id.genderSpinner);
        selectImageButton = findViewById(R.id.selectImageButton);
        registerButton = findViewById(R.id.registerButton);
        selectedImageView = findViewById(R.id.selectedImageView);

        // Init Spinner and Date Picker
        setupGenderSpinner();
        setupBirthDatePicker();
        selectedImageView.setVisibility(View.GONE);

        // Init the new image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            selectedImageView.setImageURI(selectedImageUri);
                            selectedImageView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        selectedImageUri = null;
                        selectedImageView.setImageDrawable(null);
                        selectedImageView.setVisibility(View.GONE);
                    }
                }
        );

        // Select image button logic (modern)
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Register button click
        registerButton.setOnClickListener(v -> handleRegistration());
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getRegistrationSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("showSuccessToast", true);
                startActivity(intent);
                finish();
            }
        });

        userViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showErrorToast(error);            }
        });
    }

    private void setupGenderSpinner() {
        String[] genders = {"Select Gender", "Male", "Female", "Other"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genders) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextSize(17);
                tv.setTextColor(position == 0 ? Color.parseColor("#757575") : Color.BLACK);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
    }

    private void setupBirthDatePicker() {
        birthDateInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    SignupActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                        birthDateInput.setText(date);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });
    }

    private String imageUriToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = getBytes(inputStream);

            String mimeType = getContentResolver().getType(imageUri);
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }

            String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP); // NO_WRAP כדי לא ליצור שורות חדשות
            return "data:" + mimeType + ";base64," + base64;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void handleRegistration() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String gender = genderSpinner.getSelectedItem().toString();
        String birthDate = birthDateInput.getText().toString().trim();
        String image = selectedImageUri != null ? imageUriToBase64(selectedImageUri) : null;

        User user = new User(firstName, lastName, email, password, confirmPassword, gender, birthDate, image);
        userViewModel.registerUser(this, user);
    }
}