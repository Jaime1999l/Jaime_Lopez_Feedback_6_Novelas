package com.example.jaime_lopez_feedback_6_novelas.databaseSQL;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.example.jaime_lopez_feedback_6_novelas.model.Novel;
import com.example.jaime_lopez_feedback_6_novelas.model.Review;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "novelasDB";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_NOVELS = "novelas";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "titulo";
    public static final String COLUMN_AUTHOR = "autor";
    public static final String COLUMN_YEAR = "año";
    public static final String COLUMN_SYNOPSIS = "sinopsis";
    public static final String COLUMN_IMAGE_URI = "imagen";
    public static final String COLUMN_FAVORITE = "favorite";

    public static final String TABLE_REVIEWS = "reseñas";
    public static final String COLUMN_REVIEW_ID = "id";
    public static final String COLUMN_REVIEW_NOVEL_ID = "novelaId";
    public static final String COLUMN_REVIEW_AUTHOR = "revisor";
    public static final String COLUMN_REVIEW_COMMENT = "comentario";
    public static final String COLUMN_REVIEW_RATING = "calificacion";
    public static final String COLUMN_REVIEW_NOVEL_NAME = "novelaNombre";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOVELS_TABLE = "CREATE TABLE " + TABLE_NOVELS + "("
                + COLUMN_ID + " TEXT PRIMARY KEY,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_AUTHOR + " TEXT,"
                + COLUMN_YEAR + " INTEGER,"
                + COLUMN_SYNOPSIS + " TEXT,"
                + COLUMN_IMAGE_URI + " TEXT,"
                + COLUMN_FAVORITE + " INTEGER" + ")";
        db.execSQL(CREATE_NOVELS_TABLE);

        String CREATE_REVIEWS_TABLE = "CREATE TABLE " + TABLE_REVIEWS + "("
                + COLUMN_REVIEW_ID + " TEXT PRIMARY KEY,"
                + COLUMN_REVIEW_NOVEL_ID + " TEXT,"
                + COLUMN_REVIEW_AUTHOR + " TEXT,"
                + COLUMN_REVIEW_COMMENT + " TEXT,"
                + COLUMN_REVIEW_RATING + " INTEGER,"
                + COLUMN_REVIEW_NOVEL_NAME + " TEXT" + ")";
        db.execSQL(CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOVELS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        onCreate(db);
    }

    public void addNovel(Novel novel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, novel.getId());
        values.put(COLUMN_TITLE, novel.getTitle());
        values.put(COLUMN_AUTHOR, novel.getAuthor());
        values.put(COLUMN_YEAR, novel.getYear());
        values.put(COLUMN_SYNOPSIS, novel.getSynopsis());
        values.put(COLUMN_FAVORITE, novel.isFavorite() ? 1 : 0);

        db.insertWithOnConflict(TABLE_NOVELS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Método para obtener todas las novelas
    public List<Novel> getAllNovels() {
        List<Novel> novelList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOVELS, null);

        if (cursor.moveToFirst()) {
            do {
                Novel novel = new Novel();
                novel.setId(cursor.getString(0));
                novel.setTitle(cursor.getString(1));
                novel.setAuthor(cursor.getString(2));
                novel.setYear(cursor.getInt(3));
                novel.setSynopsis(cursor.getString(4));
                novel.setFavorite(cursor.getInt(6) == 1);
                novelList.add(novel);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return novelList;
    }

    @SuppressLint("Range")
    public List<Novel> getFavoriteNovels() {
        List<Novel> favoriteNovels = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOVELS + " WHERE " + COLUMN_FAVORITE + "=1", null);

        if (cursor.moveToFirst()) {
            do {
                Novel novel = new Novel();
                novel.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                novel.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
                novel.setAuthor(cursor.getString(cursor.getColumnIndex(COLUMN_AUTHOR)));
                novel.setYear(cursor.getInt(cursor.getColumnIndex(COLUMN_YEAR)));
                novel.setSynopsis(cursor.getString(cursor.getColumnIndex(COLUMN_SYNOPSIS)));
                novel.setFavorite(cursor.getInt(cursor.getColumnIndex(COLUMN_FAVORITE)) == 1);
                favoriteNovels.add(novel);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return favoriteNovels;
    }


    // Método para actualizar una novela
    public void updateNovel(Novel novel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, novel.getTitle());
        values.put(COLUMN_AUTHOR, novel.getAuthor());
        values.put(COLUMN_YEAR, novel.getYear());
        values.put(COLUMN_SYNOPSIS, novel.getSynopsis());
        values.put(COLUMN_FAVORITE, novel.isFavorite() ? 1 : 0);

        db.update(TABLE_NOVELS, values, COLUMN_ID + "=?", new String[]{novel.getId()});
        db.close();
    }

    public void updateFavoriteStatus(String novelId, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVORITE, isFavorite ? 1 : 0); // 1 para true, 0 para false

        db.update(TABLE_NOVELS, values, COLUMN_ID + " = ?", new String[]{novelId});
        db.close();
    }



    // Método para agregar una reseña
    public void addReview(Review review) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REVIEW_ID, review.getId());
        values.put(COLUMN_REVIEW_NOVEL_ID, review.getNovelId());
        values.put(COLUMN_REVIEW_AUTHOR, review.getReviewer());
        values.put(COLUMN_REVIEW_COMMENT, review.getComment());
        values.put(COLUMN_REVIEW_RATING, review.getRating());
        values.put(COLUMN_REVIEW_NOVEL_NAME, review.getNovelName());

        db.insertWithOnConflict(TABLE_REVIEWS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Método para obtener todas las reseñas
    public List<Review> getAllReviews() {
        List<Review> reviewList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_REVIEWS, null);

        if (cursor.moveToFirst()) {
            do {
                Review review = new Review();
                review.setId(cursor.getString(0));
                review.setNovelId(cursor.getString(1));
                review.setReviewer(cursor.getString(2));
                review.setComment(cursor.getString(3));
                review.setRating(cursor.getInt(4));
                review.setNovelName(cursor.getString(5));
                reviewList.add(review);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return reviewList;
    }
}


