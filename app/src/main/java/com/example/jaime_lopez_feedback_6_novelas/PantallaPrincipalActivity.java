package com.example.jaime_lopez_feedback_6_novelas;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.jaime_lopez_feedback_6_novelas.activity.LoginActivity;
import com.example.jaime_lopez_feedback_6_novelas.firebase.FirebaseHandler;
import com.example.jaime_lopez_feedback_6_novelas.model.Usuario;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class PantallaPrincipalActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Button btnLogout;
    private FirebaseHandler firebaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnLogout = findViewById(R.id.btnLogout);

        // Inicializar FirebaseHandler
        firebaseHandler = new FirebaseHandler();

        // Configurar el ActionBarDrawerToggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configurar el listener de navegación
        setupNavigationView();

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(PantallaPrincipalActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void setupNavigationView() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firebaseHandler.obtenerUsuario(userId, task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Usuario usuario = task.getResult().toObject(Usuario.class);

                navigationView.findViewById(R.id.nav_map).setOnClickListener(v -> {
                    startActivity(new Intent(this, Actividad.class));
                    drawerLayout.closeDrawer(GravityCompat.START);
                });
            } else {
                Toast.makeText(this, "No se pudo obtener la información del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}