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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.yagocurvello.uber.R;
import com.yagocurvello.uber.config.ConfigFirebase;
import com.yagocurvello.uber.model.Destino;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth auth;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private EditText editTextDestino;
    private Button buttonChamar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);

        configIniciais();

        buttonChamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

    }



    private void configIniciais(){
        editTextDestino = findViewById(R.id.editTextDestino);
        buttonChamar = findViewById(R.id.buttonChamar);
        auth = ConfigFirebase.getFirebaseAutenticacao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Passageiro");
        setSupportActionBar(toolbar);
        
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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
                LatLng meuLocal = new LatLng(latitude,longitude);

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(meuLocal).title("Meu local")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(meuLocal,18));

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