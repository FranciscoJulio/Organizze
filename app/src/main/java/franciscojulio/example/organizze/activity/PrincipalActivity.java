package franciscojulio.example.organizze.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import franciscojulio.example.organizze.R;
import franciscojulio.example.organizze.adapter.AdapterMovimentacao;
import franciscojulio.example.organizze.config.ConfiguracaoFirebase;
import franciscojulio.example.organizze.helper.Base64Custom;
import franciscojulio.example.organizze.model.Movimentacao;
import franciscojulio.example.organizze.model.Usuario;

public class PrincipalActivity extends AppCompatActivity {

    private TextView textoSaudacao, textoSaldo;
    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private Double despesaTotal = 0.0;
    private Double receitaTotal = 0.0;
    private Double resumoUsuario = 0.0;
    private String mesAnoSelecionado;

    private FirebaseAuth auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference reference = ConfiguracaoFirebase.getFirebaseDatabase();
    private DatabaseReference usuarioRef;
    private DatabaseReference movimentacaoRef;
    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacoes;
    private AdapterMovimentacao adapterMovimentacao;
    private List<Movimentacao> movimentacoesList = new ArrayList<>();
    private Movimentacao movimentacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Organizze");
        setSupportActionBar(toolbar);


        calendarView = findViewById( R.id.calendarViewID );
        textoSaldo = findViewById( R.id.textSaldoID );
        textoSaudacao = findViewById( R.id.textSaudacaoID );
        recyclerView = findViewById( R.id.recyclerViewID );

        configuraCalendarView();
        swipe();

        adapterMovimentacao = new AdapterMovimentacao(movimentacoesList,this );

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager( layoutManager );
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter( adapterMovimentacao );

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarResumo();
        recuperarMovimentacoes();
    }

    public void swipe(){

        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

                int dragFlags = ItemTouchHelper.ACTION_STATE_IDLE;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

                return makeMovementFlags( dragFlags, swipeFlags );
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                excluirMovimentacao( viewHolder );
            }
        };

        new ItemTouchHelper( itemTouch ).attachToRecyclerView( recyclerView );

    }

    public void excluirMovimentacao(final RecyclerView.ViewHolder viewHolder){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Excluir movimentação da conta");
        alertDialog.setMessage("Você tem certeza que deseja excluir a movimentação?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    int position = viewHolder.getAdapterPosition();
                    movimentacao = movimentacoesList.get( position );

                String idUsuario = Base64Custom.codificarBase64( auth.getCurrentUser().getEmail() );
                usuarioRef = reference.child("usuarios").child( idUsuario );

                movimentacaoRef = reference.child("movimentacao")
                        .child( idUsuario )
                        .child( mesAnoSelecionado );

                movimentacaoRef.child( movimentacao.getKey() ).removeValue();
                adapterMovimentacao.notifyItemRemoved( position );
                atualizarSaldo();

            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PrincipalActivity.this, "Cancelado", Toast.LENGTH_LONG).show();
                adapterMovimentacao.notifyDataSetChanged();
            }
        });

        AlertDialog alert = alertDialog.create();

        alert.show();
    }

    public void atualizarSaldo(){

        String idUsuario = Base64Custom.codificarBase64( auth.getCurrentUser().getEmail() );
        usuarioRef = reference.child("usuarios").child( idUsuario );

        if( movimentacao.getTipo().equals("r")){
            receitaTotal = receitaTotal - movimentacao.getValor();
            usuarioRef.child("receitaTotal").setValue( receitaTotal );
        }else{
            despesaTotal = despesaTotal - movimentacao.getValor();
            usuarioRef.child("despesaTotal").setValue( despesaTotal );
        }

    }

    public void recuperarMovimentacoes(){

        String idUsuario = Base64Custom.codificarBase64( auth.getCurrentUser().getEmail() );
        usuarioRef = reference.child("usuarios").child( idUsuario );

        movimentacaoRef = reference.child("movimentacao")
                                    .child( idUsuario )
                                    .child( mesAnoSelecionado );

        valueEventListenerMovimentacoes = movimentacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                movimentacoesList.clear();

                for ( DataSnapshot dados: dataSnapshot.getChildren() ){
                    Movimentacao movimentacao = dados.getValue( Movimentacao.class );
                    movimentacao.setKey( dados.getKey() );
                    movimentacoesList.add( movimentacao );
                }

                adapterMovimentacao.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void recuperarResumo(){
        String idUsuario = Base64Custom.codificarBase64( auth.getCurrentUser().getEmail() );
        usuarioRef = reference.child("usuarios").child( idUsuario );

        valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Usuario usuario = dataSnapshot.getValue( Usuario.class );

                despesaTotal = usuario.getDespesaTotal();
                receitaTotal = usuario.getReceitaTotal();
                resumoUsuario = receitaTotal - despesaTotal;

                DecimalFormat decimalFormat = new DecimalFormat("0.00");

                String resultadoFormatado = decimalFormat.format( resumoUsuario );

                resultadoFormatado = resultadoFormatado.replace(".", ",");

                textoSaudacao.setText("Olá, " + usuario.getNome() );
                textoSaldo.setText( "R$ " + resultadoFormatado );

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ( item.getItemId() ){
            case R.id.menuSair:
                auth.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void adicionarDespesa(View view){
        startActivity(new Intent(this, DespesasActivity.class));
    }

    public void adicionarReceita(View view){
        startActivity(new Intent(this, ReceitasActivity.class));
    }

    public void configuraCalendarView(){

        CalendarDay dataAtual = calendarView.getCurrentDate();
        String mesSelecionado = String.format( "%02d", (dataAtual.getMonth()) );
        mesAnoSelecionado = String.valueOf( mesSelecionado + "" + dataAtual.getYear());

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String mesSelecionado = String.format( "%02d", (date.getMonth()) );
                mesAnoSelecionado = String.valueOf( mesSelecionado + "" + date.getYear() );

                movimentacaoRef.removeEventListener( valueEventListenerMovimentacoes );
                recuperarMovimentacoes();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioRef.removeEventListener( valueEventListenerUsuario );
        movimentacaoRef.removeEventListener( valueEventListenerMovimentacoes );

    }
}