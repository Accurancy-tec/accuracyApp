package com.example.accurancymobileapp.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.accurancymobileapp.response.PerfilResponse;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.PerfilCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Change (or set) the password: the API e-mails a 6 digit code, then takes the code and the new
 * password. It signs the account out everywhere, so the app goes back to the login afterwards.
 */
public class AlterarSenhaBottomSheet extends PerfilSheetBase {

    public static final String TAG_FRAGMENT = "AlterarSenhaBottomSheet";
    private static final String ESTADO_ETAPA = "etapa";
    private static final int SENHA_MINIMA = 8;

    private int etapa = 0;

    @Override
    protected void onPronto(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) etapa = savedInstanceState.getInt(ESTADO_ETAPA, 0);
        if (etapa == 1) mostrarEtapaCodigo(); else mostrarEtapaEnvio();
    }

    @Override
    public void onSaveInstanceState(Bundle saida) {
        super.onSaveInstanceState(saida);
        saida.putInt(ESTADO_ETAPA, etapa);
    }

    private void mostrarEtapaEnvio() {
        etapa = 0;
        erro(null);
        esconderCampos();
        tvTitulo.setText("Alterar senha");
        tvSub.setText("Por segurança, vamos enviar um código de 6 dígitos para o seu e-mail."
                + " Se a sua conta foi criada com o Google, é assim que você define uma senha.");
        botaoPrimario("Enviar código", v -> solicitarCodigo());
        botaoSecundario(null, null);
    }

    private void mostrarEtapaCodigo() {
        etapa = 1;
        erro(null);
        tvTitulo.setText("Nova senha");
        tvSub.setText("Digite o código que enviamos para o seu e-mail e escolha a nova senha."
                + " Depois você entra de novo em todos os aparelhos.");
        campo(0, "CÓDIGO", "000000", InputType.TYPE_CLASS_NUMBER, 6);
        campo(1, "NOVA SENHA", "Mínimo de " + SENHA_MINIMA + " caracteres",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, 72);
        campo(2, "CONFIRME A NOVA SENHA", "Repita a nova senha",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, 72);
        botaoPrimario("Alterar senha", v -> confirmar());
        botaoSecundario("Reenviar código", v -> solicitarCodigo());
    }

    private void solicitarCodigo() {
        erro(null);
        ocupado(true);
        api.perfilSolicitarTrocaSenha().enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                ocupado(false);
                boolean reenvio = etapa == 1;
                if (!reenvio) mostrarEtapaCodigo();
                Aviso.makeText(requireContext(),
                        resposta.texto("Enviamos um código para o seu e-mail."), Toast.LENGTH_SHORT).show();
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
        String nova = edit(1).getText().toString();
        String confirmacao = edit(2).getText().toString();

        if (!codigo.matches("[0-9]{6}")) {
            erro("Digite os 6 números do código.");
            return;
        }
        if (nova.length() < SENHA_MINIMA) {
            erro("A nova senha precisa ter pelo menos " + SENHA_MINIMA + " caracteres.");
            return;
        }
        if (!nova.equals(confirmacao)) {
            erro("As senhas não são iguais.");
            return;
        }

        ocupado(true);
        Map<String, String> corpo = new HashMap<>();
        corpo.put("codigo", codigo);
        corpo.put("nova_senha", nova);
        api.perfilConfirmarTrocaSenha(corpo).enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                Aviso.makeText(requireContext(),
                        "Senha alterada. Entre novamente com a nova senha.", Toast.LENGTH_LONG).show();
                concluir(ACAO_SAIR);
            }

            @Override
            public void aoErro(String mensagem) {
                ocupado(false);
                erro(mensagem);
            }
        });
    }
}
