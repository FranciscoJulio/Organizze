package franciscojulio.example.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import franciscojulio.example.organizze.R;
import franciscojulio.example.organizze.config.ConfiguracaoFirebase;
import franciscojulio.example.organizze.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editSenha;
    private Button botaoEntrar;
    private Usuario usuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmailLogin);
        editSenha = findViewById(R.id.editSenhaLogin);
        botaoEntrar = findViewById(R.id.buttonEntrarLogin);

        botaoEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txtEmail = editEmail.getText().toString();
                String txtSenha = editSenha.getText().toString();



                if( verificaCampos( txtEmail, txtSenha ) ){

                    usuario = new Usuario();
                    usuario.setEmail( txtEmail );
                    usuario.setSenha( txtSenha );

                    logarUsuario();


                }
            }
        });


    }

    public boolean verificaCampos( String stEmail, String stSenha){

        if( !stEmail.isEmpty() ){
            if( !stSenha.isEmpty() ){
                return true;
            }else{
                Toast.makeText(LoginActivity.this, "Preencha a senha!", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(LoginActivity.this, "Preencha o email!", Toast.LENGTH_LONG).show();
        }

        return false;
    }


    public void logarUsuario(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha() )
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if( task.isSuccessful() ){

                            abrirTelaPrincipal();

                        }else{
                            String excecao = "";
                            try{
                                throw task.getException();
                            }catch ( FirebaseAuthInvalidUserException e ) {
                                excecao = "Usuário não cadastrado";
                            }catch ( FirebaseAuthInvalidCredentialsException e ){
                                excecao =  "E-mail e senha não correspondem a um usuário cadastrado";
                            }catch ( Exception e){
                                excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                                e.printStackTrace();
                            }

                            Toast.makeText( LoginActivity.this, excecao, Toast.LENGTH_LONG).show();

                        }

                    }
                });

    }

    public void abrirTelaPrincipal(){
        startActivity( new Intent(this, PrincipalActivity.class ) );
        finish();
    }

}
