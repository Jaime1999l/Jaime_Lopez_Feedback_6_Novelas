package com.example.jaime_lopez_feedback_6_novelas.ui.mainNovel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jaime_lopez_feedback_6_novelas.R;
import com.example.jaime_lopez_feedback_6_novelas.model.Novel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NovelAdapter extends RecyclerView.Adapter<NovelAdapter.NovelHolder> {

    private List<Novel> novelList = new ArrayList<>();
    private final OnNovelClickListener onNovelClickListener;
    private final Context context;

    public NovelAdapter(OnNovelClickListener onNovelClickListener, Context context) {
        this.onNovelClickListener = onNovelClickListener;
        this.context = context;
    }

    public void setNovels(List<Novel> novels) {
        this.novelList = novels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NovelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el diseño del item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.novel_item, parent, false);
        return new NovelHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NovelHolder holder, int position) {
        Novel currentNovel = novelList.get(position);
        holder.textViewTitle.setText(currentNovel.getTitle());
        holder.textViewAuthor.setText(currentNovel.getAuthor());

        // Configurar clic en el ítem para abrir el fragmento de detalles
        holder.itemView.setOnClickListener(v -> onNovelClickListener.onNovelClick(currentNovel));
    }


    @Override
    public int getItemCount() {
        return novelList.size();
    }

    public Context getContext() {
        return context;
    }

    public interface OnNovelClickListener {
        void onNovelClick(Novel novel);
    }

    public static class NovelHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewAuthor;


        public NovelHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewAuthor = itemView.findViewById(R.id.text_view_author);
        }
    }
}
