package com.example.accurancymobileapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.AjudaActivity;
import com.example.accurancymobileapp.activities.DispositivosActivity;
import com.example.accurancymobileapp.activities.ReportActivity;
import com.example.accurancymobileapp.activities.LoginActivity;
import com.example.accurancymobileapp.activities.NotificacoesActivity;
import com.example.accurancymobileapp.model.User;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.PerfilResponse;
import com.example.accurancymobileapp.response.RespostaSimples;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.ContadorNotificacoes;
import com.example.accurancymobileapp.utils.Esqueleto;
import com.example.accurancymobileapp.utils.FotoPerfil;
import com.example.accurancymobileapp.utils.PerfilCache;
import com.example.accurancymobileapp.utils.PerfilCallback;
import com.example.accurancymobileapp.utils.SessionManager;
import com.example.accurancymobileapp.utils.TemaApp;
import com.example.accurancymobileapp.utils.TutorialTour;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Profile tab: shows the account data from the API (each line opens a small card with the
 * information), lets the person change the name, phone, photo, e-mail, password and the app
 * theme, opens help and notifications, and signs out.
 */
public class ProfileFragment extends Fragment {

    private ApiService api;
    private User usuario;
    private Call<PerfilResponse> chamadaPerfil;
    private boolean enviandoFoto = false;
    private String urlFotoMostrada;

    private TextView txtNomePerfil, txtEmailPerfil, txtIniciais, txtNomeConta, txtEmailConta,
            txtEmailExtra, txtTelefoneConta, txtTemaAtual, badgeNotificacoes;
    private ImageView imgFoto;
    private ProgressBar progressoFoto;
    private ImageButton btnAlterarFoto;
    private View sino;
    private MaterialSwitch switchTemaEscuro;
    private boolean trocandoTema = false;

