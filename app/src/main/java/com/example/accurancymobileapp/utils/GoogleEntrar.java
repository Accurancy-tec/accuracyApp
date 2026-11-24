package com.example.accurancymobileapp.utils;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;

/**
 * "Sign in with Google" through the Android Credential Manager. It only gets the Google id_token;
 * the API checks it and creates the session.
 */
public final class GoogleEntrar {

    public interface Resultado {
        void aoObterToken(String idToken);

        /** The person closed the Google card: nothing to show. */
        void aoCancelar();

        void aoFalhar(String mensagem);
    }

    private GoogleEntrar() {}

    /** @param clientIdWeb the WEB OAuth client id (the one the API checks the token against) */
    public static void iniciar(@NonNull Activity activity, @NonNull String clientIdWeb,
                               @NonNull Resultado resultado) {
        GetSignInWithGoogleOption opcao = new GetSignInWithGoogleOption.Builder(clientIdWeb).build();
        GetCredentialRequest pedido = new GetCredentialRequest.Builder()
                .addCredentialOption(opcao)
                .build();

        CredentialManager gerenciador = CredentialManager.create(activity);
        gerenciador.getCredentialAsync(
                activity,
                pedido,
                null,
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse resposta) {
                        Credential credencial = resposta.getCredential();
                        if (credencial instanceof CustomCredential
                                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                .equals(credencial.getType())) {
                            try {
                                GoogleIdTokenCredential google = GoogleIdTokenCredential
                                        .createFrom(((CustomCredential) credencial).getData());
                                resultado.aoObterToken(google.getIdToken());
                            } catch (GoogleIdTokenParsingException e) {
                                resultado.aoFalhar("Não foi possível ler a resposta do Google.");
                            }
                        } else {
                            resultado.aoFalhar("O Google não devolveu uma conta válida.");
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException erro) {
                        if (erro instanceof GetCredentialCancellationException) {
                            resultado.aoCancelar();
                        } else if (erro instanceof NoCredentialException) {
                            resultado.aoFalhar("Nenhuma conta Google disponível neste aparelho.");
                        } else {
                            resultado.aoFalhar("Não foi possível entrar com o Google agora."
                                    + " Confira a configuração do app no Google Cloud.");
                        }
                    }
                });
    }
}
