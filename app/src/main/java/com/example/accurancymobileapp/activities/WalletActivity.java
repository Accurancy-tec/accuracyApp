package com.example.accurancymobileapp.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.fragments.AportesFragment;
import com.example.accurancymobileapp.fragments.DashboardFragment;
import com.example.accurancymobileapp.fragments.WalletFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class WalletActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_wallet);

        inicializarComponente();
        configurarNavegacao();
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
            else if(itemId == R.id.nav_aporte){
                fragmentSelecionado = new AportesFragment();
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