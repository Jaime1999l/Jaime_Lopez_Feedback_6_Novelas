package com.example.jaime_lopez_feedback_6_novelas.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jaime_lopez_feedback_6_novelas.PantallaPrincipalActivity;
import com.example.jaime_lopez_feedback_6_novelas.R;
import com.example.jaime_lopez_feedback_6_novelas.firebase.FirebaseHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister; // Declaramos el TextView para el enlace
    private FirebaseHandler firebaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseHandler = new FirebaseHandler();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);  // Inicializamos el TextView

        btnLogin.setOnClickListener(v -> login());

        // Configuramos el evento de clic para el TextView
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseHandler.obtenerUsuarioPorCorreo(email, task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String idCredenciales = document.getString("idCredenciales");

                    firebaseHandler.obtenerCredenciales(idCredenciales, credTask -> {
                        if (credTask.isSuccessful() && credTask.getResult().exists()) {
                            String storedPassword = credTask.getResult().getString("contrasena");
                            if (storedPassword.equals(password)) {
                                // Inicia sesión en FirebaseAuth
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(authTask -> {
                                            if (authTask.isSuccessful()) {
                                                guardarSesion(email);
                                                redirigirPantallaPrincipal();
                                            } else {
                                                Toast.makeText(this, "Error al autenticar en Firebase: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Error al verificar las credenciales", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void guardarSesion(String email) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userEmail", email)
                .apply();
    }

    private void redirigirPantallaPrincipal() {
        Intent intent = new Intent(LoginActivity.this, PantallaPrincipalActivity.class);
        startActivity(intent);
        finish();
    }
}
