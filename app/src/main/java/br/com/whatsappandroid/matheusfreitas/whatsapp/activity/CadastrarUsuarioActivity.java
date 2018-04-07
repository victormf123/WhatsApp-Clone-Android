package br.com.whatsappandroid.matheusfreitas.whatsapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import br.com.whatsappandroid.matheusfreitas.whatsapp.R;
import br.com.whatsappandroid.matheusfreitas.whatsapp.config.ConfiguracaoFirebase;
import br.com.whatsappandroid.matheusfreitas.whatsapp.helper.Base64Custom;
import br.com.whatsappandroid.matheusfreitas.whatsapp.helper.Preferencias;
import br.com.whatsappandroid.matheusfreitas.whatsapp.model.Usuario;

public class CadastrarUsuarioActivity extends AppCompatActivity {

    private EditText nome;
    private EditText email;
    private EditText senha;
    private Button botaoCadastrar;
    private Usuario usuario;
    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_usuario);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nome = (EditText) findViewById(R.id.edit_cadastro_nome);
        email = (EditText) findViewById(R.id.edit_cadastro_email);
        senha = (EditText) findViewById(R.id.edit_cadastro_senha);
        botaoCadastrar = (Button) findViewById(R.id.bt_cadastrar);

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usuario = new Usuario();
                usuario.setNome(nome.getText().toString());
                usuario.setEmail(email.getText().toString());
                usuario.setSenha(senha.getText().toString());
                cadastrarUsuario();

            }
        });
    }

    private void cadastrarUsuario() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
            autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(CadastrarUsuarioActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CadastrarUsuarioActivity.this, "Sucesso ao cadastrar usuário", Toast.LENGTH_LONG).show();

                        FirebaseUser usuarioFirebase = task.getResult().getUser();
                        String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setId(identificadorUsuario);
                        usuario.salvar();

                        Preferencias preferencias = new Preferencias(CadastrarUsuarioActivity.this);
                        preferencias.salvarUsuarioPreferencias(identificadorUsuario);

                        abririLoginUsuario();
                    }else {
                        String erroExcecao = "";

                        try{
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            erroExcecao = "Digite uma senha mais forte, contendo mais caracteres e com letras e números!";
                            e.printStackTrace();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            erroExcecao = "O e-mail digitado é inválido, digite um novo e-mail!";
                            e.printStackTrace();
                        } catch (FirebaseAuthUserCollisionException e) {
                            erroExcecao = "O e-mail já está em uso no App";
                            e.printStackTrace();
                        } catch (Exception e) {
                            erroExcecao = "Ao cadastrar usuário";
                            e.printStackTrace();
                        }
                        Toast.makeText(CadastrarUsuarioActivity.this, "Erro: " + erroExcecao , Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    public void abririLoginUsuario(){
        Intent intent = new Intent(CadastrarUsuarioActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
