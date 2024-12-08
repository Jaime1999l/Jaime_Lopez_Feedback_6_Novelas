package com.example.jaime_lopez_feedback_6_novelas.ui.fragments;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.jaime_lopez_feedback_6_novelas.PantallaPrincipalActivity;
import com.example.jaime_lopez_feedback_6_novelas.R;
import com.example.jaime_lopez_feedback_6_novelas.activity.AddReviewActivity;
import com.example.jaime_lopez_feedback_6_novelas.model.Novel;
import com.example.jaime_lopez_feedback_6_novelas.ui.mainNovel.NovelViewModel;
import com.example.jaime_lopez_feedback_6_novelas.widget.NovelWidgetProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class NovelDetailFragment extends Fragment {

    private TextView titleTextView, authorTextView, synopsisTextView;
    private Button favoriteButton, reviewButton;
    private NovelViewModel novelViewModel;
    private FirebaseFirestore firebaseFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_novel_detail, container, false);

        // Inicialización de las vistas
        titleTextView = view.findViewById(R.id.text_view_title);
        authorTextView = view.findViewById(R.id.text_view_author);
        synopsisTextView = view.findViewById(R.id.text_view_synopsis);
        favoriteButton = view.findViewById(R.id.favorite_button);
        reviewButton = view.findViewById(R.id.review_button);

        firebaseFirestore = FirebaseFirestore.getInstance();
        novelViewModel = new ViewModelProvider(this).get(NovelViewModel.class);

        // Obtener el ID de la novela desde los argumentos
        String novelId = getArguments() != null ? getArguments().getString("novelId") : null;
        if (novelId == null) {
            // Manejo en caso de que el ID de la novela no esté presente
            titleTextView.setText("No se encontró la novela");
            return view;
        }

        // Observar cambios en la novela específica
        novelViewModel.getNovelById(novelId).observe(getViewLifecycleOwner(), novel -> {
            if (novel == null) {
                // Mostrar un mensaje si no se encuentra la novela
                titleTextView.setText("No se encontró la novela");
                return;
            }

            displayNovelDetails(novel);

            // Configuración del botón de favoritos
            favoriteButton.setText(novel.isFavorite() ? "Eliminar de Favoritos" : "Añadir a Favoritos");
            favoriteButton.setOnClickListener(v -> {
                novel.setFavorite(!novel.isFavorite());
                updateFavoriteStatusInFirebase(novel);
                favoriteButton.setText(novel.isFavorite() ? "Eliminar de Favoritos" : "Añadir a Favoritos");
            });

            // Configuración del botón de reseña
            reviewButton.setOnClickListener(v -> {
                if (getActivity() == null) return;
                Intent intent = new Intent(getActivity(), AddReviewActivity.class);
                intent.putExtra("EXTRA_NOVEL_ID", novel.getId());
                intent.putExtra("EXTRA_NOVEL_NAME", novel.getTitle());
                startActivity(intent);
            });
        });

        return view;
    }

    /**
     * Muestra los detalles de la novela en las vistas correspondientes.
     */
    @SuppressLint("SetTextI18n")
    private void displayNovelDetails(Novel novel) {
        titleTextView.setText(novel.getTitle());
        authorTextView.setText("Autor: " + novel.getAuthor());
        synopsisTextView.setText(novel.getSynopsis());
    }

    /**
     * Actualiza el estado de favorito de la novela en Firebase.
     */
    private void updateFavoriteStatusInFirebase(Novel novel) {
        firebaseFirestore.collection("novelas")
                .document(novel.getId())
                .update("favorite", novel.isFavorite())
                .addOnSuccessListener(aVoid -> {
                    updateWidget(); // Actualizar el widget después de la actualización
                    // Actualizar la lista de favoritos si estamos en PantallaPrincipalActivity
                    if (getActivity() instanceof PantallaPrincipalActivity) {
                        ((PantallaPrincipalActivity) getActivity()).refreshFavoritesList();
                    }
                })
                .addOnFailureListener(e -> {
                    // Manejo de errores al actualizar Firebase
                });
    }

    /**
     * Envia un broadcast para actualizar el widget.
     */
    private void updateWidget() {
        if (getContext() == null) return;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
        Intent intent = new Intent(getContext(), NovelWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        // Obtener los IDs de los widgets actuales
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(getContext(), NovelWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

        // Enviar el broadcast
        getContext().sendBroadcast(intent);
    }
}
