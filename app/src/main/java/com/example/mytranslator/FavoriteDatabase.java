package com.example.mytranslator;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Favorite.class}, version = 2)
public abstract class FavoriteDatabase extends RoomDatabase {
    private static volatile FavoriteDatabase INSTANCE;

    public abstract FavoriteDao favoriteDao();

    public static FavoriteDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FavoriteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FavoriteDatabase.class, "favorite_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
