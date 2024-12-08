package com.example.jaime_lopez_feedback_6_novelas.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.jaime_lopez_feedback_6_novelas.R;
import com.example.jaime_lopez_feedback_6_novelas.databaseSQL.SQLiteHelper;
import com.example.jaime_lopez_feedback_6_novelas.model.Review;
import com.example.jaime_lopez_feedback_6_novelas.ui.review.ReviewViewModel;


public class AddReviewActivity extends AppCompatActivity {

    private EditText editTextReviewer, editTextComment, editTextRating;
    private Button buttonAddReview;
    private ReviewViewModel reviewViewModel;
    private SQLiteHelper sqliteHelper;
    private String novelId;
    private String novelName;

    private boolean isLowBattery = false;
    private BroadcastReceiver batteryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        novelId = getIntent().getStringExtra("EXTRA_NOVEL_ID");
        novelName = getIntent().getStringExtra("EXTRA_NOVEL_NAME");

        editTextReviewer = findViewById(R.id.edit_text_reviewer);
        editTextComment = findViewById(R.id.edit_text_comment);
        editTextRating = findViewById(R.id.edit_text_rating);
        buttonAddReview = findViewById(R.id.button_add_review);

        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        sqliteHelper = new SQLiteHelper(this);

        buttonAddReview.setOnClickListener(v -> addReview());

        // Monitorear el estado de la batería
        monitorBatteryState();
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

    private void addReview() {
        String reviewer = editTextReviewer.getText().toString().trim();
        String comment = editTextComment.getText().toString().trim();
        String ratingStr = editTextRating.getText().toString().trim();

        if (reviewer.isEmpty() || comment.isEmpty() || ratingStr.isEmpty()) {
            Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingrese un número válido para la calificación", Toast.LENGTH_SHORT).show();
            return;
        }

        Review review = new Review(novelId, reviewer, comment, rating, novelName);

        // Guardar reseña en Firebase
        reviewViewModel.addReview(review);

        // Guardar reseña en SQLite
        sqliteHelper.addReview(review);

        Toast.makeText(this, "Reseña añadida", Toast.LENGTH_SHORT).show();
        finish();
    }
}
