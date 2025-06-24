package com.example.mailyapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.mailyapp.R;
import com.example.mailyapp.data.AppDatabase;
import com.example.mailyapp.data.LabelDao;
import com.example.mailyapp.entities.LabelEntity;

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
                .allowMainThreadQueries() // For simplicity
                .build();

        labelDao = db.labelDao();

        ImageButton btnClose = findViewById(R.id.btnCloseLabel);
        ImageButton btnSave = findViewById(R.id.btnSave);
        btnClose.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> {
            String labelName = etLabelName.getText().toString()/*.trim()*/;
            if (labelName.isEmpty()) {
                Toast.makeText(this, "Label name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            LabelEntity newLabel = new LabelEntity(labelName);
            labelDao.insert(newLabel);
            Toast.makeText(this, "Label created", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
