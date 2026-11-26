package com.example.accurancymobileapp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accurancymobileapp.response.PerfilResponse;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.PerfilCallback;

import java.util.HashMap;
import java.util.Map;

/** Edit the name and the phone. Only what changed is sent. */
public class EditarPerfilBottomSheet extends PerfilSheetBase {

    public static final String TAG_FRAGMENT = "EditarPerfilBottomSheet";
    private static final String ARG_NOME = "nome";
    private static final String ARG_TELEFONE = "telefone";

    public static EditarPerfilBottomSheet nova(String nome, String telefone) {
        EditarPerfilBottomSheet sheet = new EditarPerfilBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_NOME, nome == null ? "" : nome);
        args.putString(ARG_TELEFONE, telefone == null ? "" : telefone);
        sheet.setArguments(args);
        return sheet;
    }

    private String nomeAtual = "";
    private String telefoneAtual = "";
    private boolean formatando = false;

    @Override
    protected void onPronto(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        nomeAtual = args == null ? "" : args.getString(ARG_NOME, "");
        telefoneAtual = args == null ? "" : args.getString(ARG_TELEFONE, "");

        tvTitulo.setText("Editar perfil");
        tvSub.setText("Atualize seus dados. O e-mail e a senha são alterados em opções próprias, com confirmação por código.");

        campo(0, "NOME", "Seu nome", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS
                | InputType.TYPE_TEXT_VARIATION_PERSON_NAME, 100);
        campo(1, "TELEFONE", "(11) 91234-5678", InputType.TYPE_CLASS_PHONE, 16);
        if (savedInstanceState == null) {
            edit(0).setText(nomeAtual);
            edit(1).setText(formatarTelefone(telefoneAtual));
        }
        edit(1).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (formatando) return;
                formatando = true;
                String formatado = formatarTelefone(s.toString());
                if (!formatado.equals(s.toString())) {
                    s.replace(0, s.length(), formatado);
                }
                formatando = false;
            }
        });

        botaoPrimario("Salvar", v -> salvar());
        botaoSecundario(null, null);
    }

    private void salvar() {
        erro(null);
        String nome = texto(0).replaceAll("\\s+", " ");
        String telefone = texto(1);
        String digitosTelefone = telefone.replaceAll("\\D", "");

        if (nome.length() < 2 || nome.length() > 100) {
            erro("Informe um nome com 2 a 100 caracteres.");
            return;
        }
        if (!digitosTelefone.isEmpty() && digitosTelefone.length() != 10 && digitosTelefone.length() != 11) {
            erro("Informe o telefone com DDD, por exemplo (11) 91234-5678.");
            return;
        }

        Map<String, String> corpo = new HashMap<>();
        if (!nome.equals(nomeAtual)) corpo.put("nome_usuario", nome);
        if (!digitosTelefone.equals(telefoneAtual.replaceAll("\\D", ""))) {
            corpo.put("telefone_usuario", telefone);
        }
        if (corpo.isEmpty()) {
            dismissAllowingStateLoss();
            return;
        }

        ocupado(true);
        api.perfilAtualizar(corpo).enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                Aviso.makeText(requireContext(), "Dados atualizados.", Toast.LENGTH_SHORT).show();
                concluir(ACAO_RECARREGAR);
            }

            @Override
            public void aoErro(String mensagem) {
                ocupado(false);
                erro(mensagem);
            }
        });
    }

    /** Formats up to 11 digits as (11) 91234-5678 or (11) 1234-5678; drops a leading country code 55. */
    static String formatarTelefone(@NonNull String bruto) {
        String d = bruto.replaceAll("\\D", "");
        if (bruto.trim().startsWith("+55") && d.length() > 11) d = d.substring(2);
        if (d.length() > 11) d = d.substring(0, 11);
        if (d.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("(").append(d, 0, Math.min(2, d.length()));
        if (d.length() < 3) return sb.toString();
        sb.append(") ");
        if (d.length() <= 10) {
            sb.append(d, 2, Math.min(6, d.length()));
            if (d.length() > 6) sb.append('-').append(d, 6, d.length());
        } else {
            sb.append(d, 2, 7).append('-').append(d, 7, d.length());
        }
        return sb.toString();
    }
}
