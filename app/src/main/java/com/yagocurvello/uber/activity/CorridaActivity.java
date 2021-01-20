package com.yagocurvello.uber.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.yagocurvello.uber.R;
import com.yagocurvello.uber.config.ConfigFirebase;
import com.yagocurvello.uber.model.Requisicao;
import com.yagocurvello.uber.model.Usuario;

public class CorridaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localMotorista;
    private LatLng localPassageiro;

    private Marker marcadorMotorista;
    private Marker marcadorPassageiro;

    private DatabaseReference reference;

    private Button buttonAceitar;
    private Requisicao requisicao;
    private Usuario motorista;
    private Usuario passageiro;
    private String idRequisicao;
    private String statusRequisicao;
    private boolean reqAtiva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);

        configIniciais();

        if (getIntent().getExtras().containsKey("idRequisicao")
        && getIntent().getExtras().containsKey("motorista")){
            Bundle bundle = getIntent().getExtras();
            motorista = (Usuario) bundle.getSerializable("motorista");

            Double latitude = Double.parseDouble(motorista.getLatitude());
            Double longitude = Double.parseDouble(motorista.getLongitude());
            localMotorista = new LatLng(latitude,longitude);

            idRequisicao = bundle.getString("idRequisicao");
            reqAtiva = bundle.getBoolean("reqAtiva");

        }

        buttonAceitar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requisicao = new Requisicao();
                requisicao.setId(idRequisicao);
                requisicao.setMotorista(motorista);
                requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);
                requisicao.atualizar();
                requisicaoACaminho();
            }
        });

    }

    @SuppressLint("RestrictedApi")
    private void configIniciais(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Iniciar Corrida");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        reference = ConfigFirebase.getFirebaseDatabase();

        buttonAceitar = findViewById(R.id.buttonAceitarCorrida);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void verificaStatusRequisicao(){
        DatabaseReference requisicoes = reference.child("requisicoes").child(idRequisicao);
        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requisicao = snapshot.getValue(Requisicao.class);
                if (requisicao != null){
                    passageiro = requisicao.getPassageiro();
                    localPassageiro = new LatLng(Double.parseDouble(passageiro.getLatitude()),
                            Double.parseDouble(passageiro.getLongitude()));

                    statusRequisicao = requisicao.getStatus();
                    alteraInterfaceStatusRequisicao(statusRequisicao);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void alteraInterfaceStatusRequisicao(String status){

        switch (status){
            case Requisicao.STATUS_AGUARDANDO:
                requisicaoAguardando();
                break;

            case Requisicao.STATUS_A_CAMINHO:
                requisicaoACaminho();
                break;
        }

    }

    private void requisicaoAguardando(){
        buttonAceitar.setText("aceitar corrida");

    }

    private void requisicaoACaminho(){
        buttonAceitar.setText("A caminho");
        //Exibe marcador do motorista
        adicionaMarcadorMotorista(localMotorista, motorista.getName());

        //Exibe o marcador do passageiro
        adicionaMarcadorPassageiro(localPassageiro, passageiro.getName());

        centralizarMarcadores(marcadorMotorista, marcadorPassageiro);
    }

    private void adicionaMarcadorMotorista(LatLng localizacao, String titulo){

        if (marcadorMotorista != null) marcadorMotorista.remove();

        marcadorMotorista = mMap.addMarker(new MarkerOptions().position(localizacao).title(titulo)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro)));
    }

    private void adicionaMarcadorPassageiro(LatLng localizacao, String titulo){

        if (marcadorPassageiro != null) marcadorPassageiro.remove();

        marcadorPassageiro = mMap.addMarker(new MarkerOptions().position(localizacao).title(titulo)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario)));
    }

    private void centralizarMarcadores(Marker marker1, Marker marker2){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(marker1.getPosition());
        builder.include(marker2.getPosition());
        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.2);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, largura, altura, espacoInterno));

    }

    private void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localMotorista = new LatLng(latitude,longitude);

                alteraInterfaceStatusRequisicao(statusRequisicao);

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
                    LocationManager.GPS_PROVIDER,10000,10, locationListener
            );
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        recuperarLocalizacaoUsuario();
        verificaStatusRequisicao();
    }

    @Override
    public boolean onSupportNavigateUp() {

        if (reqAtiva){
            Toast.makeText(CorridaActivity.this, "Encerre a requisição atual para voltar", Toast.LENGTH_SHORT).show();
        }else {
            Intent i = new Intent(CorridaActivity.this, MotoristaActivity.class);
            startActivity(i);
        }

        return false;
    }
}