package com.yagocurvello.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yagocurvello.uber.R;
import com.yagocurvello.uber.config.ConfigFirebase;
import com.yagocurvello.uber.helper.UsuarioFirebase;
import com.yagocurvello.uber.model.Destino;
import com.yagocurvello.uber.model.Requisicao;
import com.yagocurvello.uber.model.Usuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localPassageiro;
    private boolean uberChamado = false;

    private EditText editTextDestino;
    private Button buttonChamar;
    private LinearLayout linearLayoutDestino;

    private DatabaseReference reference;
    private FirebaseAuth auth;
    private Requisicao requisicao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);

        configIniciais();

        recuperaStatusRequisicao();

        buttonChamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!uberChamado){
                    String enderecoDestino = editTextDestino.getText().toString();

                    if (!enderecoDestino.equals("") || enderecoDestino != null){

                        Address addressDestino = recuperarEndereco(enderecoDestino);
                        if (addressDestino != null){

                            Destino destino = new Destino();
                            destino.setCidade(addressDestino.getAdminArea());
                            destino.setCep(addressDestino.getPostalCode());
                            destino.setBairro(addressDestino.getSubLocality());
                            destino.setRua(addressDestino.getThoroughfare());
                            destino.setNumero(addressDestino.getFeatureName());
                            destino.setLatitude(String.valueOf(addressDestino.getLatitude()));
                            destino.setLongitude(String.valueOf(addressDestino.getLongitude()));

                            StringBuilder mensagem = new StringBuilder();
                            mensagem.append("Cidade: " + destino.getCidade());
                            mensagem.append("\nRua: " + destino.getRua());
                            mensagem.append("\nBairro: " + destino.getBairro());
                            mensagem.append("\nNumero: " + destino.getNumero());
                            mensagem.append("\nCep: " + destino.getCep());

                            AlertDialog.Builder builder = new AlertDialog.Builder(PassageiroActivity.this)
                                    .setTitle("Confirme seu endereço!")
                                    .setMessage(mensagem)
                                    .setPositiveButton("Confirmar",new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,int i) {
                                            salvarRequisicao(destino);
                                            uberChamado = true;
                                        }
                                    }).setNegativeButton("Cancelar",new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface,int i) {

                                        }
                                    });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }else{
                            Toast.makeText(getApplicationContext(), "Não foi possivel identificar este endereço", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Coloque um endereço de destino", Toast.LENGTH_SHORT).show();

                    }
                }else {
                    uberChamado = false;
                    linearLayoutDestino.setVisibility(View.VISIBLE);
                    buttonChamar.setText("Chamar Uber");
                }
            }
        });

    }



    private void configIniciais(){
        editTextDestino = findViewById(R.id.editTextDestino);
        buttonChamar = findViewById(R.id.buttonChamar);
        linearLayoutDestino = findViewById(R.id.linearLayoutDestino);

        auth = ConfigFirebase.getFirebaseAutenticacao();
        reference = ConfigFirebase.getFirebaseDatabase();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Passageiro");
        setSupportActionBar(toolbar);
        
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void recuperaStatusRequisicao(){

        DatabaseReference requisicoes = reference.child("requisicoes");
        Query requisicaoPesquisa = requisicoes.orderByChild("passageiro/idUsuario")
                .equalTo(UsuarioFirebase.getIdUsuario());

        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Requisicao> requisicaoList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    requisicaoList.add(dataSnapshot.getValue(Requisicao.class));
                }
                Collections.reverse(requisicaoList);
                if (requisicaoList != null && requisicaoList.size() > 0){
                    requisicao = requisicaoList.get(0);

                    switch (requisicao.getStatus()){
                        case Requisicao.STATUS_AGUARDANDO:
                            linearLayoutDestino.setVisibility(View.GONE);
                            buttonChamar.setText("Cancelar Uber");
                            uberChamado = true;
                            break;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private Address recuperarEndereco (String s){

        Geocoder geocoder = new Geocoder(this,Locale.getDefault());
        try {
            List<Address> listEnderecos = geocoder.getFromLocationName(s, 1);
            if (listEnderecos != null && listEnderecos.size() > 0){
                Address address = listEnderecos.get(0);

                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void salvarRequisicao(Destino destino){

        Usuario usuarioPassageiro = UsuarioFirebase.recuperarUsuarioLogado();
        usuarioPassageiro.setLatitude(String.valueOf(localPassageiro.latitude));
        usuarioPassageiro.setLongitude(String.valueOf(localPassageiro.longitude));

        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);
        requisicao.setPassageiro(usuarioPassageiro);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();

        linearLayoutDestino.setVisibility(View.GONE);
        buttonChamar.setText("Cancelar Uber");
        uberChamado = true;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        recuperarLocalizacaoUsuario();

    }

    private void recuperarLocalizacaoUsuario() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localPassageiro = new LatLng(latitude,longitude);

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(localPassageiro).title("Meu local")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localPassageiro,18));

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.sairMenu){
            auth.signOut();
            startActivity(new Intent(PassageiroActivity.this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}