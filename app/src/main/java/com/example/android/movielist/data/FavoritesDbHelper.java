package com.example.android.movielist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.movielist.data.FavoritesContract.FavoriteEntry;

/**
 * Created by rsteller on 11/21/2016.
 */

public class FavoritesDbHelper extends SQLiteOpenHelper {
    //curent version
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "favorite.db";

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoriteEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL, " +
                FavoriteEntry.COLUMN_MOVIE_TITLE + " TEXT, " +
                FavoriteEntry.COLUMN_MOVIE_POSTER + " TEXT, " +
                FavoriteEntry.COLUMN_MOVIE_RELEASED + " TEXT, " +
                FavoriteEntry.COLUMN_MOVIE_RATING + " TEXT, " +
                FavoriteEntry.COLUMN_MOVIE_PLOT + " TEXT);";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
