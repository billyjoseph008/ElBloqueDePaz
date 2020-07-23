package com.brainstormideas.elbloquedepaz;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.brainstormideas.elbloquedepaz.extraClasses.SessionManager;
import com.brainstormideas.elbloquedepaz.extraClasses.Contacto;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class configuracion extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    Button boton_seleccion;
    Button aceptar_btn;
    ListView listviewcontactos;
    Button limpiar_btn;
    CheckBox checkBox;
    
    ArrayList<String> contactos = new ArrayList<>();
    ArrayList<String> listaDeNumerosLocal = new ArrayList<>();
    ArrayAdapter<String> adapterBaseContactos;
    Query query;

    static final int REQUEST_SELECT_PHONE_NUMBER = 1;

    ArrayList<String> numerosConsultados = new ArrayList<>();

    SessionManager session;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference dbContactosReferencia;

    String nombre;
    String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        this.setTitle("Configuracion");

        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        session = new SessionManager(getApplicationContext());
        dbContactosReferencia =
                FirebaseDatabase.getInstance().getReference()
                        .child("Usuario").child(user.getUid()).child("Contactos");
        listviewcontactos = (ListView)findViewById(R.id.contactos);
        boton_seleccion = (Button)findViewById(R.id.boton_seleccion);
        limpiar_btn = (Button)findViewById(R.id.limpiar_btn);

        adapterBaseContactos = new ArrayAdapter<String>(this, R.layout.item_numeros, contactos);

        aceptar_btn = (Button)findViewById(R.id.aceptar_btn);

        checkBox = (CheckBox)findViewById(R.id.checkBox);
        if(session.isLlamar()){
            checkBox.setChecked(true);
        }else{
            checkBox.setChecked(false);
        }
        dbContactosReferencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    limpiar_btn.setVisibility(View.INVISIBLE);
                    adapterBaseContactos.notifyDataSetChanged();
                }else {
                    limpiar_btn.setVisibility(View.VISIBLE);
                    adapterBaseContactos.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });



        //CAMBIAR adapter PRUEBA POR NORMAL
        listviewcontactos.setAdapter(adapterBaseContactos);


        boton_seleccion.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 selectContact();
             }
         });
        aceptar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continuar();
            }
        });




        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    String myChildvalues = dataSnapshot.getValue(Contacto.class).toString();
                    contactos.add(myChildvalues);
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

        dbContactosReferencia.addChildEventListener(childEventListener);

        limpiar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                limpiar();
            }
        });

        query = dbContactosReferencia.orderByKey().limitToFirst(50);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot data: dataSnapshot.getChildren()){
                        String info = data.child("numero").getValue().toString();
                        numerosConsultados.add(info);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    // Aqui termina el OnCreate

    private void consultarNumeros(){

    }
    
    private void limpiar(){
        dbContactosReferencia.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(),"Se ha limpiado el registro.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void continuar() {
        if(checkBox.isChecked()){
            session.setLlamar(true);
        }else {
            session.setLlamar(false);
        }
       Intent i = new Intent(this, MainActivity.class);
       startActivity(i);
    }

    public void selectContact() {
        // Start an activity for the user to pick a phone number from contacts
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER);
        }
    }

    private boolean existeNumero(ArrayList<String> lista, String valor){
            for(int i =0; i<lista.size();i++){
                if(lista.get(i).equals(valor)){
                    return true;
                }
            }
            return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Data.DISPLAY_NAME};
            Cursor cursor = getContentResolver().query(contactUri, projection,
                    null, null, null);
            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nombreIndex = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
                number = cursor.getString(numberIndex);
                nombre = cursor.getString(nombreIndex);

                if(!numerosConsultados.isEmpty()){
                    if(!existeNumero(numerosConsultados,number)&&!existeNumero(listaDeNumerosLocal,number)){
                        Contacto nuevoContacto = new Contacto(nombre, number);
                        listaDeNumerosLocal.add(nuevoContacto.getNumero());
                        databaseReference.child("Usuario").child(user.getUid()).child("Contactos").push().setValue(nuevoContacto);
                        Toast.makeText(getApplicationContext(), nombre + " agregado correctamente.",Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "Ya agregaste este contacto.", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Contacto nuevoContacto = new Contacto(nombre, number);
                    listaDeNumerosLocal.add(nuevoContacto.getNumero());
                    databaseReference.child("Usuario").child(user.getUid()).child("Contactos").push().setValue(nuevoContacto);
                    Toast.makeText(getApplicationContext(), nombre + " agregado correctamente.",Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
    @Override
    public void onBackPressed() {

    }

}
