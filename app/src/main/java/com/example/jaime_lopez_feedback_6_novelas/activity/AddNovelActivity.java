package com.example.jaime_lopez_feedback_6_novelas.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jaime_lopez_feedback_6_novelas.R;
import com.example.jaime_lopez_feedback_6_novelas.databaseSQL.SQLiteHelper;
import com.example.jaime_lopez_feedback_6_novelas.model.Novel;
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

        Novel novel = new Novel(title, author, year, synopsis);

        if (novelId != null) {
            novel.setId(novelId);
            db.collection("novelas").document(novelId).set(novel)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddNovelActivity.this, "Novela actualizada", Toast.LENGTH_SHORT).show();
                        sqliteHelper.updateNovel(novel);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddNovelActivity.this, "Error al actualizar la novela", Toast.LENGTH_SHORT).show();
                    });
        } else {
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

    /**
     * Método para monitorear el estado de la batería.
     */
    private void monitorBatteryState() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Obtener el nivel y la escala de la batería
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = (level / (float) scale) * 100;

                // Determinar si la batería está baja (por debajo del 20%)
                isLowBattery = batteryPct < 20;

                // Ajustar el brillo de la pantalla según el estado de la batería
                adjustScreenBrightness();
            }
        };
        registerReceiver(batteryReceiver, filter);
    }

    /**
     * Método para ajustar el brillo de la pantalla según el estado de la batería.
     */
    private void adjustScreenBrightness() {
        // Obtener los parámetros de la ventana
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

        if (isLowBattery) {
            // Verificar si el brillo ya está al 70%
            if (layoutParams.screenBrightness != 0.7f) {
                // Ajustar el brillo de la pantalla al 70%
                layoutParams.screenBrightness = 0.7f; // Valor entre 0.0f y 1.0f (70% de brillo)
                getWindow().setAttributes(layoutParams);
                Toast.makeText(this, "Batería baja... se ha reducido el brillo.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Restaurar el brillo de la pantalla al valor predeterminado
            if (layoutParams.screenBrightness != WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
                getWindow().setAttributes(layoutParams);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Ajustar el brillo al reanudar la actividad
        adjustScreenBrightness();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }
}
