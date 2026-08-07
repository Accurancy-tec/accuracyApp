package com.example.accurancymobileapp.activities;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.fragments.DashboardFragment;
import com.example.accurancymobileapp.fragments.WalletFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainPageActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarComponente();
        configurarNavegacao();

        if(savedInstanceState == null){
            bottomNavigation.setSelectedItemId(R.id.nav_wallet);
        }
    }

    private void inicializarComponente(){
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void configurarNavegacao(){
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragmentSelecionado;

            int itemId = item.getItemId();

            if(itemId == R.id.nav_wallet){
                fragmentSelecionado = new WalletFragment();
            }
            else if(itemId == R.id.nav_home){
                fragmentSelecionado = new DashboardFragment();
            }

            else {
                return false;
            }

            trocarFragment(fragmentSelecionado);
            return true;

        });
    }

    private void trocarFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContent, fragment)
                .commit();
    }
}