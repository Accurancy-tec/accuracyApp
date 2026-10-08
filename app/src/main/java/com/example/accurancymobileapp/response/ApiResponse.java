package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.clsAportes;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import kotlinx.serialization.Serializable;


//Classe criada para pegar os dados do JSON enviados pelo PHP
public class ApiResponse {
   private boolean sucesso;
   private String mensagem;
   @SerializedName(value = "ativos", alternate = {"aportes"})
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
