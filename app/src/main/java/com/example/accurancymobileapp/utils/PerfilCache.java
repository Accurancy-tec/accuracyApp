package com.example.accurancymobileapp.utils;

import com.example.accurancymobileapp.model.User;

/** Last profile read from the API, so the profile tab opens already filled while it refreshes. */
public final class PerfilCache {

    private static volatile User usuario;

    private PerfilCache() {}

    public static User obter() {
        return usuario;
    }

    public static void guardar(User novo) {
        usuario = novo;
    }

    /** Called when the session ends so the next account never sees the previous one's data. */
    public static void limpar() {
        usuario = null;
        FotoPerfil.limparCache();
    }
}
