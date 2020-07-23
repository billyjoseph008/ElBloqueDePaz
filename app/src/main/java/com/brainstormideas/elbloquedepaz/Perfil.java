package com.brainstormideas.elbloquedepaz;

import android.app.ProgressDialog;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brainstormideas.elbloquedepaz.extraClasses.Contacto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Perfil extends AppCompatActivity {

    TextView nombre_txt;
    TextView apellido_txt;
    TextView telefono_txt;
    EditText recuperarPass_edb;
    Button recuperarPass_btn;
    ListView listView;
    String email="";

    FirebaseUser user;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference dbContactosReferencia;
    DatabaseReference dbContactosReferenciaLista;
    private ProgressDialog mprogressdialg;

    ArrayList<String> contactosFirebase = new ArrayList<>();
    ArrayAdapter<String> adapterBaseContactos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        this.setTitle("Perfil");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        dbContactosReferencia =
                FirebaseDatabase.getInstance().getReference()
                        .child("Usuario").child(user.getUid());
        dbContactosReferenciaLista =
                FirebaseDatabase.getInstance().getReference()
                        .child("Usuario").child(user.getUid()).child("Contactos");

        nombre_txt = (TextView)findViewById(R.id.nombre_txt);
        apellido_txt = (TextView)findViewById(R.id.apellido_txt);
        telefono_txt = (TextView)findViewById(R.id.telefono_txt);
        recuperarPass_edb = (EditText)findViewById(R.id.recuperarPass_edb);
        recuperarPass_btn = (Button)findViewById(R.id.recuperarPass_btn);
        listView = (ListView)findViewById(R.id.contactos);
        mprogressdialg = new ProgressDialog(this);

        adapterBaseContactos = new ArrayAdapter<String>(this, R.layout.item_numeros, contactosFirebase);
        listView.setAdapter(adapterBaseContactos);

        nombre_txt.setText("Nombre: " + user.getDisplayName());

        recuperarPass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = recuperarPass_edb.getText().toString().trim();
                if(!email.isEmpty()){
                    mprogressdialg.setMessage("Enviando solicitud...");
                    mprogressdialg.setCanceledOnTouchOutside(false);
                    mprogressdialg.show();
                    resetPassword();
                }else{
                    Toast.makeText(getApplicationContext(), "Debe ingresar un email.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dbContactosReferencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String apellido = dataSnapshot.child("apellido").getValue().toString();
                    String telefono = dataSnapshot.child("numero").getValue().toString();
                    apellido_txt.setText("Apellido: "+ apellido);
                    telefono_txt.setText("Telefono: " + telefono);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String myChildvalues = dataSnapshot.getValue(Contacto.class).toString();
                contactosFirebase.add(myChildvalues);
                adapterBaseContactos.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                adapterBaseContactos.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        dbContactosReferenciaLista.addChildEventListener(childEventListener);
    }

    private void resetPassword() {
        mAuth.setLanguageCode("es");
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Se envio un correo de restablecimiento a esta direccion.", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"No se pudo completar su operacion.", Toast.LENGTH_SHORT).show();
                }
                mprogressdialg.dismiss();
            }
        });
    }
}
