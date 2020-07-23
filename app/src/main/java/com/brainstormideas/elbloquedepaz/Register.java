package com.brainstormideas.elbloquedepaz;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.brainstormideas.elbloquedepaz.extraClasses.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    EditText nombre_edt;
    EditText apellido_edt;
    EditText correo_edt;
    EditText pass1_edt;
    EditText pass2_edt;
    EditText numeroTelefono_edt;
    Button reg_btn;
    String nombre;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    FirebaseUser user;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference dbContactosReferencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        if(dbContactosReferencia!=null){
            dbContactosReferencia =
                    FirebaseDatabase.getInstance().getReference()
                            .child("Usuario").child(user.getUid()).child("Contactos");
        }

        nombre_edt = (EditText)findViewById(R.id.nombre_edt);
        apellido_edt = (EditText)findViewById(R.id.apellido_edt);
        correo_edt = (EditText)findViewById(R.id.correo_edt);
        pass1_edt = (EditText)findViewById(R.id.pass1_edt);
        pass2_edt = (EditText)findViewById(R.id.pass2_edt);
        numeroTelefono_edt = (EditText)findViewById(R.id.numeroTelefono_edt);
        reg_btn = (Button) findViewById(R.id.reg_btn);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario(){

        nombre = nombre_edt.getText().toString().trim();
        final String apellido = apellido_edt.getText().toString().trim();
        String email = correo_edt.getText().toString().trim();
        String pass1 = pass1_edt.getText().toString().trim();
        String pass2 = pass2_edt.getText().toString().trim();
        final String numeroTelefono = numeroTelefono_edt.getText().toString().trim();

        if(nombre.isEmpty() || nombre.length()<3){
            Toast.makeText(getApplicationContext(), "Debe escribir un nombre de almenos 3 carateres", Toast.LENGTH_SHORT).show();
            return;
        }
        if(apellido.isEmpty() || apellido.length() < 3){
            Toast.makeText(getApplicationContext(), "Debe escribir un apellido de almenos 3 carateres", Toast.LENGTH_SHORT).show();
            return;
        }
        if(email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(getApplicationContext(), "Debe escribir un email valido", Toast.LENGTH_SHORT).show();
            return;
        }
        if(numeroTelefono.isEmpty()){
            Toast.makeText(getApplicationContext(),"Debe escribir un numero de teleono", Toast.LENGTH_SHORT).show();
            return;
        }
        if(pass1.isEmpty() || pass1.length() < 4){
            Toast.makeText(getApplicationContext(), "Debe escribir una pass de almenos 4 carateres", Toast.LENGTH_SHORT).show();
            return;
        }
        if(pass2.isEmpty() || pass2.length() < 4){
            Toast.makeText(getApplicationContext(), "Vuelva a escribir la pass de almenos 4 carateres", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!pass1.equals(pass2)){
            Toast.makeText(getApplicationContext(), "Las pass no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registrando nuevo usuario...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, pass1).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    user = mAuth.getCurrentUser();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nombre).build();
                    user.updateProfile(profileUpdates);
                    DatabaseReference dbTemporalReference = FirebaseDatabase.getInstance().getReference()
                                    .child("Usuario").child(user.getUid());
                    dbTemporalReference.child("apellido").setValue(apellido);
                    dbTemporalReference.child("numero").setValue(numeroTelefono);
                    Toast.makeText(getApplicationContext(), "Ha sido registrado correctamente.",
                            Toast.LENGTH_SHORT).show();
                    mAuth.setLanguageCode("es");
                    user.sendEmailVerification();

                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                    builder.setCancelable(false);
                    builder.setTitle("Registro exitoso.");
                    builder.setMessage("Se ha registrado exitosamente a El Bloque de Paz: Boton de Panico. \n\nSe ha enviado un correo electronico a su cuenta para verificar el mismo. Gracias!");
                    builder.setNeutralButton("Entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            irALogin();
                        }
                    });
                    builder.show();


                } else {
                    // If sign in fails, display a message to the user.
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(), "Ya esta registrado una cuenta con este correo.",
                                Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                return;
                    }else{
                        Toast.makeText(getApplicationContext(), "Fallo al registrar esta cuenta.",
                                Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                return;
                    }

                }
                progressDialog.dismiss();

            }
        });

    }
    private void irALogin(){
        Intent i = new Intent(this, Login.class);
        startActivity(i);
    }

}
