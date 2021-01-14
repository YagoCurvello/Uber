package com.yagocurvello.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yagocurvello.uber.R;
import com.yagocurvello.uber.adapter.RequisicoesAdapter;
import com.yagocurvello.uber.config.ConfigFirebase;
import com.yagocurvello.uber.helper.UsuarioFirebase;
import com.yagocurvello.uber.model.Requisicao;
import com.yagocurvello.uber.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class MotoristaActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference reference;
    private DatabaseReference referenceReq;
    private List<Requisicao> requisicoes = new ArrayList<>();;

    private RecyclerView recyclerViewReq;
    private RequisicoesAdapter requisicoesAdapter;
    private TextView textViewResultado;
    private Usuario motorista;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motorista);

        configIniciais();

        requisicoesAdapter = new RequisicoesAdapter(requisicoes, getApplicationContext(), motorista);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewReq.setLayoutManager(layoutManager);
        recyclerViewReq.setHasFixedSize(true);
        recyclerViewReq.setAdapter(requisicoesAdapter);

    }

    private void configIniciais(){
        getSupportActionBar().setTitle("Motorista");

        auth = ConfigFirebase.getFirebaseAutenticacao();
        reference = ConfigFirebase.getFirebaseDatabase();

        recyclerViewReq = findViewById(R.id.recyclerRequisicoes);
        textViewResultado = findViewById(R.id.textViewResultado);



    }

    private void recuperarRequisicoes(){


        referenceReq = reference.child("requisicoes");

        Query requisicaoPesquisa = referenceReq.orderByChild("status").equalTo(Requisicao.STATUS_AGUARDANDO);
        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if (snapshot.getChildrenCount() > 0){
                    textViewResultado.setVisibility(View.GONE);
                    recyclerViewReq.setVisibility(View.VISIBLE);
                }else {
                    textViewResultado.setVisibility(View.VISIBLE);
                    recyclerViewReq.setVisibility(View.GONE);
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Requisicao req = dataSnapshot.getValue(Requisicao.class);
                    requisicoes.add(req);
                }

                requisicoesAdapter = new RequisicoesAdapter(requisicoes, getApplicationContext(), motorista);
                recyclerViewReq.setAdapter(requisicoesAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.sairMenu){
            auth.signOut();
            startActivity(new Intent(MotoristaActivity.this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        motorista = UsuarioFirebase.recuperarUsuarioLogado();
        recuperarRequisicoes();
    }
}