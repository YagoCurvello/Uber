package com.yagocurvello.uber.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;
import com.yagocurvello.uber.activity.EntrarActivity;
import com.yagocurvello.uber.activity.MapsActivity;
import com.yagocurvello.uber.activity.MotoristaActivity;
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

    public void redirecionar(Activity activity){

        DatabaseReference reference = ConfigFirebase.getFirebaseDatabase().child("usuarios")
                .child(UsuarioFirebase.getIdUsuario());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                if (usuario.isMotorista()){
                    activity.startActivity(new Intent(activity, MotoristaActivity.class));
                }else {
                    activity.startActivity(new Intent(activity, MapsActivity.class));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

}

