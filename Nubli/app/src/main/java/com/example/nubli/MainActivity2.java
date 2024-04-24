package com.example.nubli;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;


public class MainActivity2 extends AppCompatActivity {


    EditText etcontra, etcontra2, etusuario;
    TextView tvregistro, btnentrar;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String TEXT_KEY_PREFIX = "savedText";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        etcontra = (EditText) findViewById(R.id.etcontra);
        etcontra2 = (EditText) findViewById(R.id.etcontra2);
        etusuario = (EditText) findViewById(R.id.etusuario);
        tvregistro = (TextView) findViewById(R.id.tvregistro);
        btnentrar = (TextView) findViewById(R.id.btnentrar);
        btnentrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contrasena = etcontra.getText().toString();
                String contrasena2 = etcontra2.getText().toString();
                String usuario = etusuario.getText().toString();
                if (contrasena.equals(contrasena2) && contrasena != "" && usuario != "" ) {
                    Intent intent2 = new Intent(MainActivity2.this, MainActivity.class);
                    intent2.putExtra("contrasena", contrasena);
                    intent2.putExtra("usuario", usuario);
                    intent2.putExtra("registro","registrado");
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    String vacio= " ";
                    editor.putString(TEXT_KEY_PREFIX + "1", vacio);
                    editor.putString(TEXT_KEY_PREFIX + "2", vacio);
                    editor.putString(TEXT_KEY_PREFIX + "3", vacio);
                    editor.apply();
                    startActivity(intent2);
                } else {
                    Toast.makeText(MainActivity2.this, "La contraseña no coincide ", Toast.LENGTH_SHORT).show();

                }
            }
        });
        tvregistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(TEXT_KEY_PREFIX + "1", etusuario.getText().toString());
        editor.putString(TEXT_KEY_PREFIX + "2", etcontra.getText().toString());
        editor.putString(TEXT_KEY_PREFIX + "3", etcontra2.getText().toString());
        editor.apply();
    }

}