package com.yagocurvello.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.yagocurvello.uber.R;
import com.yagocurvello.uber.config.ConfigFirebase;
import com.yagocurvello.uber.helper.UsuarioFirebase;
import com.yagocurvello.uber.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    //Interface
    private EditText editTextNome, editTextEmail, editTextSenha;
    private TextView textViewEntrar;
    private Button buttonSalvar;
    private Switch switchTipo;

    private Usuario usuario;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        configIniciais();

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usuario = new Usuario();
                usuario.setName(editTextNome.getText().toString());
                usuario.setEmail(editTextEmail.getText().toString());
                usuario.setSenha(editTextSenha.getText().toString());
                if (switchTipo.isChecked()){
                    usuario.setMotorista(true);
                }

                if (verificaTexto(usuario)){
                    cadastrar();
                }
            }
        });

        textViewEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CadastroActivity.this, EntrarActivity.class));
                finish();
            }
        });

    }

    private void configIniciais(){
        editTextNome = findViewById(R.id.editTextNome);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextSenha = findViewById(R.id.editTextSenha);
        textViewEntrar = findViewById(R.id.textEnrar);
        buttonSalvar = findViewById(R.id.buttonSalvarCadastro);
        switchTipo = findViewById(R.id.switchLogin);

        auth = ConfigFirebase.getFirebaseAutenticacao();

    }

    private boolean verificaTexto(Usuario usuario){
        if (!usuario.getName().isEmpty()){
            if (!usuario.getEmail().isEmpty()){
                if (!usuario.getSenha().isEmpty()){
                    return true;
                }else {
                    Toast.makeText(this, "Preencha sua Senha", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }else {
                Toast.makeText(this, "Preencha seu Email", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else {
            Toast.makeText(this, "Preencha seu Nome", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void cadastrar(){
        auth.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    try {
                        Toast.makeText(getApplicationContext(), "Usuario Cadastrado com sucesso", Toast.LENGTH_SHORT).show();
                        usuario.setIdUsuario(auth.getUid());
                        usuario.salvar();
                        UsuarioFirebase.atualizarNomeUsuarioFb(usuario.getName());
                        if (usuario.isMotorista()){
                            startActivity(new Intent(CadastroActivity.this, MotoristaActivity.class));
                            finish();
                        }else {
                            startActivity(new Intent(CadastroActivity.this, MapsActivity.class));
                            finish();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                } else {
                    String error;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        error = "Senha fraca";
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        error = "email inválido";
                    } catch (FirebaseAuthUserCollisionException e){
                        error = "email já cadastrado";
                    }catch (Exception e){
                        error = "Erro: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}