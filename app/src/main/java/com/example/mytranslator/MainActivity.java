package com.example.mytranslator;

import static com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.EN;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.room.Room;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceEdt;
    private ImageView micIV, photoIV, folderIV;
    private TextView translatedTV;
    private Handler handler = new Handler();
    private Runnable translateRunnable;
    private ImageView copyButton;
    private ImageView bookmarkIV;
    private FavoriteDatabase db;
    private FavoriteDao favoriteDao;


    private boolean isBookmarked = false; // Флаг для отслеживания состояния
    private List<Pair<String, String>> favoritesList = new ArrayList<>(); // Список избранного

    String[] fromLanguages = {
            "From", "English", "Afrikanns", "Arabic","Russian","Turkish", "Belarusian","Bulgarian", "Bengali", "Catalan","Czech","Welsh","Danish","German","Greek","Esperanto","Spanish","Estonian","Persian","Finnish","French","Irish","Galician","Gujarati","Hebrew", "Hindi","Croatian","Haitian","Hungarian","Indonesian","Icelandic","Italian","Japanese","Georgian","Kannada","Korean","Lithuanian","Latvian","Macedonian","Marathi","Malay","Maltese","Dutch","Norwegian","Polish","Portuguese","Romanian","Slovak","Slovenian","Albanian","Swedish","Swahili","Tamil","Telugu","Thai","Tagalog","Ukranian", "Urdu","Vietnamese"
    };

    String[] toLanguages = {
            "To", "English", "Afrikanns", "Arabic","Russian","Turkish", "Belarusian","Bulgarian", "Bengali", "Catalan","Czech","Welsh","Danish","German","Greek","Esperanto","Spanish","Estonian","Persian","Finnish","French","Irish","Galician","Gujarati","Hebrew", "Hindi","Croatian","Haitian","Hungarian","Indonesian","Icelandic","Italian","Japanese","Georgian","Kannada","Korean","Lithuanian","Latvian","Macedonian","Marathi","Malay","Maltese","Dutch","Norwegian","Polish","Portuguese","Romanian","Slovak","Slovenian","Albanian","Swedish","Swahili","Tamil","Telugu","Thai","Tagalog","Ukranian", "Urdu","Vietnamese"
    };
    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final int IMAGE_PICK_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    int languageCode, fromLanguageCode, toLanguageCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        db = FavoriteDatabase.getInstance(this);
        favoriteDao = db.favoriteDao();
        TextInputEditText editText = findViewById(R.id.idEdtSource);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});




        bookmarkIV = findViewById(R.id.idFavorites); // Замените idBookmark на ваш ID элемент
        bookmarkIV.setImageResource(isBookmarked ? R.drawable.bookmark_24 : R.drawable.bookmark_border);

        // Инициализация базы данных
        db = FavoriteDatabase.getInstance(this);
        if (db == null) {
            Log.e("DatabaseError", "FavoriteDatabase is null");
        } else {
            Log.d("DatabaseStatus", "FavoriteDatabase initialized");
        }

        // Инициализация DAO
        favoriteDao = db.favoriteDao();
        if (favoriteDao == null) {
            Log.e("DatabaseError", "FavoriteDao is null");
        } else {
            Log.d("DatabaseStatus", "FavoriteDao initialized");
        }



