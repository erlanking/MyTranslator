package com.example.mytranslator;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert
    void insert(Favorite favorite);

    @Query("SELECT * FROM favorites")
    List<Favorite> getAllFavorites();

    @Delete
    void delete(Favorite favorite);

    @Query("SELECT * FROM favorites WHERE source_text = :sourceText AND translated_text = :translatedText LIMIT 1")
    Favorite getFavoriteByText(String sourceText, String translatedText);

    @Query("DELETE FROM favorites WHERE source_text = :sourceText")
    void deleteFavoriteBySourceText(String sourceText);
}
