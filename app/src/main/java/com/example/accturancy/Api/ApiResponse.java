package com.example.accturancy.Api;

import com.example.accturancy.Classes.clsAportes;

import java.util.ArrayList;


//Classe criada para pegar os dados do JSON enviados pelo PHP
public class ApiResponse {
   private boolean sucesso;
   private String mensagem;
   private ArrayList<clsAportes> ativos;
   public boolean isSucesso() {
       return sucesso;
   }

    public String getMensagem() {
       return mensagem;
   }

    public ArrayList<clsAportes> getLista() {
        return ativos;
    }
}