// При добавлении в избранное:
        // При добавлении в избранное:
        // Логика для изменения состояния избранного
        bookmarkIV.setOnClickListener(v -> {
            String sourceText = sourceEdt.getText().toString();
            String translatedText = translatedTV.getText().toString();

            if (!sourceText.isEmpty() && !translatedText.isEmpty()) {
                // Проверяем, есть ли уже этот перевод в избранном
                new Thread(() -> {
                    Favorite favorite = db.favoriteDao().getFavoriteByText(sourceText, translatedText);
                    if (favorite != null) {
                        // Если запись уже есть в базе, удаляем её
                        db.favoriteDao().delete(favorite);
                        runOnUiThread(() -> {
                            bookmarkIV.setImageResource(R.drawable.bookmark_border); // Меняем иконку на bookmark_border
                            isBookmarked = false;
                            Toast.makeText(MainActivity.this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // Если записи нет, добавляем её в базу данных
                        Favorite newFavorite = new Favorite(sourceText, translatedText);
                        db.favoriteDao().insert(newFavorite);
                        runOnUiThread(() -> {
                            bookmarkIV.setImageResource(R.drawable.bookmark_24); // Меняем иконку на bookmark_24
                            isBookmarked = true;
                            Toast.makeText(MainActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                Toast.makeText(MainActivity.this, "Please translate text first", Toast.LENGTH_SHORT).show();
            }
        });









           bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.nav_favorites) {
                        Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                        startActivity(intent);
                        return true;
                    }
                    if (item.getItemId() == R.id.nav_conversation) {
                        Intent intent = new Intent(MainActivity.this, ConversationsActivity.class);
                        startActivity(intent);
                        return true;
                    }
                    return false;
                }

        });


        // Проверка и запрос разрешения на камеру
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Если разрешение не предоставлено, запрашиваем его
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        }

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard();
            }
        });


        // Set up spinners
        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguages[position]);
                SharedPreferences sharedPreferences = getSharedPreferences("LanguagePreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

// Сохраняем выбранный язык
                editor.putInt("fromLanguage", fromLanguageCode);
                editor.putInt("toLanguage", toLanguageCode);
                editor.apply();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        sourceEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Удаляем предыдущий отложенный вызов
                if (translateRunnable != null) {
                    handler.removeCallbacks(translateRunnable);
                }

                // Создаём новый отложенный вызов
                translateRunnable = () -> {
                    if (!s.toString().isEmpty() && fromLanguageCode != 0 && toLanguageCode != 0) {
                        translateText(fromLanguageCode, toLanguageCode, s.toString());
                    }
                };
                handler.postDelayed(translateRunnable, 500); // Задержка в 500 мс
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        // Set up translate button click listener



        // Set up microphone click listener
        micIV.setOnClickListener(v -> {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text");
            try {
                startActivityForResult(i, REQUEST_PERMISSION_CODE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Установка обработчика на иконку фото (для фотографирования)
        photoIV.setOnClickListener(v -> {
            // Запускаем камеру для фотографирования
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        });


        // Set up folder click listener
        folderIV.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PERMISSION_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                sourceEdt.setText(result.get(0)); // Устанавливаем распознанный текст в поле ввода
                if (fromLanguageCode != 0 && toLanguageCode != 0) {
                    translateText(fromLanguageCode, toLanguageCode, result.get(0)); // Переводим текст
                }
            } else {
                Toast.makeText(this, "No speech recognized", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            try {
                // Преобразуем Bitmap в InputImage для распознавания текста
                InputImage image = InputImage.fromBitmap(photo, 0);
                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                recognizer.process(image)
                        .addOnSuccessListener(result -> {
                            // Извлекаем текст из результата распознавания
                            StringBuilder extractedText = new StringBuilder();
                            for (Text.TextBlock block : result.getTextBlocks()) {
                                extractedText.append(block.getText()).append("\n");
                            }

                            // Устанавливаем распознанный текст в sourceEdt (TextInputEditText)
                            sourceEdt.setText(extractedText.toString());

                            // Запускаем перевод, если тексты заполнены
                            if (!extractedText.toString().isEmpty() && fromLanguageCode != 0 && toLanguageCode != 0) {
                                // Запускаем метод перевода
                                translateText(fromLanguageCode, toLanguageCode, extractedText.toString());
                            } else {
                                Toast.makeText(MainActivity.this, "Please select source and target languages", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error recognizing text: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Этот метод будет вызван, если пользователь ответил на запрос разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение на камеру предоставлено
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                // Разрешение на камеру отклонено
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // В методе для перевода текста:
    private void translateText(int fromLanguageCode, int toLanguageCode, String source) {
        if (fromLanguageCode == 0) {
            Toast.makeText(MainActivity.this, "Please select the source language", Toast.LENGTH_SHORT).show();
            return;
        }
        if (toLanguageCode == 0) {
            Toast.makeText(MainActivity.this, "Please select the target language", Toast.LENGTH_SHORT).show();
            return;
        }

        translatedTV.setText("Downloading Model...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(unused -> {
            translatedTV.setText("Translating...");
            translator.translate(source)
                    .addOnSuccessListener(translatedText -> {
                        translatedTV.setText(translatedText);
                        // После перевода обновляем иконку закладки
                        updateBookmarkIcon(source, translatedText);
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Translation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Model download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }




    private int getLanguageCode(String language) {
        switch (language) {
            case "English":
                return FirebaseTranslateLanguage.EN;
            case "Afrikaans":
                return FirebaseTranslateLanguage.AF;
            case "Arabic":
                return FirebaseTranslateLanguage.AR;
            case "Russian":
                return FirebaseTranslateLanguage.RU;
            case "Turkish":
                return FirebaseTranslateLanguage.TR;
            case "Belarusian":
                return FirebaseTranslateLanguage.BE;
            case "Bulgarian":
                return FirebaseTranslateLanguage.BG;
            case "Bengali":
                return FirebaseTranslateLanguage.BN;
            case "Catalan":
                return FirebaseTranslateLanguage.CA;
            case "Czech":
                return FirebaseTranslateLanguage.CS;
            case "Welsh":
                return FirebaseTranslateLanguage.CY;
            case "Danish":
                return FirebaseTranslateLanguage.DA;
            case "German":
                return FirebaseTranslateLanguage.DE;
            case "Greek":
                return FirebaseTranslateLanguage.EL;
            case "Esperanto":
                return FirebaseTranslateLanguage.EO;
            case "Spanish":
                return FirebaseTranslateLanguage.ES;
            case "Estonian":
                return FirebaseTranslateLanguage.ET;
            case "Persian":
                return FirebaseTranslateLanguage.FA;
            case "Finnish":
                return FirebaseTranslateLanguage.FI;
            case "French":
                return FirebaseTranslateLanguage.FR;
            case "Irish":
                return FirebaseTranslateLanguage.GA;
            case "Galician":
                return FirebaseTranslateLanguage.GL;
            case "Gujarati":
                return FirebaseTranslateLanguage.GU;
            case "Hebrew":
                return FirebaseTranslateLanguage.HE;
            case "Hindi":
                return FirebaseTranslateLanguage.HI;
            case "Croatian":
                return FirebaseTranslateLanguage.HR;
            case "Haitian":
                return FirebaseTranslateLanguage.HT;
            case "Hungarian":
                return FirebaseTranslateLanguage.HU;
            case "Indonesian":
                return FirebaseTranslateLanguage.ID;
            case "Icelandic":
                return FirebaseTranslateLanguage.IS;
            case "Italian":
                return FirebaseTranslateLanguage.IT;
            case "Japanese":
                return FirebaseTranslateLanguage.JA;
            case "Georgian":
                return FirebaseTranslateLanguage.KA;
            case "Kannada":
                return FirebaseTranslateLanguage.KN;
            case "Korean":
                return FirebaseTranslateLanguage.KO;
            case "Lithuanian":
                return FirebaseTranslateLanguage.LT;
            case "Latvian":
                return FirebaseTranslateLanguage.LV;
            case "Macedonian":
                return FirebaseTranslateLanguage.MK;
            case "Marathi":
                return FirebaseTranslateLanguage.MR;
            case "Malay":
                return FirebaseTranslateLanguage.MS;
            case "Maltese":
                return FirebaseTranslateLanguage.MT;
            case "Dutch":
                return FirebaseTranslateLanguage.NL;
            case "Norwegian":
                return FirebaseTranslateLanguage.NO;
            case "Polish":
                return FirebaseTranslateLanguage.PL;
            case "Portuguese":
                return FirebaseTranslateLanguage.PT;
            case "Romanian":
                return FirebaseTranslateLanguage.RO;
            case "Slovak":
                return FirebaseTranslateLanguage.SK;
            case "Slovenian":
                return FirebaseTranslateLanguage.SL;
            case "Albanian":
                return FirebaseTranslateLanguage.SQ;
            case "Swedish":
                return FirebaseTranslateLanguage.SV;
            case "Swahili":
                return FirebaseTranslateLanguage.SW;
            case "Tamil":
                return FirebaseTranslateLanguage.TA;
            case "Telugu":
                return FirebaseTranslateLanguage.TE;
            case "Thai":
                return FirebaseTranslateLanguage.TH;
            case "Tagalog":
                return FirebaseTranslateLanguage.TL;
            case "Ukrainian":
                return FirebaseTranslateLanguage.UK;
            case "Urdu":
                return FirebaseTranslateLanguage.UR;
            case "Vietnamese":
                return FirebaseTranslateLanguage.VI;

            default:
                return 0;
        }
    }

    private void recognizeTextFromImage(Uri imageUri) {
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image).addOnSuccessListener(result -> {
                StringBuilder extractedText = new StringBuilder();
                for (Text.TextBlock block : result.getTextBlocks()) {
                    extractedText.append(block.getText()).append("\n");
                }

                // Устанавливаем извлеченный текст в поле ввода
                sourceEdt.setText(extractedText.toString());

                // Проверяем, выбраны ли исходный и целевой языки, и запускаем перевод
                if (!extractedText.toString().isEmpty() && fromLanguageCode != 0 && toLanguageCode != 0) {
                    translateText(fromLanguageCode, toLanguageCode, extractedText.toString());
                } else {
                    Toast.makeText(MainActivity.this, "Please select source and target languages", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error recognizing text: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void swapLanguages() {
        // Получаем выбранные языки
        String fromLanguage = fromSpinner.getSelectedItem().toString();
        String toLanguage = toSpinner.getSelectedItem().toString();

        // Обмениваем выбранные языки
        fromSpinner.setSelection(getLanguagePosition(toLanguage));
        toSpinner.setSelection(getLanguagePosition(fromLanguage));

        // Обмениваем коды языков
        int temp = fromLanguageCode;
        fromLanguageCode = toLanguageCode;
        toLanguageCode = temp;
    }

    private int getLanguagePosition(String language) {
        for (int i = 0; i < fromLanguages.length; i++) {
            if (fromLanguages[i].equals(language)) {
                return i;
            }
        }
        return 0; // По умолчанию выбираем "From", если нет совпадений
    }
    private void copyToClipboard() {
        // Получаем текст из TextView
        String translatedText = translatedTV.getText().toString();

        // Получаем ClipboardManager
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Создаем ClipData для копирования
        android.content.ClipData clip = android.content.ClipData.newPlainText("Translated Text", translatedText);

        // Помещаем ClipData в буфер обмена
        clipboard.setPrimaryClip(clip);

        // Показываем уведомление
        Toast.makeText(MainActivity.this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    private void updateBookmarkIcon(String sourceText, String translatedText) {
        // Проверяем, существует ли уже такая пара в избранном
        new Thread(() -> {
            FavoriteDatabase db = FavoriteDatabase.getInstance(MainActivity.this);
            Favorite existingFavorite = db.favoriteDao().getFavoriteByText(sourceText, translatedText);

            runOnUiThread(() -> {
                if (existingFavorite != null) {
                    // Если пара уже в избранном, показываем иконку bookmark_24
                    bookmarkIV.setImageResource(R.drawable.bookmark_24);
                    isBookmarked = true;
                } else {
                    // Если пары нет, показываем иконку bookmark_border
                    bookmarkIV.setImageResource(R.drawable.bookmark_border);
                    isBookmarked = false;
                }
            });
        }).start();
    }


}

