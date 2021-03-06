package com.yagocurvello.uber.model;

import com.google.firebase.database.DatabaseReference;
import com.yagocurvello.uber.config.ConfigFirebase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Requisicao {

    private String id, status;
    private Usuario passageiro, motorista;
    private Destino destino;

    public static final String STATUS_AGUARDANDO = "aguardando";
    public static final String STATUS_A_CAMINHO = "acaminho";
    public static final String STATUS_VIAGEM = "viagem";
    public static final String STATUS_FINALIZADA = "finalizada";

    public Requisicao() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }

    public void salvar(){
        DatabaseReference reference = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = reference.child("requisicoes");

        setId(requisicoes.push().getKey());

        requisicoes.child(getId()).setValue(this);
    }

    public void atualizar(){
        DatabaseReference reference = ConfigFirebase.getFirebaseDatabase();
        DatabaseReference requisicoes = reference.child("requisicoes").child(getId());

        Map objeto = new HashMap();
        objeto.put("motorista", getMotorista());
        objeto.put("status", getStatus());

        requisicoes.updateChildren(objeto);
    }
}
