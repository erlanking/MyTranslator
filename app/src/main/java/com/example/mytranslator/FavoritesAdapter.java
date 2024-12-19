package com.example.mytranslator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {
    private List<String> favoriteList;
    private Context context;
    private FavoriteDatabase db;

    public FavoritesAdapter(List<String> favoriteList, Context context) {
        this.favoriteList = favoriteList;
        this.context = context;
        this.db = FavoriteDatabase.getInstance(context); // Инициализация базы данных
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflating item_favorite.xml layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavoriteViewHolder holder, int position) {
        // Setting the text for source and translated text views
        String favoriteItem = favoriteList.get(position);
        String[] parts = favoriteItem.split(" -> ");  // Split source and translated text

        if (parts.length == 2) {
            holder.sourceTextView.setText(parts[0]);
            holder.translatedTextView.setText(parts[1]);
        }

        // Обработка долгого нажатия
        holder.itemView.setOnLongClickListener(v -> {
            String sourceText = parts[0];

            // Показать диалог подтверждения удаления
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Удалить элемент?")
                    .setMessage("Вы уверены, что хотите удалить этот элемент?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        // Удаление из базы данных
                        new Thread(() -> {
                            db.favoriteDao().deleteFavoriteBySourceText(sourceText); // Удаляем из базы
                            ((FavoritesActivity) context).runOnUiThread(() -> {
                                favoriteList.remove(position);  // Удаляем из списка
                                notifyItemRemoved(position);    // Обновляем адаптер
                                Toast.makeText(context, "Элемент удален", Toast.LENGTH_SHORT).show();
                            });
                        }).start();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true; // Возвращаем true для обработки долгого нажатия
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    // ViewHolder to bind views
    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView sourceTextView;
        TextView translatedTextView;

        public FavoriteViewHolder(View itemView) {
            super(itemView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            translatedTextView = itemView.findViewById(R.id.translatedTextView);
        }

    }
}
