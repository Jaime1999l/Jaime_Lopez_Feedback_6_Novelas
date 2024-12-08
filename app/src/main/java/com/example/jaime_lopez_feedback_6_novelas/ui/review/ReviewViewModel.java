package com.example.jaime_lopez_feedback_6_novelas.ui.review;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.jaime_lopez_feedback_6_novelas.model.Novel;
import com.example.jaime_lopez_feedback_6_novelas.model.Review;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReviewViewModel extends AndroidViewModel {

    private final FirebaseFirestore db;
    private final MutableLiveData<List<Review>> reviewsLiveData;
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private ListenerRegistration reviewsListener;
    private ScheduledExecutorService scheduler;

    public ReviewViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        reviewsLiveData = new MutableLiveData<>();
        monitorBatteryState(application.getApplicationContext());
        schedulePeriodicReviewUpdates();
    }

    // Obtener todas las reseñas con carga optimizada
    public LiveData<List<Review>> getAllReviews() {
        return reviewsLiveData;
    }

    // Cargar reseñas en tiempo real
    public void loadReviewsInRealTime() {
        isLoadingLiveData.setValue(true);
        reviewsListener = db.collection("reviews")
                .orderBy("timestamp") // Ordena las reseñas por tiempo
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        isLoadingLiveData.setValue(false);
                        return;
                    }
                    if (snapshots != null) {
                        List<Review> reviewList = new ArrayList<>();
                        for (DocumentSnapshot document : snapshots) {
                            Review review = document.toObject(Review.class);
                            if (review != null) {
                                review.setId(document.getId());
                                reviewList.add(review);
                            }
                        }
                        reviewsLiveData.setValue(reviewList);
                        isLoadingLiveData.setValue(false);
                    }
                });
    }

    // Obtener una novela por su ID
    public LiveData<Novel> getNovelById(String novelId) {
        MutableLiveData<Novel> liveData = new MutableLiveData<>();
        db.collection("novelas")
                .document(novelId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Novel novel = task.getResult().toObject(Novel.class);
                        if (novel != null) {
                            liveData.setValue(novel);
                        }
                    }
                });
        return liveData;
    }

    // Agregar una reseña
    public void addReview(Review review) {
        db.collection("reviews").add(review)
                .addOnSuccessListener(documentReference -> {
                    review.setId(documentReference.getId());
                    db.collection("reviews").document(review.getId()).set(review);
                })
                .addOnFailureListener(e -> {
                });
    }

    // Carga periódica de reseñas
    private void schedulePeriodicReviewUpdates() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::loadReviewsOnce, 0, 15, TimeUnit.MINUTES); // Cada 15 minutos
    }

    // Cargar reseñas una sola vez
    private void loadReviewsOnce() {
        isLoadingLiveData.setValue(true);
        db.collection("reviews")
                .orderBy("timestamp")
                .limit(50) // Limitar la cantidad de reseñas cargadas
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviewList = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Review review = document.toObject(Review.class);
                        if (review != null) {
                            review.setId(document.getId());
                            reviewList.add(review);
                        }
                    }
                    reviewsLiveData.setValue(reviewList);
                    isLoadingLiveData.setValue(false);
                })
                .addOnFailureListener(e -> isLoadingLiveData.setValue(false));
    }

    // Monitorear el estado de la batería
    private void monitorBatteryState(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;

                if (batteryPct < 20) {
                    // Reducir la frecuencia de las actualizaciones si la batería está baja
                    if (scheduler != null && !scheduler.isShutdown()) {
                        scheduler.shutdownNow();
                    }
                    scheduler = Executors.newSingleThreadScheduledExecutor();
                    scheduler.scheduleAtFixedRate(ReviewViewModel.this::loadReviewsOnce, 0, 30, TimeUnit.MINUTES);
                } else {
                    // Restaurar frecuencia normal
                    schedulePeriodicReviewUpdates();
                }
            }
        };
        context.registerReceiver(batteryReceiver, filter);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (reviewsListener != null) {
            reviewsListener.remove();
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
