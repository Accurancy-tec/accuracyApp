package com.example.accurancymobileapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.LoginActivity;
import com.example.accurancymobileapp.utils.SessionManager;

public class  ProfileFragment extends Fragment {

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    LinearLayout lnlLogout;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        lnlLogout = (LinearLayout) view.findViewById(R.id.itemSair);

        Profile();
    }

    public void Profile(){
        lnlLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutSession();
            }
        });
    }

    public void logoutSession(){
        SessionManager sessionManager = new SessionManager(requireContext());

        sessionManager.logout();

        Intent it = new Intent(requireContext(), LoginActivity.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(it);

    }
}