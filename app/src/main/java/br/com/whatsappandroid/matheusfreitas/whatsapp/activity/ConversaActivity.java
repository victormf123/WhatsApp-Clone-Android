package br.com.whatsappandroid.matheusfreitas.whatsapp.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.com.whatsappandroid.matheusfreitas.whatsapp.Adpter.MensagemAdapter;
import br.com.whatsappandroid.matheusfreitas.whatsapp.R;
import br.com.whatsappandroid.matheusfreitas.whatsapp.config.ConfiguracaoFirebase;
import br.com.whatsappandroid.matheusfreitas.whatsapp.helper.Base64Custom;
import br.com.whatsappandroid.matheusfreitas.whatsapp.helper.Preferencias;
import br.com.whatsappandroid.matheusfreitas.whatsapp.model.Conversa;
import br.com.whatsappandroid.matheusfreitas.whatsapp.model.Mensagem;

public class ConversaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editMensagem;
    private ImageButton btMensagem;
    private DatabaseReference firebase;
    private ListView listView;
    private ArrayList<Mensagem> mensagens;
    private ArrayAdapter<Mensagem> adapter;
    private ValueEventListener valueEventListenerMensagens;


    //Dados do destinatario
    private String nomeUsuarioDestinatario;
    private String idUsuarioDestinatario;

    //Dados do remetente
    private String idUsuarioRemetente;
    private String nomeUsuarioRemetente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);

        toolbar = (Toolbar) findViewById(R.id.tb_conversa);
        editMensagem = (EditText) findViewById(R.id.edit_mensagem);
        btMensagem = (ImageButton) findViewById(R.id.bt_enviar);
        listView = (ListView) findViewById(R.id.lv_conversas);

        //dados do usuario logado
        Preferencias preferencias = new Preferencias(ConversaActivity.this);
        idUsuarioRemetente = preferencias.getIdentificador();
        nomeUsuarioRemetente = preferencias.getNOME();

        Bundle extra = getIntent().getExtras();
        if(extra != null){
            nomeUsuarioDestinatario = extra.getString("nome");
            String emailDestinatario = extra.getString("email");
            idUsuarioDestinatario = Base64Custom.codificarBase64(emailDestinatario);
        }

        //Configurando toolbar
        toolbar.setTitle(nomeUsuarioDestinatario);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left);
        setSupportActionBar(toolbar);

        //Montando listview e adapter
        mensagens = new ArrayList<>();
        adapter = new MensagemAdapter(ConversaActivity.this, mensagens);
        listView.setAdapter(adapter);

        //Recuperar mensagensdo firebase
        firebase = ConfiguracaoFirebase.getFirebase()
                .child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);
        //Criar listener para mensagens
        valueEventListenerMensagens = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mensagens.clear();
                for (DataSnapshot dados: dataSnapshot.getChildren()){
                    Mensagem mensagem = dados.getValue(Mensagem.class);
                    mensagens.add(mensagem);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        firebase.addValueEventListener(valueEventListenerMensagens);

        //Enviar mensagem
        btMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textoMensagem = editMensagem.getText().toString();
                if(textoMensagem.isEmpty()){
                    Toast.makeText(ConversaActivity.this, "Digite uma mensagem para enviar", Toast.LENGTH_LONG).show();
                }else {

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioRemetente);
                    mensagem.setMensagem(textoMensagem);

                    // salvamos mensagem para o remetente
                   Boolean retornoMensagemRemetente = salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                   if(!retornoMensagemRemetente){
                       Toast.makeText(ConversaActivity.this, "Problema ao salvar mensagem, tente novamente", Toast.LENGTH_LONG).show();
                   }else{

                       // salvamos mensagem para o destinatario
                       Boolean retornoMensagemDestinatario = salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente, mensagem);
                       if(!retornoMensagemRemetente){
                           Toast.makeText(ConversaActivity.this, "Problema ao salvar mensagem do destinatário, tente novamente!", Toast.LENGTH_LONG).show();
                       }
                   }

                   // salvamos Conversa para o remetente
                    Conversa conversa = new Conversa();
                    conversa.setIdUsuario(idUsuarioDestinatario);
                    conversa.setNome(nomeUsuarioDestinatario);
                    conversa.setMensagem(textoMensagem);
                    Boolean retornoConversaRemetente = salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, conversa);
                    if(!retornoConversaRemetente){
                        Toast.makeText(ConversaActivity.this, "Problema ao salvar conversa, tente novamente!", Toast.LENGTH_LONG).show();
                    }else{
                        // salvamos Conversa para o destinatario
                        conversa = new Conversa();
                        conversa.setIdUsuario(idUsuarioRemetente);
                        conversa.setNome(nomeUsuarioRemetente);
                        conversa.setMensagem(textoMensagem);
                        salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, conversa);
                    }


                    editMensagem.setText("");

                }
            }
        });
    }

    private boolean salvarConversa(String idRemetente, String idDestinatario, Conversa conversa){
        try{
            firebase = ConfiguracaoFirebase.getFirebase().child("conversas");
            firebase.child(idRemetente)
                    .child(idDestinatario)
                    .setValue(conversa);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private boolean salvarMensagem(String idRemetente, String idDestinatario, Mensagem mensagem){
        try {

            firebase = ConfiguracaoFirebase.getFirebase().child("mensagens");
            firebase.child(idRemetente)
                    .child(idDestinatario)
                    .push().setValue(mensagem);


            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListenerMensagens);
    }
}
