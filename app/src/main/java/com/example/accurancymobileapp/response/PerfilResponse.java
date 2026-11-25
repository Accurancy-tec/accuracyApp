package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.User;

/**
 * One response class for the profile, verification and Google config routes.
 * The API answers with two shapes: {success, message} and {status:"sucesso"|"erro", mensagem}.
 */
public class PerfilResponse {
    private boolean success;
    private String message;
    private String status;
    private String mensagem;
    private String client_id;
    private String foto_usuario;
    private String email_usuario;
    private User user;

    /** True for both response shapes. */
    public boolean ok() {
        return success || "sucesso".equals(status);
    }

    /** The message in whichever shape it came, or the fallback when it is empty. */
    public String texto(String padrao) {
        if (message != null && !message.trim().isEmpty()) return message;
        if (mensagem != null && !mensagem.trim().isEmpty()) return mensagem;
        return padrao;
    }

    public String getClientId() { return client_id; }
    public String getFotoUsuario() { return foto_usuario; }
    public String getEmailUsuario() { return email_usuario; }
    public User getUser() { return user; }
}
