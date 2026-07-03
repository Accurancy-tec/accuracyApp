package com.example.accurancymobileapp.model;

public class User {
    private String nome_usuario;
    private String email_usuario;
    private String senha_usuario;
    private String cpf_usuario;
    private String telefone_usuario;

    public User(String nome_usuario, String telefone_usuario, String cpf_usuario, String senha_usuario, String email_usuario) {
        this.nome_usuario = nome_usuario;
        this.telefone_usuario = telefone_usuario;
        this.cpf_usuario = cpf_usuario;
        this.senha_usuario = senha_usuario;
        this.email_usuario = email_usuario;
    }
    public User(String email_usuario, String senha_usuario){
        this.email_usuario = email_usuario;
        this.senha_usuario = senha_usuario;
    }

    public User(){

    }

    public String getNome_usuario() {
        return nome_usuario;
    }

    public void setNome_usuario(String nome_usuario) {
        this.nome_usuario = nome_usuario;
    }

    public String getEmail_usuario() {
        return email_usuario;
    }

    public void setEmail_usuario(String email_usuario) {
        this.email_usuario = email_usuario;
    }

    public String getSenha_usuario() {
        return senha_usuario;
    }

    public void setSenha_usuario(String senha_usuario) {
        this.senha_usuario = senha_usuario;
    }

    public String getCpf_usuario() {
        return cpf_usuario;
    }

    public void setCpf_usuario(String cpf_usuario) {
        this.cpf_usuario = cpf_usuario;
    }

    public String getTelefone_usuario() {
        return telefone_usuario;
    }

    public void setTelefone_usuario(String telefone_usuario) {
        this.telefone_usuario = telefone_usuario;
    }
}
