package com.yagocurvello.uber.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.yagocurvello.uber.R;
import com.yagocurvello.uber.config.ConfigFirebase;
import com.yagocurvello.uber.helper.UsuarioFirebase;
import com.yagocurvello.uber.model.Usuario;

public class EntrarActivity extends AppCompatActivity {

    //Interface
    private EditText editTextEmail, editTextSenha;
    private TextView textViewCadastrar;
    private Button buttonEntrar;
    private Usuario usuario;

    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrar);

        configIniciais();

        buttonEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verificaTexto()){
                    entrar();
                }
            }
        });

        textViewCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EntrarActivity.this, CadastroActivity.class));
                finish();
            }
        });
    }

    private void configIniciais(){
        editTextEmail = findViewById(R.id.editTextEmailLogin);
        editTextSenha = findViewById(R.id.editTextSenhaLogin);
        textViewCadastrar = findViewById(R.id.textCadastro);
        buttonEntrar = findViewById(R.id.buttonEntrar);

        auth = ConfigFirebase.getFirebaseAutenticacao();
        reference = ConfigFirebase.getFirebaseDatabase().child("usuarios");

    }

    private boolean verificaTexto(){
            if (!editTextEmail.getText().toString().isEmpty()){
                if (!editTextSenha.getText().toString().isEmpty()){
                    return true;
                }else {
                    Toast.makeText(this, "Preencha sua Senha", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else {
                Toast.makeText(this, "Preencha seu Email", Toast.LENGTH_SHORT).show();
                return false;
            }
    }

    private void entrar(){
        String email = editTextEmail.getText().toString();
        String senha = editTextSenha.getText().toString();

        auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    UsuarioFirebase.redirecionaUsuario(EntrarActivity.this);
                }else {
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }
}