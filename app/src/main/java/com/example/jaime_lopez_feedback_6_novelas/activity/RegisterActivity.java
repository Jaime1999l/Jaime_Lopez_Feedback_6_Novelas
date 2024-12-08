package com.example.jaime_lopez_feedback_6_novelas.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.jaime_lopez_feedback_6_novelas.PantallaPrincipalActivity;
import com.example.jaime_lopez_feedback_6_novelas.R;
import com.example.jaime_lopez_feedback_6_novelas.firebase.FirebaseHandler;
import com.example.jaime_lopez_feedback_6_novelas.model.Credenciales;
import com.example.jaime_lopez_feedback_6_novelas.model.Ubicacion;
import com.example.jaime_lopez_feedback_6_novelas.model.Usuario;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Spinner spRole;
    private Button btnRegister;
    private FirebaseHandler firebaseHandler;
    private FusedLocationProviderClient fusedLocationClient;
    private Ubicacion ubicacionActual;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseHandler = new FirebaseHandler();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spRole = findViewById(R.id.spRole);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> checkLocationAndRegister());
    }

    private void checkLocationAndRegister() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchUserLocation(this::register);
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchUserLocation(Runnable onLocationFetched) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Verificar si GPS o red están habilitados
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "Por favor, activa el GPS o la red para obtener la ubicación.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(1000);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = getValidLocation(locationResult);
                if (location != null) {
                    ubicacionActual = new Ubicacion(location.getLatitude(), location.getLongitude(), "Ubicación actual");
                    fusedLocationClient.removeLocationUpdates(this);
                    onLocationFetched.run();
                } else {
                    Toast.makeText(RegisterActivity.this, "No se pudo obtener una ubicación válida. Intentando proveedor de red.", Toast.LENGTH_LONG).show();
                    fetchLocationFromNetwork(onLocationFetched);
                }
            }
        }, null);
    }

    private void fetchLocationFromNetwork(Runnable onLocationFetched) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location networkLocation =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (networkLocation != null && isValidLocation(networkLocation)) {
            ubicacionActual = new Ubicacion(networkLocation.getLatitude(), networkLocation.getLongitude(), "Ubicación de red");
            onLocationFetched.run();
        } else {
            Toast.makeText(this, "No se pudo obtener la ubicación. Reintente activando GPS.", Toast.LENGTH_LONG).show();
        }
    }

    private Location getValidLocation(LocationResult locationResult) {
        for (Location location : locationResult.getLocations()) {
            if (location != null && isValidLocation(location)) {
                return location;
            }
        }
        return null;
    }

    private boolean isValidLocation(Location location) {
        if (location == null) return false;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spRole.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ubicacionActual == null) {
            Toast.makeText(this, "Ubicación no disponible. Por favor, intente nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            Credenciales credenciales = new Credenciales(userId, password);
                            Usuario usuario = new Usuario(userId, name, email, role, ubicacionActual, userId);

                            firebaseHandler.guardarUsuario(usuario, taskUsuario -> {
                                if (taskUsuario.isSuccessful()) {
                                    firebaseHandler.guardarCredenciales(credenciales, taskCredenciales -> {
                                        if (taskCredenciales.isSuccessful()) {
                                            onRegisterSuccess(email);
                                        } else {
                                            Toast.makeText(this, "Error al guardar credenciales", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(this, "Error al guardar usuario", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(this, "Error al registrar en FirebaseAuth: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error durante el registro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void onRegisterSuccess(String email) {
        try {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, PantallaPrincipalActivity.class));
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation(this::register);
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