    private final ActivityResultLauncher<String> seletorFoto = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) enviarFoto(uri);
            });

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        api = RetrofitClient.getApiService(requireContext());

        txtNomePerfil = view.findViewById(R.id.txtNomePerfil);
        txtEmailPerfil = view.findViewById(R.id.txtEmailPerfil);
        txtIniciais = view.findViewById(R.id.txtIniciaisPerfil);
        txtNomeConta = view.findViewById(R.id.txtNomeConta);
        txtEmailConta = view.findViewById(R.id.txtEmailConta);
        txtEmailExtra = view.findViewById(R.id.txtEmailExtra);
        txtTelefoneConta = view.findViewById(R.id.txtTelefoneConta);
        imgFoto = view.findViewById(R.id.imgFotoPerfil);
        progressoFoto = view.findViewById(R.id.progressoFoto);
        btnAlterarFoto = view.findViewById(R.id.btnAlterarFoto);

        sino = view.findViewById(R.id.btnNotificacoes);
        badgeNotificacoes = view.findViewById(R.id.tvBadgeNotificacoes);
        txtTemaAtual = view.findViewById(R.id.txtTemaAtual);
        switchTemaEscuro = view.findViewById(R.id.switchTemaEscuro);

        view.findViewById(R.id.btnEditarPerfil).setOnClickListener(v -> abrirEdicao());
        view.findViewById(R.id.itemNome).setOnClickListener(v -> mostrarInfoNome());
        view.findViewById(R.id.itemEmail).setOnClickListener(v -> mostrarInfoEmail());
        view.findViewById(R.id.itemTelefone).setOnClickListener(v -> mostrarInfoTelefone());
        view.findViewById(R.id.itemAparencia).setOnClickListener(v -> trocarTema());
        view.findViewById(R.id.itemPerguntasFrequentes).setOnClickListener(
                v -> startActivity(AjudaActivity.intent(requireContext(), false)));
        view.findViewById(R.id.itemFaleConosco).setOnClickListener(
                v -> startActivity(AjudaActivity.intent(requireContext(), true)));
        view.findViewById(R.id.itemReverTutorial).setOnClickListener(
                v -> TutorialTour.iniciar(requireActivity(), false));
        view.findViewById(R.id.itemAlterarSenha).setOnClickListener(v -> abrirTrocaSenha());
        view.findViewById(R.id.itemAparelhos).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), DispositivosActivity.class)));
        view.findViewById(R.id.itemReport).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), ReportActivity.class)));
        view.findViewById(R.id.itemSair).setOnClickListener(v -> logoutSession());
        sino.setOnClickListener(v -> startActivity(new Intent(requireContext(), NotificacoesActivity.class)));
        btnAlterarFoto.setOnClickListener(v -> seletorFoto.launch("image/*"));
        imgFoto.setOnClickListener(v -> seletorFoto.launch("image/*"));

        // Something changed in one of the cards (data saved, e-mail changed, password changed),
        // or the info card asked for one of the edit cards.
        getParentFragmentManager().setFragmentResultListener(PerfilSheetBase.RESULTADO_CHAVE,
                getViewLifecycleOwner(), (chave, resultado) -> {
                    String acao = resultado.getString(PerfilSheetBase.RESULTADO_ACAO, "");
                    switch (acao) {
                        case PerfilSheetBase.ACAO_SAIR:
                            logoutSession();
                            break;
                        case PerfilSheetBase.ACAO_EDITAR_DADOS:
                            abrirEdicao();
                            break;
                        case PerfilSheetBase.ACAO_ALTERAR_EMAIL:
                            abrirTrocaEmail();
                            break;
                        default:
                            carregarPerfil();
                    }
                });

        mostrarTema();
        usuario = PerfilCache.obter();
        mostrarUsuario();
        carregarPerfil();
    }

    @Override
    public void onResume() {
        super.onResume();
        ContadorNotificacoes.atualizar(this, sino, badgeNotificacoes);
    }

    // ---------------------------------------------------------------- theme

    private void mostrarTema() {
        boolean escuro = !TemaApp.claroAtivo(requireContext());
        txtTemaAtual.setText(TemaApp.nomeAtual(requireContext()));
        switchTemaEscuro.setChecked(escuro);
        switchTemaEscuro.setContentDescription("Tema escuro");
    }

    /** Liga ou desliga o tema escuro. O switch anima antes de as telas serem recriadas com a nova paleta. */
    private void trocarTema() {
        if (trocandoTema) return;
        trocandoTema = true;
        switchTemaEscuro.setChecked(!switchTemaEscuro.isChecked());
        Context contexto = requireContext().getApplicationContext();
        new Handler(Looper.getMainLooper()).postDelayed(() -> TemaApp.alternar(contexto), 220L);
    }

    // ------------------------------------------------------------------ data

    private void carregarPerfil() {
        if (chamadaPerfil != null) chamadaPerfil.cancel();
        chamadaPerfil = api.perfilObter();
        chamadaPerfil.enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                if (resposta.getUser() == null) {
                    aoErro("O servidor não devolveu os dados do perfil.");
                    return;
                }
                usuario = resposta.getUser();
                PerfilCache.guardar(usuario);
                mostrarUsuario();
            }

            @Override
            public void aoErro(String mensagem) {
                if (usuario == null) {
                    Esqueleto.parar(txtNomePerfil, txtEmailPerfil, txtNomeConta, txtEmailConta, txtTelefoneConta);
                    txtNomePerfil.setText("Não foi possível carregar");
                    txtEmailPerfil.setText("Abra a aba de novo para tentar outra vez");
                    txtNomeConta.setText("—");
                    txtEmailConta.setText("—");
                    txtTelefoneConta.setText("—");
                    Aviso.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void mostrarUsuario() {
        if (usuario == null) {
            // Nothing known yet: shimmering bars (the texts only give them a width).
            txtNomePerfil.setText("Carregando nome");
            txtEmailPerfil.setText("carregando@email.com");
            txtNomeConta.setText("Carregando nome");
            txtEmailConta.setText("carregando@email.com");
            txtTelefoneConta.setText("(00) 00000-0000");
            Esqueleto.iniciar(txtNomePerfil, txtEmailPerfil, txtNomeConta, txtEmailConta, txtTelefoneConta);
            txtEmailExtra.setVisibility(View.GONE);
            mostrarFoto(null, null);
            return;
        }
        // Real data: give the views their normal look back before setting texts and colors.
        Esqueleto.parar(txtNomePerfil, txtEmailPerfil, txtNomeConta, txtEmailConta, txtTelefoneConta);
        String nome = valor(usuario.getNome_usuario());
        String email = valor(usuario.getEmail_usuario());
        String telefone = valor(usuario.getTelefone_usuario());

        txtNomePerfil.setText(nome.isEmpty() ? "Sem nome" : nome);
        txtEmailPerfil.setText(email);
        txtNomeConta.setText(nome.isEmpty() ? "Adicionar nome" : nome);
        txtEmailConta.setText(email.isEmpty() ? "—" : email);
        if (telefone.isEmpty()) {
            txtTelefoneConta.setText("Adicionar telefone");
            txtTelefoneConta.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_primary));
        } else {
            txtTelefoneConta.setText(telefone);
            txtTelefoneConta.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        }
        if (usuario.isGoogleVinculado()) {
            txtEmailExtra.setText("Conta Google vinculada");
            txtEmailExtra.setVisibility(View.VISIBLE);
        } else {
            txtEmailExtra.setVisibility(View.GONE);
        }
        mostrarFoto(usuario.getFoto_usuario(), nome);
    }

    private static String valor(@Nullable String texto) {
        return texto == null ? "" : texto.trim();
    }

    // ----------------------------------------------------------------- photo

    /** Shows the photo when there is one, otherwise the initials (or the generic icon without a name). */
    private void mostrarFoto(@Nullable String url, @Nullable String nome) {
        if (url != null && !url.isEmpty()) {
            if (url.equals(urlFotoMostrada)) return;
            final String pedida = url;
            FotoPerfil.carregar(url, new FotoPerfil.Resultado<Bitmap>() {
                @Override
                public void aoConcluir(Bitmap bitmap) {
                    if (!isAdded() || getView() == null) return;
                    if (usuario == null || !pedida.equals(usuario.getFoto_usuario())) return;
                    urlFotoMostrada = pedida;
                    colocarBitmap(bitmap);
                }

                @Override
                public void aoFalhar(String mensagem) {
                    if (!isAdded() || getView() == null) return;
                    mostrarIniciais(nome);
                }
            });
            return;
        }
        urlFotoMostrada = null;
        mostrarIniciais(nome);
    }

    private void colocarBitmap(Bitmap bitmap) {
        RoundedBitmapDrawable redondo = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        redondo.setCircular(true);
        imgFoto.setImageTintList(null);
        imgFoto.setPadding(0, 0, 0, 0);
        imgFoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgFoto.setImageDrawable(redondo);
        txtIniciais.setVisibility(View.GONE);
    }

    private void mostrarIniciais(@Nullable String nome) {
        String iniciais = iniciaisDe(nome);
        int padding = Math.round(24 * getResources().getDisplayMetrics().density);
        if (iniciais.isEmpty()) {
            imgFoto.setImageResource(R.drawable.ic_pessoa);
            imgFoto.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.white)));
            imgFoto.setPadding(padding, padding, padding, padding);
            imgFoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            txtIniciais.setVisibility(View.GONE);
        } else {
            imgFoto.setImageDrawable(null);
            txtIniciais.setText(iniciais);
            txtIniciais.setVisibility(View.VISIBLE);
        }
    }

    private static String iniciaisDe(@Nullable String nome) {
        if (nome == null) return "";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 0 || partes[0].isEmpty()) return "";
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase();
    }

    private void enviarFoto(Uri uri) {
        if (enviandoFoto) return; // one upload at a time
        definirEnviandoFoto(true);
        FotoPerfil.prepararJpeg(requireContext(), uri, new FotoPerfil.Resultado<byte[]>() {
            @Override
            public void aoConcluir(byte[] jpeg) {
                if (!isAdded() || getView() == null) {
                    enviandoFoto = false;
                    return;
                }
                RequestBody corpo = RequestBody.create(jpeg, MediaType.get("image/jpeg"));
                MultipartBody.Part parte = MultipartBody.Part.createFormData("foto", "foto.jpg", corpo);
                api.perfilFoto(parte).enqueue(new PerfilCallback(ProfileFragment.this) {
                    @Override
                    public void aoSucesso(PerfilResponse resposta) {
                        definirEnviandoFoto(false);
                        String novaUrl = resposta.getFotoUsuario();
                        Bitmap local = FotoPerfil.decodificar(jpeg);
                        if (novaUrl != null && !novaUrl.isEmpty() && local != null) {
                            // Show the picked image now and keep it as the cached copy of the new address.
                            FotoPerfil.guardarNoCache(novaUrl, local);
                            urlFotoMostrada = novaUrl;
                            colocarBitmap(local);
                        }
                        carregarPerfil();
                        Aviso.makeText(requireContext(), "Foto atualizada.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void aoErro(String mensagem) {
                        definirEnviandoFoto(false);
                        Aviso.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void aoFalhar(String mensagem) {
                enviandoFoto = false;
                if (!isAdded() || getView() == null) return;
                definirEnviandoFoto(false);
                Aviso.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void definirEnviandoFoto(boolean enviando) {
        enviandoFoto = enviando;
        progressoFoto.setVisibility(enviando ? View.VISIBLE : View.GONE);
        btnAlterarFoto.setEnabled(!enviando);
        imgFoto.setEnabled(!enviando);
        imgFoto.setAlpha(enviando ? 0.5f : 1f);
    }

    // ----------------------------------------------------------------- cards

    private boolean perfilPronto() {
        if (usuario == null) {
            Aviso.makeText(requireContext(), "Aguarde o perfil carregar.", Toast.LENGTH_SHORT).show();
            carregarPerfil();
            return false;
        }
        return true;
    }

    private void abrirEdicao() {
        if (!perfilPronto()) return;
        EditarPerfilBottomSheet.nova(usuario.getNome_usuario(), usuario.getTelefone_usuario())
                .show(getParentFragmentManager(), EditarPerfilBottomSheet.TAG_FRAGMENT);
    }

    private void abrirTrocaEmail() {
        if (!perfilPronto()) return;
        AlterarEmailBottomSheet.nova(usuario.getEmail_usuario())
                .show(getParentFragmentManager(), AlterarEmailBottomSheet.TAG_FRAGMENT);
    }

    private void abrirTrocaSenha() {
        new AlterarSenhaBottomSheet()
                .show(getParentFragmentManager(), AlterarSenhaBottomSheet.TAG_FRAGMENT);
    }

    // ----------------------------------------------------- info cards (mini janela)

    private void mostrarInfo(InfoPerfilBottomSheet cartao) {
        if (getParentFragmentManager().findFragmentByTag(InfoPerfilBottomSheet.TAG_FRAGMENT) != null) return;
        cartao.show(getParentFragmentManager(), InfoPerfilBottomSheet.TAG_FRAGMENT);
    }

    private void mostrarInfoNome() {
        if (!perfilPronto()) return;
        String nome = valor(usuario.getNome_usuario());
        mostrarInfo(InfoPerfilBottomSheet.nova(R.drawable.ic_pessoa, "Nome",
                nome.isEmpty() ? "Nenhum nome cadastrado" : nome, null,
                "É como você aparece no Accuracy, no app e no site.",
                "Editar nome", PerfilSheetBase.ACAO_EDITAR_DADOS));
    }

    private void mostrarInfoEmail() {
        if (!perfilPronto()) return;
        String email = valor(usuario.getEmail_usuario());
        mostrarInfo(InfoPerfilBottomSheet.nova(R.drawable.ic_email, "E-mail",
                email.isEmpty() ? "—" : email,
                usuario.isGoogleVinculado() ? "Conta Google vinculada" : null,
                "Usado para entrar na conta e para receber os códigos de confirmação. "
                        + "Para trocar, confirmamos pelo e-mail atual.",
                "Alterar e-mail", PerfilSheetBase.ACAO_ALTERAR_EMAIL));
    }

    private void mostrarInfoTelefone() {
        if (!perfilPronto()) return;
        String telefone = valor(usuario.getTelefone_usuario());
        boolean semTelefone = telefone.isEmpty();
        mostrarInfo(InfoPerfilBottomSheet.nova(R.drawable.ic_telefone, "Telefone",
                semTelefone ? "Nenhum telefone cadastrado" : telefone, null,
                "Opcional. Fica guardado só na sua conta e não aparece para outras pessoas.",
                semTelefone ? "Adicionar telefone" : "Editar telefone",
                PerfilSheetBase.ACAO_EDITAR_DADOS));
    }

    // ---------------------------------------------------------------- logout

    @Override
    public void onDestroyView() {
        if (chamadaPerfil != null) chamadaPerfil.cancel();
        super.onDestroyView();
    }

    public void logoutSession() {
        SessionManager sessao = new SessionManager(requireContext());
        encerrarSessaoNoServidor(sessao.getRefreshToken());
        sessao.logout();

        Intent it = new Intent(requireContext(), LoginActivity.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(it);
    }

    /**
     * Closes this device's session on the API too, so it leaves the connected devices list.
     * If the request fails the phone is signed out anyway (the session expires on its own).
     */
    private void encerrarSessaoNoServidor(@Nullable String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) return;
        Map<String, String> corpo = new HashMap<>();
        corpo.put("refresh_token", refreshToken);
        RetrofitClient.getApiService(requireContext().getApplicationContext()).sair(corpo)
                .enqueue(new retrofit2.Callback<RespostaSimples>() {
                    @Override
                    public void onResponse(@NonNull Call<RespostaSimples> call,
                                           @NonNull retrofit2.Response<RespostaSimples> response) {
                    }

                    @Override
                    public void onFailure(@NonNull Call<RespostaSimples> call, @NonNull Throwable t) {
                    }
                });
    }
}
