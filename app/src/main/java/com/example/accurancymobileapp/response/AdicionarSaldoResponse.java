package com.example.accurancymobileapp.response;

public class AdicionarSaldoResponse {
    private boolean success;
    private String message;
    private String saldo_atual;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getSaldoAtual() {
        return saldo_atual;
    }
}
