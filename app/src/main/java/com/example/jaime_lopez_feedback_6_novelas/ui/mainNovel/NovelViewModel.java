package com.example.jaime_lopez_feedback_6_novelas.ui.mainNovel;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NovelViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Novel>> novelListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final FirebaseFirestore db;
    private ListenerRegistration registration;

    private DocumentSnapshot lastVisible; // Último documento visible para paginación
    private boolean isLastPage = false; // Verifica si se cargó la última página
    private ScheduledExecutorService scheduler; // Programador para sincronización periódica

    public NovelViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        loadNovels(); // Cargar los datos iniciales
        monitorBatteryState(application.getApplicationContext()); // Monitorear el estado de la batería
        schedulePeriodicUpdates(); // Programar actualizaciones periódicas
    }

    // Método para obtener todas las novelas
    public LiveData<List<Novel>> getAllNovels() {
        return novelListLiveData;
    }

    // Método para verificar si los datos están cargando
    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    // Método para cargar novelas con una escucha en tiempo real y optimización
    private void loadNovels() {
        isLoadingLiveData.setValue(true);
        registration = db.collection("novelas")
                .orderBy("title") // Ordena por título
                .limit(20) // Solo obtén las primeras 20 novelas
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        isLoadingLiveData.setValue(false);
                        return;
                    }

                    if (snapshots != null) {
                        List<Novel> novelList = new ArrayList<>();
                        for (DocumentSnapshot document : snapshots) {
                            Novel novel = document.toObject(Novel.class);
                            if (novel != null) {
                                novel.setId(document.getId());
                                novelList.add(novel);
                            }
                        }

                        // Almacena el último documento visible para paginación
                        if (!snapshots.isEmpty()) {
                            lastVisible = snapshots.getDocuments().get(snapshots.size() - 1);
                            isLastPage = snapshots.size() < 20; // Si se cargaron menos de 20, es la última página
                        }

                        novelListLiveData.setValue(novelList);
                        isLoadingLiveData.setValue(false);
                    }
                });
    }

    // Método para cargar novelas una sola vez
    private void loadNovelsOnce() {
        isLoadingLiveData.setValue(true);
        db.collection("novelas")
                .orderBy("title")
                .limit(20)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null) {
                        List<Novel> novelList = new ArrayList<>();
                        for (DocumentSnapshot document : snapshots) {
                            Novel novel = document.toObject(Novel.class);
                            if (novel != null) {
                                novel.setId(document.getId());
                                novelList.add(novel);
                            }
                        }
                        novelListLiveData.setValue(novelList);
                    }
                    isLoadingLiveData.setValue(false);
                })
                .addOnFailureListener(e -> isLoadingLiveData.setValue(false));
    }


    // Método para obtener una novela por su ID
    public LiveData<Novel> getNovelById(String novelId) {
        MutableLiveData<Novel> novelLiveData = new MutableLiveData<>();
        db.collection("novelas").document(novelId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Novel novel = task.getResult().toObject(Novel.class);
                if (novel != null) {
                    novel.setId(task.getResult().getId());
                    novelLiveData.setValue(novel);
                }
            }
        });
        return novelLiveData;
    }

    public void updateFavoriteStatus(Novel novel) {
        db.collection("novelas").document(novel.getId())
                .update(
                        "favorite", novel.isFavorite()
                )
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }


    // Método para programar sincronización periódica
    private void schedulePeriodicUpdates() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> loadNovelsOnce(), 0, 15, TimeUnit.MINUTES); // Cada 15 minutos
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
                    // Reduce la frecuencia de sincronización si la batería está baja
                    if (scheduler != null && !scheduler.isShutdown()) {
                        scheduler.shutdownNow();
                    }
                    scheduler = Executors.newSingleThreadScheduledExecutor();
                    scheduler.scheduleAtFixedRate(() -> loadNovelsOnce(), 0, 30, TimeUnit.MINUTES); // Cada 30 minutos
                } else {
                    // Restaura la frecuencia de sincronización normal
                    schedulePeriodicUpdates();
                }
            }
        };
        context.registerReceiver(batteryReceiver, filter);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (registration != null) {
            registration.remove();
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
