package franciscojulio.example.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import franciscojulio.example.organizze.R;
import franciscojulio.example.organizze.config.ConfiguracaoFirebase;
import franciscojulio.example.organizze.helper.Base64Custom;
import franciscojulio.example.organizze.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private Button botaoCadastrar;
    private Usuario usuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById( R.id.editNomeID );
        campoEmail = findViewById( R.id.editEmailID );
        campoSenha = findViewById( R.id.editSenhaID );
        botaoCadastrar = findViewById( R.id.botaoCadastrarID );

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                if( verificaCampos(textoNome, textoEmail, textoSenha) ){

                        usuario = new Usuario();
                        usuario.setNome( textoNome );
                        usuario.setEmail( textoEmail );
                        usuario.setSenha( textoSenha );

                        cadastrarUsuario();

                }

            }
        });

    }




    public boolean verificaCampos(String cpNome, String cpEmail, String cpSenha){

        if( !cpNome.isEmpty() ){
            if( !cpEmail.isEmpty() ){
                if( !cpSenha.isEmpty() ){
                        return true;
                }else{
                    Toast.makeText(CadastroActivity.this, "Preencha a senha!", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(CadastroActivity.this, "Preencha o email!", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(CadastroActivity.this, "Preencha o nome!", Toast.LENGTH_LONG).show();
        }

        return false;
    }

    public void cadastrarUsuario(){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if( task.isSuccessful() ){

                    String idUsuario = Base64Custom.codificarBase64( usuario.getEmail() );
                    usuario.setIdUsuario( idUsuario );
                    usuario.salvar();

                    finish();

                }else{

                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch ( FirebaseAuthWeakPasswordException e ){
                        excecao = "Digite uma senha de no mínimo 6 digitos";
                    }catch ( FirebaseAuthInvalidCredentialsException e ){
                        excecao = "Digite um email válido";
                    }catch ( FirebaseAuthUserCollisionException e ){
                        excecao = "Esta conta já está cadastrada";
                    }catch ( Exception e ){
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this,
                            excecao,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}
