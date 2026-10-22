package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.R;

public class OpcoesRendaFixaActivity extends AppCompatActivity {

    ImageButton btnVoltarRendaFixa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcoes_renda_fixa);

        btnVoltarRendaFixa = findViewById(R.id.btnVoltarRendaFixa);

        btnVoltarRendaFixa.setOnClickListener(v -> finish());

    }
}