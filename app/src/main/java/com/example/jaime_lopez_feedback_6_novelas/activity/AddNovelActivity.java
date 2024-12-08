package com.example.jaime_lopez_feedback_6_novelas.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.jaime_lopez_feedback_6_novelas.R;
import com.example.jaime_lopez_feedback_6_novelas.databaseSQL.SQLiteHelper;
import com.example.jaime_lopez_feedback_6_novelas.model.Novel;
import com.example.jaime_lopez_feedback_6_novelas.model.Ubicacion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AddNovelActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextAuthor, editTextYear, editTextSynopsis;
    private Button buttonSave;
    private FirebaseFirestore db;
    private SQLiteHelper sqliteHelper;
    private String novelId;

    private boolean isLowBattery = false;
    private BroadcastReceiver batteryReceiver;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_novel);

        // Inicializar Firestore y SQLiteHelper
        db = FirebaseFirestore.getInstance();
        sqliteHelper = new SQLiteHelper(this);

        // Inicializar componentes de la interfaz de usuario
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextAuthor = findViewById(R.id.edit_text_author);
        editTextYear = findViewById(R.id.edit_text_year);
        editTextSynopsis = findViewById(R.id.edit_text_synopsis);
        buttonSave = findViewById(R.id.button_save);

        // Inicializar ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configurar listener para el botón Guardar
        buttonSave.setOnClickListener(v -> saveNovel());

        // Verificar si se está editando una novela existente
        if (getIntent().hasExtra("EXTRA_ID")) {
            novelId = getIntent().getStringExtra("EXTRA_ID");
            loadNovelDetails(novelId);
        }

        // Monitorear el estado de la batería
        monitorBatteryState();
    }

    private void saveNovel() {
        String title = editTextTitle.getText().toString().trim();
        String author = editTextAuthor.getText().toString().trim();
        String yearString = editTextYear.getText().toString().trim();
        String synopsis = editTextSynopsis.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author) || TextUtils.isEmpty(yearString) || TextUtils.isEmpty(synopsis)) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Año inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Solicitar ubicación actual
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Generar ubicación aleatoria en base a la ubicación actual
                Ubicacion randomUbicacion = generateRandomLocation(location.getLatitude(), location.getLongitude(), 1000);
                Novel novel = new Novel(title, author, year, synopsis, randomUbicacion);
                saveNovelToDatabase(novel);
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNovelToDatabase(Novel novel) {
        String randomId = UUID.randomUUID().toString();
        novel.setId(randomId);

        db.collection("novelas").document(randomId).set(novel)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddNovelActivity.this, "Novela agregada", Toast.LENGTH_SHORT).show();
                    sqliteHelper.addNovel(novel);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddNovelActivity.this, "Error al agregar la novela", Toast.LENGTH_SHORT).show();
                });
    }

    private Ubicacion generateRandomLocation(double centerLat, double centerLng, int radiusInMeters) {
        double radiusInDegrees = radiusInMeters / 111000f;

        double u = Math.random();
        double v = Math.random();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double deltaLat = w * Math.cos(t);
        double deltaLng = w * Math.sin(t) / Math.cos(Math.toRadians(centerLat));

        double randomLat = centerLat + deltaLat;
        double randomLng = centerLng + deltaLng;

        return new Ubicacion(randomLat, randomLng, "Ubicación aleatoria");
    }

    private void loadNovelDetails(String novelId) {
        db.collection("novelas").document(novelId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Novel novel = documentSnapshot.toObject(Novel.class);
                    if (novel != null) {
                        editTextTitle.setText(novel.getTitle());
                        editTextAuthor.setText(novel.getAuthor());
                        editTextYear.setText(String.valueOf(novel.getYear()));
                        editTextSynopsis.setText(novel.getSynopsis());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddNovelActivity.this, "Error al cargar los detalles de la novela", Toast.LENGTH_SHORT).show();
                });
    }

    private void monitorBatteryState() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = (level / (float) scale) * 100;

                isLowBattery = batteryPct < 20;
                adjustScreenBrightness();
            }
        };
        registerReceiver(batteryReceiver, filter);
    }

    private void adjustScreenBrightness() {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        if (isLowBattery) {
            if (layoutParams.screenBrightness != 0.7f) {
                layoutParams.screenBrightness = 0.7f;
                getWindow().setAttributes(layoutParams);
                Toast.makeText(this, "Batería baja... se ha reducido el brillo.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (layoutParams.screenBrightness != WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
                getWindow().setAttributes(layoutParams);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adjustScreenBrightness();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveNovel();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
