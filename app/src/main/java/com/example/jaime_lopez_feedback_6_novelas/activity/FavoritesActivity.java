package com.example.jaime_lopez_feedback_6_novelas.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.jaime_lopez_feedback_6_novelas.R;
import com.example.jaime_lopez_feedback_6_novelas.model.Novel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FavoritesActivity extends AppCompatActivity {

    private LinearLayout favoritesLayout;
    private FirebaseFirestore firebaseFirestore;
    private ScheduledExecutorService scheduler;
    private BroadcastReceiver batteryReceiver;
    private boolean isLowBattery = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_activity);

        favoritesLayout = findViewById(R.id.favorites_layout);
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Monitorear el estado de la batería para ajustar la frecuencia de actualizaciones y el brillo
        monitorBatteryState();

        // Cargar las novelas favoritas periódicamente
        schedulePeriodicFavoriteUpdates();

        // Realizar la carga inicial
        loadFavoriteNovelsFromFirebase();
    }

    private void loadFavoriteNovelsFromFirebase() {
        firebaseFirestore.collection("novelas")
                .whereEqualTo("favorite", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Novel> favoriteNovels = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Novel novel = document.toObject(Novel.class);
                        if (novel != null) {
                            novel.setId(document.getId());
                            favoriteNovels.add(novel);
                        }
                    }
                    displayFavoriteNovels(favoriteNovels);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar las novelas favoritas.", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayFavoriteNovels(List<Novel> novels) {
        favoritesLayout.removeAllViews();

        if (novels == null || novels.isEmpty()) {
            Toast.makeText(this, "No tienes novelas favoritas.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Novel novel : novels) {
            // Crear un CardView para cada novela
            CardView cardView = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(16, 16, 16, 16); // Margen entre los CardViews
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(12);
            cardView.setCardElevation(8);
            cardView.setUseCompatPadding(true);

            // Crear un LinearLayout para contener título y autor
            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(24, 24, 24, 24);

            // Configurar el título
            TextView titleText = new TextView(this);
            titleText.setText(novel.getTitle());
            titleText.setTextSize(18);
            titleText.setGravity(Gravity.START);
            titleText.setTextColor(getResources().getColor(android.R.color.black));

            // Configurar el autor
            TextView authorText = new TextView(this);
            authorText.setText("Autor: " + novel.getAuthor());
            authorText.setTextSize(14);
            authorText.setGravity(Gravity.START);
            authorText.setTextColor(getResources().getColor(android.R.color.darker_gray));

            // Agregar el contenido al CardView
            cardContent.addView(titleText);
            cardContent.addView(authorText);
            cardView.addView(cardContent);

            // Agregar el CardView al LinearLayout principal
            favoritesLayout.addView(cardView);
        }
    }

    private void schedulePeriodicFavoriteUpdates() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        int updateInterval = isLowBattery ? 30 : 15;
        scheduler.scheduleAtFixedRate(this::loadFavoriteNovelsFromFirebase, 0, updateInterval, TimeUnit.MINUTES);
    }

    private void monitorBatteryState() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;

                boolean wasLowBattery = isLowBattery;
                isLowBattery = batteryPct < 20;

                if (wasLowBattery != isLowBattery) {
                    adjustScreenBrightness();

                    if (scheduler != null && !scheduler.isShutdown()) {
                        scheduler.shutdownNow();
                        schedulePeriodicFavoriteUpdates();
                    }
                }
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
    protected void onDestroy() {
        super.onDestroy();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }
}
