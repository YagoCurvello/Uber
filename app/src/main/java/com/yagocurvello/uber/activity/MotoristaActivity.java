package com.yagocurvello.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import com.yagocurvello.uber.helper.RecyclerItemClickListener;
import com.yagocurvello.uber.helper.UsuarioFirebase;
import com.yagocurvello.uber.model.Requisicao;
import com.yagocurvello.uber.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class MotoristaActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference reference;
    private DatabaseReference referenceReq;
    private LocationManager locationManager;
    private LocationListener locationListener;
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

        recuperarLocalizacaoUsuario();

        requisicoesAdapter = new RequisicoesAdapter(requisicoes, getApplicationContext(), motorista);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewReq.setLayoutManager(layoutManager);
        recyclerViewReq.setHasFixedSize(true);
        recyclerViewReq.setAdapter(requisicoesAdapter);

        recyclerViewReq.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(), recyclerViewReq, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view,int position) {
                Requisicao requisicao = requisicoes.get(position);
                Intent i = new Intent(MotoristaActivity.this, CorridaActivity.class);
                i.putExtra("idRequisicao", requisicao.getId());
                i.putExtra("motorista", motorista);
                startActivity(i);
            }

            @Override
            public void onLongItemClick(View view,int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> adapterView,View view,int i,long l) {

            }
        }));

    }

    private void configIniciais(){
        getSupportActionBar().setTitle("Motorista");

        auth = ConfigFirebase.getFirebaseAutenticacao();
        reference = ConfigFirebase.getFirebaseDatabase();

        recyclerViewReq = findViewById(R.id.recyclerRequisicoes);
        textViewResultado = findViewById(R.id.textViewResultado);



    }

    private void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                String latitude = String.valueOf(location.getLatitude());
                String longitude =  String.valueOf(location.getLongitude());

                if (!latitude.isEmpty() && !longitude.isEmpty()){
                    motorista.setLatitude(latitude);
                    motorista.setLongitude(longitude);
                    locationManager.removeUpdates(locationListener);
                    requisicoesAdapter.notifyDataSetChanged();
                }

            }
            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,0,0, locationListener
            );
        }

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

                requisicoes.clear();
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