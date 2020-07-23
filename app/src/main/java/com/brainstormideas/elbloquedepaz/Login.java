package com.brainstormideas.elbloquedepaz;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import com.google.firebase.auth.FirebaseUser;


public class Login extends AppCompatActivity {


    SessionManager session;
    EditText correolog_txt;
    EditText passlog_txt;
    Button login_btn;
    Button register_btn;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    private static final int RESQUEST_ASK_CODE_PERMISSION = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        solicitarPermisos();
        correolog_txt = (EditText)findViewById(R.id.correolog_txt);
        passlog_txt = (EditText)findViewById(R.id.passlog_txt);
        login_btn = (Button)findViewById(R.id.login_btn);
        register_btn = (Button)findViewById(R.id.register_btn);

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        session = new SessionManager(getApplicationContext());

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loguear();
            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Register.class);
                startActivity(i);
            }
        });

    }

    private void salir(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Salir");
        builder.setMessage("Desea salir de la aplicacion?");
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();

    }
    private void loguear(){

        String email = correolog_txt.getText().toString().trim();
        String pass = passlog_txt.getText().toString().trim();

        if(email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(getApplicationContext(), "Debe escribir un email valido", Toast.LENGTH_SHORT).show();
            return;
        }
        if(pass.isEmpty()){
            Toast.makeText(getApplicationContext(), "Pass vacia", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Iniciando sesion...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(user.isEmailVerified()){
                        Toast.makeText(getApplicationContext(), "Inicio de sesión exitoso. Bienvenido:" + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Este usuario no ha sido verificado.", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        return;
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(getApplicationContext(), "Inicio de sesión erroneo.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    return;
                }

                progressDialog.dismiss();
                session.createLoginSession(mAuth.getUid(),
                        mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getPhoneNumber(),mAuth.getCurrentUser().getEmail());
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

    }

    @Override
    public void onBackPressed() {
        salir();
    }
    public void solicitarPermisos(){

        int permisoSMS = ActivityCompat.checkSelfPermission(Login.this, Manifest.permission.SEND_SMS);
        int permisoLocation = ActivityCompat.checkSelfPermission(Login.this,Manifest.permission.ACCESS_FINE_LOCATION);
        int permisoState = ActivityCompat.checkSelfPermission(Login.this, Manifest.permission.READ_PHONE_STATE);
        int permisoCallPhone = ActivityCompat.checkSelfPermission(Login.this, Manifest.permission.CALL_PHONE);

        if(permisoSMS!= PackageManager.PERMISSION_GRANTED ||
                permisoLocation!=PackageManager.PERMISSION_GRANTED || permisoState!=PackageManager.PERMISSION_GRANTED
                || permisoCallPhone!=PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE}, RESQUEST_ASK_CODE_PERMISSION);
            }
        }
    }
}
