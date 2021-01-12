package com.yagocurvello.uber.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.yagocurvello.uber.R;
import com.yagocurvello.uber.config.ConfigFirebase;

public class LoginActivity extends AppCompatActivity {

    private Button buttonEntrar, buttonCadastrar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonEntrar = findViewById(R.id.buttonEntrar);
        buttonCadastrar = findViewById(R.id.buttonCadastrar);

        auth = ConfigFirebase.getFirebaseAutenticacao();
        if (auth.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, MapsActivity.class));
            finish();
        }

        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, CadastroActivity.class));
                finish();
            }
        });
    }
}