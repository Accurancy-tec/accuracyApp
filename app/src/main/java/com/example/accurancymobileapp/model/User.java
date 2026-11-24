package com.example.accurancymobileapp.model;

public class User {
    private int id_usuario;
    private String nome_usuario;
    private String email_usuario;
    private String senha_usuario;
    private String telefone_usuario;
    private String foto_usuario;
    private Boolean email_verificado_usuario;
    private Boolean google_vinculado;

    public User(String nome_usuario, String telefone_usuario, String senha_usuario, String email_usuario) {
        this.nome_usuario = nome_usuario;
        this.telefone_usuario = telefone_usuario;
        this.senha_usuario = senha_usuario;
        this.email_usuario = email_usuario;
    }
    public User(String email_usuario, String senha_usuario){
        this.email_usuario = email_usuario;
        this.senha_usuario = senha_usuario;
    }

    public User(){

    }

    public int getId_usuario(){
        return id_usuario;
    }
    public String getNome_usuario() {
        return nome_usuario;
    }

    public void setNome_usuario(String nome_usuario) {
        this.nome_usuario = nome_usuario;
    }

    public String getEmail_usuario(String email_usuario) {
        return email_usuario;
    }

    public String getEmail_usuario(){
        return email_usuario;
    }

    public void setEmail_usuario(String email_usuario) {
        this.email_usuario = email_usuario;
    }

    public String getSenha_usuario(String senha_usuario) {
        return senha_usuario;
    }

    public String getSenha_usuario(){
        return senha_usuario;
    }

    public void setSenha_usuario(String senha_usuario) {
        this.senha_usuario = senha_usuario;
    }



    public String getTelefone_usuario() {
        return telefone_usuario;
    }

    public void setTelefone_usuario(String telefone_usuario) {
        this.telefone_usuario = telefone_usuario;
    }

    public String getFoto_usuario() {
        return foto_usuario;
    }

    public boolean isEmailVerificado() {
        return Boolean.TRUE.equals(email_verificado_usuario);
    }

    public boolean isGoogleVinculado() {
        return Boolean.TRUE.equals(google_vinculado);
    }
}
