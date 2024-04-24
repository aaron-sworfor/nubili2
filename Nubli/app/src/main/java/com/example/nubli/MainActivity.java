package com.example.nubli;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    EditText etcontra,etusuario;
    private String registro = " ";
    TextView tvregistro,btnentrar;
    String usuario= "nulo", contrasena= "nulo";
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String TEXT_KEY_PREFIX = "savedText";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etcontra=(EditText) findViewById(R.id.etcontra);
        etusuario=(EditText) findViewById(R.id.etusuario);
        tvregistro=(TextView) findViewById(R.id.tvregistro);
        btnentrar=(TextView) findViewById(R.id.btnentrar);

        btnentrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etusuario.getText().toString();
                String con = etcontra.getText().toString();
                if (con.equalsIgnoreCase(contrasena) && user.equalsIgnoreCase(usuario)){
                    Intent intent =new Intent(MainActivity.this, MainActivity3.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(MainActivity.this, "Contraseña y usuario incorrectos " + usuario + "," + contrasena, Toast.LENGTH_SHORT).show();
                }
            }
        });
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Obtener correo y contraseña
            usuario = extras.getString("usuario");
            contrasena = extras.getString("contrasena");
            registro = extras.getString("registro");
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(TEXT_KEY_PREFIX + "1", registro);
            editor.putString(TEXT_KEY_PREFIX + "2", usuario);
            editor.putString(TEXT_KEY_PREFIX + "3", contrasena);
            editor.apply();
        }
        tvregistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });
        loadSavedText();
        if (registro.equals("registrado")) {
            Executor executor = ContextCompat.getMainExecutor(this);

            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    moveNextActivity();
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(MainActivity.this, "Error de autenticación: " + errString, Toast.LENGTH_SHORT).show();
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Autenticación mediante huella digital")
                    .setSubtitle("Escanea tu huella para continuar")
                    .setNegativeButtonText("Cancelar")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        } else {
            Toast.makeText(this, "Debes registrarte para poder usar la autenticación mediante huella digital", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveNextActivity() {
        Intent intent = new Intent(this, MainActivity3.class);
        startActivity(intent);
    }

    private void loadSavedText() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        registro= settings.getString(TEXT_KEY_PREFIX + "1", "");
        usuario= settings.getString(TEXT_KEY_PREFIX + "2", "");
        contrasena= settings.getString(TEXT_KEY_PREFIX + "3", "");
    }
}