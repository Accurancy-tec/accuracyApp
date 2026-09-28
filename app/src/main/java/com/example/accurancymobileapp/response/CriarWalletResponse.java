package com.example.accurancymobileapp.response;

public class CriarWalletResponse {
    private boolean success;
    private String message;
    private String nomeWallet;
    private String tipoWallet;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

}
