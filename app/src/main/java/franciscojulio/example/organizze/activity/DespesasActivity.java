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

public class DespesasActivity extends AppCompatActivity {

    private EditText campoValor;
    private TextInputEditText campoData, campoCategoria, campoDescricao;
    private Movimentacao movimentacao;
    private Double despesaTotal;
    private DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despesas);

        campoValor = findViewById(R.id.editTextValor);
        campoData = findViewById(R.id.editTextDataID);
        campoCategoria = findViewById(R.id.editTextCategoriaID);
        campoDescricao = findViewById(R.id.editTextDescricaoID);


        //Preencher o campo data com a data atual
        campoData.setText( DateCustom.dataAtual() );

        SimpleMaskFormatter smf = new SimpleMaskFormatter("NN/NN/NNNN");
        MaskTextWatcher mtw = new MaskTextWatcher(campoData, smf);
        campoData.addTextChangedListener(mtw);

        recuperarDespesaTotal();

    }

    public void salvarDespesa(View view){

        if( validarCamposDespesas() ) {
            Double valorRecuperado = Double.parseDouble( campoValor.getText().toString() );
            movimentacao = new Movimentacao();
            movimentacao.setValor( valorRecuperado );
            movimentacao.setCategoria( campoCategoria.getText().toString() );
            movimentacao.setDescricao( campoDescricao.getText().toString() );
            movimentacao.setData( campoData.getText().toString() );
            movimentacao.setTipo("d");

            Double despesaAtualizada = despesaTotal + valorRecuperado;

            atualizarDespesa( despesaAtualizada );

            movimentacao.salvar();

            finish();
        }

    }

    public Boolean validarCamposDespesas(){
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
                        Toast.makeText(DespesasActivity.this, "Descrição não informada", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }else{
                    Toast.makeText(DespesasActivity.this, "Categoria não informada", Toast.LENGTH_LONG).show();
                    return false;
                }
            }else{
                Toast.makeText(DespesasActivity.this, "Data não informada", Toast.LENGTH_LONG).show();
                return false;
            }
        }else{
            Toast.makeText(DespesasActivity.this, "Valor não informado", Toast.LENGTH_LONG).show();
            return false;
        }

    }
    public void recuperarDespesaTotal(){
        String idUsuario = Base64Custom.codificarBase64( auth.getCurrentUser().getEmail() );
        DatabaseReference usuarioRef = reference.child("usuarios").child( idUsuario );

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue( Usuario.class );
                despesaTotal = usuario.getDespesaTotal();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void atualizarDespesa(Double despesa){
        String idUsuario = Base64Custom.codificarBase64( auth.getCurrentUser().getEmail() );
        DatabaseReference usuarioRef = reference.child("usuarios").child( idUsuario );

        usuarioRef.child("despesaTotal").setValue( despesa );

    }

}