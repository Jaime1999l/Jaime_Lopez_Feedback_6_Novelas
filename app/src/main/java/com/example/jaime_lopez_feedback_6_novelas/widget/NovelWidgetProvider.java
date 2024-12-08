// NovelWidgetProvider.java
package com.example.jaime_lopez_feedback_6_novelas.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.jaime_lopez_feedback_6_novelas.PantallaPrincipalActivity;
import com.example.jaime_lopez_feedback_6_novelas.R;
import com.example.jaime_lopez_feedback_6_novelas.databaseSQL.SQLiteHelper;
import com.example.jaime_lopez_feedback_6_novelas.model.Novel;

import java.util.List;

public class NovelWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SQLiteHelper db = new SQLiteHelper(context);
        List<Novel> favoriteNovels = db.getFavoriteNovels();

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_novel);

            // Mostrar los títulos de las primeras 3 novelas favoritas
            StringBuilder titles = new StringBuilder();
            for (int i = 0; i < Math.min(favoriteNovels.size(), 3); i++) {
                titles.append(favoriteNovels.get(i).getTitle()).append("\n");
            }
            views.setTextViewText(R.id.widget_title, "Favoritos:\n" + titles.toString());

            // Intent para abrir la aplicación al tocar el widget
            Intent intent = new Intent(context, PantallaPrincipalActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
