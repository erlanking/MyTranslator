package com.example.mytranslator;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private FavoriteDatabase db;
    private RecyclerView favoritesListView;
    private FavoritesAdapter adapter;
    private List<String> favoriteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Инициализация базы данных и RecyclerView
        db = FavoriteDatabase.getInstance(this);
        favoritesListView = findViewById(R.id.recyclerView);
        favoritesListView.setLayoutManager(new LinearLayoutManager(this));

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
            if (item.getItemId() == R.id.nav_conversation) {
                Intent intent = new Intent(FavoritesActivity.this, ConversationsActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });

        // Получаем все избранные записи
        new Thread(() -> {
            List<Favorite> favorites = db.favoriteDao().getAllFavorites();
            runOnUiThread(() -> {
                // Заполняем список избранных
                for (Favorite favorite : favorites) {
                    favoriteList.add(favorite.getSourceText() + " -> " + favorite.getTranslatedText());
                }
                // Устанавливаем адаптер
                adapter = new FavoritesAdapter(favoriteList, FavoritesActivity.this);
                favoritesListView.setAdapter(adapter);
            });
        }).start();
    }
}
