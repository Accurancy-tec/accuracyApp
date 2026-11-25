package com.example.accurancymobileapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accurancymobileapp.R;

/**
 * Cartão pequeno que mostra um dado da conta (nome, e-mail ou telefone) ao tocar na linha do perfil.
 * O botão de ação não edita nada aqui: ele avisa o perfil, que abre o cartão de edição certo.
 */
public class InfoPerfilBottomSheet extends CartaoDeslizante {

    public static final String TAG_FRAGMENT = "info_perfil";

    private static final String ARG_ICONE = "icone";
    private static final String ARG_ROTULO = "rotulo";
    private static final String ARG_VALOR = "valor";
    private static final String ARG_SELO = "selo";
    private static final String ARG_DETALHE = "detalhe";
    private static final String ARG_BOTAO = "botao";
    private static final String ARG_ACAO = "acao";

    /**
     * @param selo  linha curta em verde abaixo do valor (ex.: "Conta Google vinculada"); null para esconder
     * @param acao  uma das PerfilSheetBase.ACAO_* devolvida ao perfil quando o botão é tocado
     */
    public static InfoPerfilBottomSheet nova(@DrawableRes int icone, String rotulo, String valor,
                                             @Nullable String selo, String detalhe,
                                             String textoBotao, String acao) {
        Bundle args = new Bundle();
        args.putInt(ARG_ICONE, icone);
        args.putString(ARG_ROTULO, rotulo);
        args.putString(ARG_VALOR, valor);
        args.putString(ARG_SELO, selo);
        args.putString(ARG_DETALHE, detalhe);
        args.putString(ARG_BOTAO, textoBotao);
        args.putString(ARG_ACAO, acao);
        InfoPerfilBottomSheet cartao = new InfoPerfilBottomSheet();
        cartao.setArguments(args);
        return cartao;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_info_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();

        ((ImageView) view.findViewById(R.id.imgInfoPerfil)).setImageResource(args.getInt(ARG_ICONE));
        ((TextView) view.findViewById(R.id.tvInfoPerfilRotulo)).setText(args.getString(ARG_ROTULO));
        ((TextView) view.findViewById(R.id.tvInfoPerfilValor)).setText(args.getString(ARG_VALOR));
        ((TextView) view.findViewById(R.id.tvInfoPerfilDetalhe)).setText(args.getString(ARG_DETALHE));

        TextView selo = view.findViewById(R.id.tvInfoPerfilSelo);
        String textoSelo = args.getString(ARG_SELO);
        if (textoSelo != null && !textoSelo.isEmpty()) {
            selo.setText(textoSelo);
            selo.setVisibility(View.VISIBLE);
        }

        view.findViewById(R.id.btnInfoPerfilFechar).setOnClickListener(v -> dismiss());

        Button acao = view.findViewById(R.id.btnInfoPerfilAcao);
        acao.setText(args.getString(ARG_BOTAO));
        acao.setOnClickListener(v -> {
            Bundle resultado = new Bundle();
            resultado.putString(PerfilSheetBase.RESULTADO_ACAO, args.getString(ARG_ACAO));
            getParentFragmentManager().setFragmentResult(PerfilSheetBase.RESULTADO_CHAVE, resultado);
            dismiss();
        });
    }
}
