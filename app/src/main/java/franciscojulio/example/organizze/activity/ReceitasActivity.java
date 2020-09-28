package franciscojulio.example.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import franciscojulio.example.organizze.R;
import franciscojulio.example.organizze.config.ConfiguracaoFirebase;
import franciscojulio.example.organizze.helper.Base64Custom;
import franciscojulio.example.organizze.helper.DateCustom;
import franciscojulio.example.organizze.model.Movimentacao;
import franciscojulio.example.organizze.model.Usuario;

public class ReceitasActivity extends AppCompatActivity {

    private EditText campoValor;
    private TextInputEditText campoData, campoCategoria, campoDescricao;
    private Movimentacao movimentacao;
    private Double receitaTotal;
    private DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);

        campoValor = findViewById(R.id.editTextValorID);
        campoData = findViewById(R.id.editTextData);
        campoCategoria = findViewById(R.id.editTextCategoria);
        campoDescricao = findViewById(R.id.editTextDescricao);

        campoData.setText( DateCustom.dataAtual() );

        SimpleMaskFormatter smf = new SimpleMaskFormatter("NN/NN/NNNN");
        MaskTextWatcher mtw = new MaskTextWatcher(campoData, smf);
        campoData.addTextChangedListener(mtw);

        recuperarReceitaTotal();


    }

    public void salvarReceita(View view){

        if( validarCamposReceitas() ) {
            Double valorRecuperado = Double.parseDouble( campoValor.getText().toString() );
            movimentacao = new Movimentacao();
            movimentacao.setValor( valorRecuperado );
            movimentacao.setCategoria( campoCategoria.getText().toString() );
            movimentacao.setDescricao( campoDescricao.getText().toString() );
            movimentacao.setData( campoData.getText().toString() );
            movimentacao.setTipo("r");

            Double receitaAtualizada = receitaTotal + valorRecuperado;

            atualizarReceita( receitaAtualizada );

            movimentacao.salvar();

            finish();
        }

    }

    public Boolean validarCamposReceitas(){
        String textoValor = campoValor.getText().toString();
        String textoData = campoData.getText().toString();
        String textoCategoria = campoCategoria.getText().toString();
        String textoDescricao = campoDescricao.getText().toString();

        if( !textoValor.isEmpty()){
            if( !textoData.isEmpty()){
                if( !textoCategoria.isEmpty()){
                    if( !textoDescricao.isEmpty()){
                        return true;
                    }else{
                        Toast.makeText(ReceitasActivity.this, "Descrição não informada", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }else{
                    Toast.makeText(ReceitasActivity.this, "Categoria não informada", Toast.LENGTH_LONG).show();
                    return false;
                }
            }else{
                Toast.makeText(ReceitasActivity.this, "Data não informada", Toast.LENGTH_LONG).show();
                return false;
            }
        }else{
            Toast.makeText(ReceitasActivity.this, "Valor não informado", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    public void recuperarReceitaTotal(){
        String idUsuario = Base64Custom.codificarBase64( auth.getCurrentUser().getEmail() );
        DatabaseReference usuarioRef = reference.child("usuarios").child( idUsuario );

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue( Usuario.class );
                receitaTotal = usuario.getReceitaTotal();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void atualizarReceita(Double receita){
        String idUsuario = Base64Custom.codificarBase64( auth.getCurrentUser().getEmail() );
        DatabaseReference usuarioRef = reference.child("usuarios").child( idUsuario );

        usuarioRef.child("receitaTotal").setValue( receita );

    }

}