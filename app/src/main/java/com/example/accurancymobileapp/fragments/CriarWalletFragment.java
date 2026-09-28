package com.example.accurancymobileapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;


public class CriarWalletFragment extends Fragment {

    private WalletRepository walletRepository;
    private RecyclerView minhasWallets;
    private MaterialButton btnCriarWallet;

    public CriarWalletFragment() {
        super(R.layout.fragment_criar_wallet);
    }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
     super.onViewCreated(view, savedInstanceState);

     minhasWallets = view.findViewById(R.id.minhasWallets);
     btnCriarWallet = view.findViewById(R.id.btnCriarWallet);
     walletRepository = new WalletRepository(requireContext());

     configurarRecyclerView();

     btnCriarWallet.setOnClickListener(v -> {abrirDialogoCriarWallet();});
   }

   private void configurarRecyclerView(){
        minhasWallets.setLayoutManager( new LinearLayoutManager(requireContext()));

        // Colocar o adapter
   }

   private void abrirDialogoCriarWallet(){
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_criar_wallet, null);

       TextInputEditText nomeWallet = dialogView.findViewById(R.id.nomeWallet);
       AutoCompleteTextView tipoWallet = dialogView.findViewById(R.id.tipoWallet);

       String[] tiposWallet = {"Real", "Simulada"};

       ArrayAdapter<String> tipoCarteiraAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, tiposWallet);

       tipoWallet.setAdapter(tipoCarteiraAdapter);

       // Não exigir caracteres para mostrar as opções
       tipoWallet.setThreshold(0);

        // Abrir as opções ao clicar
       tipoWallet.setOnClickListener(v -> {
           tipoWallet.showDropDown();
       });


        // Também abrir quando receber foco
       tipoWallet.setOnFocusChangeListener((v, hasFocus) -> {
           if (hasFocus) {
               tipoWallet.showDropDown();
           }
       });

       AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
               .setView(dialogView)
               .setNegativeButton("Cancelar", null)
               .setPositiveButton("Criar", null)
               .create();

       dialog.setOnShowListener(dialogInterface -> {
           Button btnCriar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

           btnCriar.setOnClickListener(v -> {
               String nome = nomeWallet.getText().toString().trim();
               String tipo = tipoWallet.getText().toString().trim();

               if(nome.isEmpty()){
                   nomeWallet.setError("Digite o nome da carteira");
                   return;
               }
               if(tipo.isEmpty()){
                   tipoWallet.setError("Selecione um tipo");
                   return;
               }
               criarWallet(nome, tipo);

               Log.d("CARTEIRA", "TIPO: " + tipo);

               dialog.dismiss();
           });
       });

       dialog.show();

   }

   private void criarWallet(String nome, String tipo){
       walletRepository.criarNovaWallet(nome, tipo, new WalletRepository.WalletCallback() {
           @Override
           public void onSuccess(String mensagem) {
               Toast.makeText(
                       requireContext(),
                       mensagem,
                       Toast.LENGTH_SHORT
               ).show();


           }

           @Override
           public void onError(String message) {
               Toast.makeText(
                       requireContext(),
                       message,
                       Toast.LENGTH_SHORT
               ).show();
           }
       });

       Log.d("CARTEIRA", "Nome:" + nome + "| Tipo: " + tipo);
   }
}