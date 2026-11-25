package com.example.accurancymobileapp.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accurancymobileapp.response.PerfilResponse;
import com.example.accurancymobileapp.utils.PerfilCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Change the e-mail in three steps, as the API requires:
 * 1) new address, 2) code sent to the CURRENT e-mail (authorizes the change), 3) code sent to the NEW e-mail.
 */
public class AlterarEmailBottomSheet extends PerfilSheetBase {

    public static final String TAG_FRAGMENT = "AlterarEmailBottomSheet";
    private static final String ARG_EMAIL_ATUAL = "email_atual";
    private static final String ESTADO_ETAPA = "etapa";
    private static final String ESTADO_NOVO_EMAIL = "novo_email";

    public static AlterarEmailBottomSheet nova(String emailAtual) {
        AlterarEmailBottomSheet sheet = new AlterarEmailBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL_ATUAL, emailAtual == null ? "" : emailAtual);
        sheet.setArguments(args);
        return sheet;
    }

    private int etapa = 0;
    private String emailAtual = "";
    private String novoEmail = "";

    @Override
    protected void onPronto(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        emailAtual = args == null ? "" : args.getString(ARG_EMAIL_ATUAL, "");
        if (savedInstanceState != null) {
            etapa = savedInstanceState.getInt(ESTADO_ETAPA, 0);
            novoEmail = savedInstanceState.getString(ESTADO_NOVO_EMAIL, "");
        }
        if (etapa == 1) mostrarEtapaAutorizar();
        else if (etapa == 2) mostrarEtapaConfirmar();
        else mostrarEtapaNovoEmail();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle saida) {
        super.onSaveInstanceState(saida);
        saida.putInt(ESTADO_ETAPA, etapa);
        saida.putString(ESTADO_NOVO_EMAIL, novoEmail);
    }

    private void mostrarEtapaNovoEmail() {
        etapa = 0;
        erro(null);
        tvTitulo.setText("Alterar e-mail");
        tvSub.setText("Seu e-mail atual é " + emailAtual + ". Para sua segurança, a troca precisa ser"
                + " autorizada por um código enviado para ele.");
        esconderCampos();
        campo(0, "NOVO E-MAIL", "novo@email.com",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, 150);
        botaoPrimario("Enviar código", v -> solicitar());
        botaoSecundario(null, null);
    }

    private void mostrarEtapaAutorizar() {
        etapa = 1;
        erro(null);
        tvTitulo.setText("Autorize a troca");
        tvSub.setText("Enviamos um código para o seu e-mail atual (" + emailAtual + "). Digite-o para autorizar.");
        esconderCampos();
        campo(0, "CÓDIGO", "000000", InputType.TYPE_CLASS_NUMBER, 6);
        botaoPrimario("Autorizar", v -> autorizar());
        botaoSecundario("Usar outro e-mail", v -> mostrarEtapaNovoEmail());
    }

    private void mostrarEtapaConfirmar() {
        etapa = 2;
        erro(null);
        tvTitulo.setText("Confirme o novo e-mail");
        tvSub.setText("Agora enviamos um código para " + novoEmail + ". Digite-o para concluir a troca.");
        esconderCampos();
        campo(0, "CÓDIGO", "000000", InputType.TYPE_CLASS_NUMBER, 6);
        botaoPrimario("Confirmar troca", v -> confirmar());
        botaoSecundario(null, null);
    }

    private void solicitar() {
        erro(null);
        String email = texto(0).toLowerCase();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            erro("Informe um e-mail válido.");
            return;
        }
        if (email.equalsIgnoreCase(emailAtual)) {
            erro("Esse já é o seu e-mail atual.");
            return;
        }
        ocupado(true);
        Map<String, String> corpo = new HashMap<>();
        corpo.put("novo_email", email);
        api.perfilSolicitarTrocaEmail(corpo).enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                ocupado(false);
                novoEmail = email;
                mostrarEtapaAutorizar();
            }

            @Override
            public void aoErro(String mensagem) {
                ocupado(false);
                erro(mensagem);
            }
        });
    }

    private void autorizar() {
        erro(null);
        String codigo = texto(0);
        if (!codigo.matches("[0-9]{6}")) {
            erro("Digite os 6 números do código.");
            return;
        }
        ocupado(true);
        Map<String, String> corpo = new HashMap<>();
        corpo.put("codigo", codigo);
        api.perfilAutorizarTrocaEmail(corpo).enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                ocupado(false);
                mostrarEtapaConfirmar();
            }

            @Override
            public void aoErro(String mensagem) {
                ocupado(false);
                erro(mensagem);
            }
        });
    }

    private void confirmar() {
        erro(null);
        String codigo = texto(0);
        if (!codigo.matches("[0-9]{6}")) {
            erro("Digite os 6 números do código.");
            return;
        }
        ocupado(true);
        Map<String, String> corpo = new HashMap<>();
        corpo.put("codigo", codigo);
        api.perfilConfirmarTrocaEmail(corpo).enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                Toast.makeText(requireContext(), "E-mail alterado com sucesso.", Toast.LENGTH_LONG).show();
                concluir(ACAO_RECARREGAR);
            }

            @Override
            public void aoErro(String mensagem) {
                ocupado(false);
                erro(mensagem);
            }
        });
    }
}
