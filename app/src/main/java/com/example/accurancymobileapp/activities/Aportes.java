package com.example.accurancymobileapp.activities;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.accurancymobileapp.fragments.AportesFragment;
import com.example.accurancymobileapp.fragments.DashboardFragment;
import com.example.accurancymobileapp.fragments.WalletFragment;
import com.example.accurancymobileapp.model.QuoteResult;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Aportes extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_aportes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
            } else if (itemId == R.id.nav_aporte) {
                fragmentSelecionado = new AportesFragment();
            } else {
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