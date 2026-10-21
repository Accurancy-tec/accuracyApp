package com.example.accurancymobileapp.activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.R;

public class FIIsActivity extends AppCompatActivity {

    ImageButton btnVoltarFIIs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiis);

        btnVoltarFIIs = findViewById(R.id.btnVoltarFIIs);

        btnVoltarFIIs.setOnClickListener(v -> finish());
    }
}