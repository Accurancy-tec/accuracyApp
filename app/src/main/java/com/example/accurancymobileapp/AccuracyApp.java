package com.example.accurancymobileapp;

import android.app.Application;

import com.example.accurancymobileapp.utils.TemaApp;

/** Ponto de entrada do app: aplica o tema escolhido antes de qualquer tela abrir. */
public class AccuracyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TemaApp.aplicarSalvo(this);
    }
}
