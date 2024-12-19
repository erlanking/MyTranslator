package com.example.mytranslator;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class Favorite {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "source_text")
    private String sourceText;

    @ColumnInfo(name = "translated_text")
    private String translatedText;

    // Конструкторы, геттеры и сеттеры
    public Favorite(String sourceText, String translatedText) {
        this.sourceText = sourceText;
        this.translatedText = translatedText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
