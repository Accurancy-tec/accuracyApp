package com.example.accturancy.Api;

//Classe criada para pegar os dados do JSON enviados pelo PHP
public class ApiResponse {
   private boolean sucesso;
   private String mensagem;
   public boolean isSucesso() {
       return sucesso; }

    public String getMensagem() {
       return mensagem; }
}
