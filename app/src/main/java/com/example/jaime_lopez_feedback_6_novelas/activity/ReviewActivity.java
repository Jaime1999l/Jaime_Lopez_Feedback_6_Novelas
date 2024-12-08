package com.example.jaime_lopez_feedback_6_novelas.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.jaime_lopez_feedback_6_novelas.R;
import com.example.jaime_lopez_feedback_6_novelas.databaseSQL.SQLiteHelper;
import com.example.jaime_lopez_feedback_6_novelas.model.Novel;
import com.example.jaime_lopez_feedback_6_novelas.model.Review;
import com.example.jaime_lopez_feedback_6_novelas.ui.review.ReviewViewModel;

import java.util.List;

public class ReviewActivity extends AppCompatActivity {

    private ReviewViewModel reviewViewModel;
    private LinearLayout reviewsLayout;
    private SQLiteHelper sqliteHelper;

    private boolean isLowBattery = false;
    private BroadcastReceiver batteryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        reviewsLayout = findViewById(R.id.reviews_layout);
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        sqliteHelper = new SQLiteHelper(this);

        // Obtener todas las reseñas desde Firebase y SQLite
        loadAllReviewsFromSQLite();
        loadAllReviewsFromFirebase();

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
        // Ajustar el brillo de la pantalla al reanudar la actividad
        adjustScreenBrightness();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }

    // Cargar reseñas desde SQLite
    private void loadAllReviewsFromSQLite() {
        List<Review> reviewsFromSQLite = sqliteHelper.getAllReviews();
        displayReviews(reviewsFromSQLite);
    }

    // Cargar reseñas desde Firebase y almacenarlas también en SQLite
    private void loadAllReviewsFromFirebase() {
        reviewViewModel.getAllReviews().observe(this, reviews -> {
            for (Review review : reviews) {
                sqliteHelper.addReview(review);
            }
            displayReviews(reviews);
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayReviews(List<Review> reviews) {
        reviewsLayout.removeAllViews();
        for (Review review : reviews) {
            TextView reviewView = new TextView(this);
            reviewView.setText("Usuario: " + review.getReviewer() + "\n" +
                    "Comentario: " + review.getComment() + "\n" +
                    "Puntuación: " + review.getRating() + "\n" +
                    "Nombre de la novela: " + review.getNovelName());
            reviewView.setPadding(16, 16, 16, 16);

            // Botón para ver más detalles de la novela
            Button viewNovelButton = new Button(this);
            viewNovelButton.setText("Ver Novela");
            viewNovelButton.setOnClickListener(v -> loadNovelDetails(review.getNovelId()));

            reviewsLayout.addView(reviewView);
            reviewsLayout.addView(viewNovelButton);
        }

        if (reviews.isEmpty()) {
            TextView noReviewsView = new TextView(this);
            noReviewsView.setText("No hay reseñas disponibles.");
            noReviewsView.setPadding(16, 16, 16, 16);
            reviewsLayout.addView(noReviewsView);
        }
    }

    private void loadNovelDetails(String novelId) {
        reviewViewModel.getNovelById(novelId).observe(this, new Observer<Novel>() {
            @Override
            public void onChanged(Novel novel) {
                if (novel != null) {
                    showNovelDetails(novel);
                }
            }
        });
    }

    private void showNovelDetails(Novel novel) {
        Toast.makeText(this, "Título: " + novel.getTitle() + "\nAutor: " + novel.getAuthor(), Toast.LENGTH_LONG).show();
    }
}
