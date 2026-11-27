package com.example.accurancymobileapp.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accurancymobileapp.response.PerfilResponse;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.PerfilCallback;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * "Esqueci a senha", na tela de login: a API manda um código de 6 números para o e-mail e,
 * com ele, a pessoa escolhe a senha nova. Todos os aparelhos são desconectados depois.
 */
public class ForgotPasswordBottomSheet extends PerfilSheetBase {

    public static final String TAG_FRAGMENT = "ForgotPasswordBottomSheet";
    /** Fragment result com o e-mail usado, para a tela de login já preencher o campo. */
    public static final String RESULTADO_CHAVE = "recuperar_senha";
    public static final String RESULTADO_EMAIL = "email";

    private static final String ARG_EMAIL = "email";
    private static final String ESTADO_ETAPA = "etapa";
    private static final String ESTADO_EMAIL = "email_estado";
    private static final int SENHA_MINIMA = 8;

    private int etapa = 0;
    private String email = "";

    public static ForgotPasswordBottomSheet nova(@Nullable String emailDigitado) {
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, emailDigitado == null ? "" : emailDigitado.trim());
        ForgotPasswordBottomSheet cartao = new ForgotPasswordBottomSheet();
        cartao.setArguments(args);
        return cartao;
    }

    @Override
    protected void onPronto(@Nullable Bundle savedInstanceState) {
        email = getArguments() != null ? getArguments().getString(ARG_EMAIL, "") : "";
        if (savedInstanceState != null) {
            etapa = savedInstanceState.getInt(ESTADO_ETAPA, 0);
            email = savedInstanceState.getString(ESTADO_EMAIL, email);
        }
        if (etapa == 1) mostrarEtapaCodigo(); else mostrarEtapaEmail();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle saida) {
        super.onSaveInstanceState(saida);
        saida.putInt(ESTADO_ETAPA, etapa);
        saida.putString(ESTADO_EMAIL, email);
    }

    private void mostrarEtapaEmail() {
        etapa = 0;
        erro(null);
        esconderCampos();
        tvTitulo.setText("Recuperar senha");
        tvSub.setText("Informe o e-mail da sua conta. Vamos enviar um código de 6 números para você criar uma senha nova.");
        campo(0, "E-MAIL", "seu@email.com",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, 120);
        edit(0).setText(email);
        botaoPrimario("Enviar código", v -> pedirCodigo(texto(0)));
        botaoSecundario("Cancelar", v -> dismiss());
    }

    private void mostrarEtapaCodigo() {
        etapa = 1;
        erro(null);
        esconderCampos();
        tvTitulo.setText("Crie uma senha nova");
        tvSub.setText("Se houver uma conta com " + email + ", o código chegou nesse e-mail (confira também o spam)."
                + " Ele vale por 10 minutos.");
        campo(0, "CÓDIGO", "000000", InputType.TYPE_CLASS_NUMBER, 6);
        campo(1, "NOVA SENHA", "Mínimo de " + SENHA_MINIMA + " caracteres",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, 72);
        campo(2, "CONFIRME A NOVA SENHA", "Repita a nova senha",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, 72);
        botaoPrimario("Redefinir senha", v -> redefinir());
        botaoSecundario("Reenviar código", v -> pedirCodigo(email));
    }

    private void pedirCodigo(String informado) {
        erro(null);
        String limpo = informado == null ? "" : informado.trim().toLowerCase(Locale.ROOT);
        if (!Patterns.EMAIL_ADDRESS.matcher(limpo).matches()) {
            erro("Informe um e-mail válido.");
            return;
        }
        email = limpo;
        ocupado(true);
        Map<String, String> corpo = new HashMap<>();
        corpo.put("email_usuario", email);
        api.recuperarSenha(corpo).enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                ocupado(false);
                boolean reenvio = etapa == 1;
                if (!reenvio) mostrarEtapaCodigo();
                Aviso.makeText(requireContext(),
                        reenvio ? "Se a conta existir, enviamos um novo código." : "Confira o seu e-mail.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void aoErro(String mensagem) {
                ocupado(false);
                erro(mensagem);
            }
        });
    }

    private void redefinir() {
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
        corpo.put("email_usuario", email);
        corpo.put("codigo", codigo);
        corpo.put("nova_senha", nova);
        api.redefinirSenha(corpo).enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                Aviso.makeText(requireContext(), resposta.texto("Senha redefinida. Entre com a nova senha."),
                        Toast.LENGTH_LONG).show();
                Bundle resultado = new Bundle();
                resultado.putString(RESULTADO_EMAIL, email);
                getParentFragmentManager().setFragmentResult(RESULTADO_CHAVE, resultado);
                dismissAllowingStateLoss();
            }

            @Override
            public void aoErro(String mensagem) {
                ocupado(false);
                erro(mensagem);
            }
        });
    }
}
