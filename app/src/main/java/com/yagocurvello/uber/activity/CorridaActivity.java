package com.yagocurvello.uber.activity;

import android.Manifest;
import android.content.Context;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.Button;

import com.yagocurvello.uber.R;
import com.yagocurvello.uber.model.Requisicao;
import com.yagocurvello.uber.model.Usuario;

public class CorridaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localMotorista;

    private Button buttonAceitar;
    private Requisicao requisicao;
    private Usuario motorista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);

        configIniciais();

        Bundle bundle = getIntent().getExtras();


        buttonAceitar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    private void configIniciais(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Iniciar Corrida");

        buttonAceitar = findViewById(R.id.buttonAceitarCorrida);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
                localMotorista = new LatLng(latitude,longitude);

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(localMotorista).title("Meu local")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localMotorista,18));

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

}