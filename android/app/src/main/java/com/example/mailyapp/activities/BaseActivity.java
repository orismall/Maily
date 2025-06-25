package com.example.mailyapp.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mailyapp.R;

public class BaseActivity extends AppCompatActivity {

    protected void showErrorToast(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.custom_toast_error, findViewById(android.R.id.content), false);
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    protected void showSuccessToast(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.custom_toast_success, findViewById(android.R.id.content), false);
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}