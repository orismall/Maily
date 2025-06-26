package com.example.mailyapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.example.mailyapp.R;
import com.example.mailyapp.data.AppDatabase;
import com.example.mailyapp.data.LabelDao;
import com.example.mailyapp.entities.LabelEntity;
import com.example.mailyapp.models.Label;
import com.example.mailyapp.viewmodels.LabelViewModel;

import java.util.ArrayList;

public class CreateLabelActivity extends AppCompatActivity {

    private EditText etLabelName;
    private LabelDao labelDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_label);

        etLabelName = findViewById(R.id.etLabelName);

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "MailyDB")
                .allowMainThreadQueries()
                .build();

        labelDao = db.labelDao();

        ImageButton btnClose = findViewById(R.id.btnCloseLabel);
        ImageButton btnSave = findViewById(R.id.btnSave);
        btnClose.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> {
            String labelName = etLabelName.getText().toString();
            if (labelName.isEmpty()) {
                Toast.makeText(this, "Label name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Label label = new Label(labelName);
            label.setName(labelName);
            label.setColor("#000000"); // or let user choose later
            label.setMailIds(new ArrayList<>()); // empty list for now

            LabelViewModel viewModel = new ViewModelProvider(this).get(LabelViewModel.class);
            viewModel.createLabel(label, createdLabel -> {
                runOnUiThread(() -> {
                    if (createdLabel != null) {
                        Toast.makeText(this, "Label created", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to create label", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

    }
}
