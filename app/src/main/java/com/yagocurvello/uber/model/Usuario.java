package com.yagocurvello.uber.model;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;
import com.yagocurvello.uber.activity.MotoristaActivity;
import com.yagocurvello.uber.activity.PassageiroActivity;
import com.yagocurvello.uber.config.ConfigFirebase;
import com.yagocurvello.uber.helper.UsuarioFirebase;

import java.io.Serializable;


public class Usuario implements Serializable {

    private String name;
    private String email;
    private String senha;
    private String IdUsuario;
    private String foto;
    private boolean isMotorista = false;

    public Usuario() {
    }

    public boolean isMotorista() {
        return isMotorista;
    }

    public void setMotorista(boolean motorista) {
        isMotorista = motorista;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Exclude
    public String getIdUsuario() {
        return IdUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        IdUsuario = idUsuario;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public void salvar(){
        DatabaseReference reference = ConfigFirebase.getFirebaseDatabase();
        reference.child("usuarios").child(getIdUsuario()).setValue(this);
    }

}

