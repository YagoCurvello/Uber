package com.yagocurvello.uber.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.yagocurvello.uber.config.ConfigFirebase;

import java.io.Serializable;


public class Usuario implements Serializable {

    private String name;
    private String email;
    private String senha;
    private String IdUsuario;
    private String latitude;
    private String longitude;
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

    public String getIdUsuario() {
        return IdUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        IdUsuario = idUsuario;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void salvar(){
        DatabaseReference reference = ConfigFirebase.getFirebaseDatabase();
        reference.child("usuarios").child(getIdUsuario()).setValue(this);
    }

}

