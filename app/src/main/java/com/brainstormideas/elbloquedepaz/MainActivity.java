package com.brainstormideas.elbloquedepaz;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brainstormideas.elbloquedepaz.extraClasses.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements LocationListener {


    SessionManager session;
    ImageButton enviar;
    String direccion;
    String latitud;
    String longitud;
    public String mensaje;
    ImageView indicador_rec;
    ImageView indicador_call;

    TextView lat;
    TextView lon;
    TextView dir;
    FirebaseUser user;
    FirebaseAuth mAuth;
    Query query;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference dbContactosReferencia;

    ArrayList<String> listaDeContactos;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (mAuth.getUid() == null) {
            irALogin();
            return;
        }

        this.setTitle("¡" + user.getDisplayName() + "! ¿Estás en peligro?");

        listaDeContactos = new ArrayList<>();



        session = new SessionManager(getApplicationContext());firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        dbContactosReferencia =
                FirebaseDatabase.getInstance().getReference()
                        .child("Usuario").child(user.getUid()).child("Contactos");
        session.checkLogin();

        dbContactosReferencia.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), "No se ha agregado ningun contacto.", Toast.LENGTH_SHORT).show();
                    irAConfiguracion();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        enviar = (ImageButton) findViewById(R.id.enviar);
        indicador_rec = (ImageView) findViewById(R.id.indicador_rec);
        indicador_call = (ImageView) findViewById(R.id.indicador_call);

        if (session.isLlamar()){
            indicador_call.setAlpha(255);
        }else {
            indicador_call.setAlpha(120);
        }

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (direccion != null) {
                    view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_anim));
                    mensaje = "Hola estoy en peligro." + "\n Latitud = "
                            + latitud + "\n Longitud = " + longitud + "\n Mi direccion actual:" + direccion;
                    for (String numero : listaDeContactos) {
                        enviarMensaje(numero, mensaje);
                    }
                    if (session.isLlamar()) {
                        Intent i = new Intent(android.content.Intent.ACTION_CALL,
                                Uri.parse("tel:911"));
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        startActivity(i);
                   }
               }else{
                   Toast.makeText(getApplicationContext(),"Aun no se carga la informacion.", Toast.LENGTH_SHORT).show();
               }


            }
        });

        query = dbContactosReferencia.orderByKey().limitToFirst(50);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot data: dataSnapshot.getChildren()){
                        String info = data.child("numero").getValue().toString();
                        String finalInfo = info.replaceAll(" ","").replaceAll("\\+52 1","");
                        listaDeContactos.add(finalInfo);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5, this);


    }
    //Aqui terminae el OnCreate

    private void irALogin() {
        Intent i = new Intent(this, Login.class);
        startActivity(i);
    }

    public void irAConfiguracion(){
        Intent i = new Intent(this,configuracion.class);
        startActivity(i);
    }

    public void enviarMensaje(String numero, String mensaje){
        try{
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(numero, null, mensaje, null, null);
            Toast.makeText(getApplicationContext(), "Enviado correctamente", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "No se pudo enviar el mensaje", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void setLocation(Location location) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud

        if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    direccion = DirCalle.getAddressLine(0);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location){

        lat = (TextView)findViewById(R.id.lat);
        lon = (TextView)findViewById(R.id.lon);
        dir = (TextView)findViewById(R.id.dir);

        latitud = String.valueOf(location.getLatitude());
        longitud = String.valueOf(location.getLongitude());

        this.setLocation(location);

        lat.setText("Latitud: " + latitud);
        lon.setText("Longitud: " + longitud);
        dir.setText("Direccion: " + direccion);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {

            AlertDialog.Builder ventana = new AlertDialog.Builder(this);
            ventana.setTitle("Salir");
            ventana.setMessage("Seguro que desea cerrar sesion?");
            ventana.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    signOut();
                }
            });
            ventana.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            ventana.show();

            return true;
        }
        if (id == R.id.configuracion){
            irAConfiguracion();
            return true;
        }
        if (id == R.id.perfil){
            irAPerfil();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void irAPerfil() {
        Intent i = new Intent(this, Perfil.class);
        startActivity(i);

    }

    private void signOut() {
        mAuth.signOut();
        session.logoutUser();
    }

    @Override
    public void onBackPressed() {

    }

}
