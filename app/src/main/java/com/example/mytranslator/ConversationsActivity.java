
package com.example.mytranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mytranslator.databinding.ActivityConversationBinding;
;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class ConversationsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    ActivityConversationBinding binding;

    String[] fromLanguages = {"From" , "English", "Afrikanns", "Arabic","Russian","Turkish", "Belarusian","Bulgarian", "Bengali", "Catalan","Czech","Welsh","Danish","German","Greek","Esperanto","Spanish","Estonian","Persian","Finnish","French","Irish","Galician","Gujarati","Hebrew", "Hindi","Croatian","Haitian","Hungarian","Indonesian","Icelandic","Italian","Japanese","Georgian","Kannada","Korean","Lithuanian","Latvian","Macedonian","Marathi","Malay","Maltese","Dutch","Norwegian","Polish","Portuguese","Romanian","Slovak","Slovenian","Albanian","Swedish","Swahili","Tamil","Telugu","Thai","Tagalog","Ukranian", "Urdu","Vietnamese"};
    String[] toLanguages = {"To" , "English", "Afrikanns", "Arabic", "Russian","Turkish","Belarusian","Bulgarian", "Bengali", "Catalan","Czech","Welsh","Danish","German","Greek","Esperanto","Spanish","Estonian","Persian","Finnish","French","Irish","Galician","Gujarati","Hebrew", "Hindi","Croatian","Haitian","Hungarian","Indonesian","Icelandic","Italian","Japanese","Georgian","Kannada","Korean","Lithuanian","Latvian","Macedonian","Marathi","Malay","Maltese","Dutch","Norwegian","Polish","Portuguese","Romanian","Slovak","Slovenian","Albanian","Swedish","Swahili","Tamil","Telugu","Thai","Tagalog","Ukranian", "Urdu","Vietnamese"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int langauageCode, fromLanguageCode, toLanguageCode = 0;

    private TextToSpeech textToSpeech;
    private static final String FROM_LANGUAGE_KEY = "from_language";
    private static final String TO_LANGUAGE_KEY = "to_language";

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        textToSpeech = new TextToSpeech(this, this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        fromLanguageCode = sharedPreferences.getInt(FROM_LANGUAGE_KEY, 0);
        toLanguageCode = sharedPreferences.getInt(TO_LANGUAGE_KEY, 0);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);


        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(ConversationsActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
            if (item.getItemId() == R.id.nav_favorites) {
                Intent intent = new Intent(ConversationsActivity.this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });

        binding.SwapConversationLangauges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapLanguages();
            }
        });
        binding.SwapLanguages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapLanguages();
            }
        });



        binding.FromSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguages[i]);
                saveSelectedLanguages();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.ToSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                toLanguageCode = getLanguageCode(toLanguages[i]);
                saveSelectedLanguages();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fromLanguages);
        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, toLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.FromSpinner1.setAdapter(fromAdapter);
        binding.ToSpinner1.setAdapter(toAdapter);

        binding.FromSpinner1.setSelection(getLanguageIndex(fromLanguageCode, fromLanguages));
        binding.ToSpinner1.setSelection(getLanguageIndex(toLanguageCode, toLanguages));

        binding.FromSpeakMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                int selectedLanguageCode = getLanguageCode(binding.FromSpinner1.getSelectedItem().toString());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLanguageLocale(selectedLanguageCode));

                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak To Convert In Text");
                try {
                    startActivityForResult(intent, REQUEST_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ConversationsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });




    }
    private int getLanguageIndex(int languageCode, String[] languages) {
        for (int i = 0; i < languages.length; i++) {
            if (getLanguageCode(languages[i]) == languageCode) {
                return i;
            }
        }
        return 0;
    }
    private void swapLanguages() {
        int tempLanguageCode = fromLanguageCode;
        fromLanguageCode = toLanguageCode;
        toLanguageCode = tempLanguageCode;

        sharedPreferences.edit().putInt(FROM_LANGUAGE_KEY, fromLanguageCode).apply();
        sharedPreferences.edit().putInt(TO_LANGUAGE_KEY, toLanguageCode).apply();

        binding.FromSpinner1.setSelection(getLanguageIndex(fromLanguageCode, fromLanguages));
        binding.ToSpinner1.setSelection(getLanguageIndex(toLanguageCode, toLanguages));



    }

    //Mic Call
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PERMISSION_CODE){
            if(resultCode == RESULT_OK && data!=null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                binding.fromText.setText(result.get(0));
                binding.toText.setText("");
                if(binding.fromText.getText().toString().isEmpty()){
                    Toast.makeText(ConversationsActivity.this, "Please Enter Your Text To Translate...", Toast.LENGTH_SHORT).show();
                } else if (fromLanguageCode == 0) {
                    Toast.makeText(ConversationsActivity.this, "Please Select Your Source Language...", Toast.LENGTH_SHORT).show();

                } else if (toLanguageCode==0) {
                    Toast.makeText(ConversationsActivity.this, "Please Select Language To Translation...", Toast.LENGTH_SHORT).show();
                }
                else{
                    translateText(fromLanguageCode,toLanguageCode,binding.fromText.getText().toString());
                }
            }
        }

    }

    private String getLanguageLocale(int languageCode) {
        Locale locale;
        switch (languageCode) {
            case FirebaseTranslateLanguage.EN:
                locale = Locale.ENGLISH;
                break;
            case FirebaseTranslateLanguage.AF:
                locale = new Locale("af");
                break;
            case FirebaseTranslateLanguage.AR:
                locale = new Locale("ar");
                break;
            case FirebaseTranslateLanguage.BE:
                locale = new Locale("be");
                break;
            case FirebaseTranslateLanguage.BG:
                locale = new Locale("bg");
                break;
            case FirebaseTranslateLanguage.BN:
                locale = new Locale("bn");
                break;
            case FirebaseTranslateLanguage.CA:
                locale = new Locale("ca");
                break;
            case FirebaseTranslateLanguage.CS:
                locale = new Locale("cs");
                break;
            case FirebaseTranslateLanguage.CY:
                locale = new Locale("cy");
                break;
            case FirebaseTranslateLanguage.DA:
                locale = new Locale("da");
                break;
            case FirebaseTranslateLanguage.DE:
                locale = new Locale("de");
                break;
            case FirebaseTranslateLanguage.EL:
                locale = new Locale("el");
                break;
            case FirebaseTranslateLanguage.EO:
                locale = new Locale("eo");
                break;
            case FirebaseTranslateLanguage.ES:
                locale = new Locale("es");
                break;
            case FirebaseTranslateLanguage.ET:
                locale = new Locale("et");
                break;
            case FirebaseTranslateLanguage.FA:
                locale = new Locale("fa");
                break;
            case FirebaseTranslateLanguage.FI:
                locale = new Locale("fi");
                break;
            case FirebaseTranslateLanguage.FR:
                locale = new Locale("fr");
                break;
            case FirebaseTranslateLanguage.GA:
                locale = new Locale("ga");
                break;
            case FirebaseTranslateLanguage.GL:
                locale = new Locale("gl");
                break;
            case FirebaseTranslateLanguage.GU:
                locale = new Locale("gu");
                break;
            case FirebaseTranslateLanguage.HE:
                locale = new Locale("he");
                break;
            case FirebaseTranslateLanguage.HI:
                locale = new Locale("hi");
                break;
            case FirebaseTranslateLanguage.HR:
                locale = new Locale("hr");
                break;
            case FirebaseTranslateLanguage.HT:
                locale = new Locale("ht");
                break;
            case FirebaseTranslateLanguage.HU:
                locale = new Locale("hu");
                break;
            case FirebaseTranslateLanguage.ID:
                locale = new Locale("id");
                break;
            case FirebaseTranslateLanguage.IS:
                locale = new Locale("is");
                break;
            case FirebaseTranslateLanguage.IT:
                locale = new Locale("it");
                break;
            case FirebaseTranslateLanguage.JA:
                locale = new Locale("ja");
                break;
            case FirebaseTranslateLanguage.KA:
                locale = new Locale("ka");
                break;
            case FirebaseTranslateLanguage.KN:
                locale = new Locale("kn");
                break;
            case FirebaseTranslateLanguage.KO:
                locale = new Locale("ko");
                break;
            case FirebaseTranslateLanguage.LT:
                locale = new Locale("lt");
                break;
            case FirebaseTranslateLanguage.LV:
                locale = new Locale("lv");
                break;
            case FirebaseTranslateLanguage.MK:
                locale = new Locale("mk");
                break;
            case FirebaseTranslateLanguage.MR:
                locale = new Locale("mr");
                break;
            case FirebaseTranslateLanguage.MS:
                locale = new Locale("ms");
                break;
            case FirebaseTranslateLanguage.MT:
                locale = new Locale("mt");
                break;
            case FirebaseTranslateLanguage.NL:
                locale = new Locale("nl");
                break;
            case FirebaseTranslateLanguage.NO:
                locale = new Locale("no");
                break;
            case FirebaseTranslateLanguage.PL:
                locale = new Locale("pl");
                break;
            case FirebaseTranslateLanguage.PT:
                locale = new Locale("pt");
                break;
            case FirebaseTranslateLanguage.RO:
                locale = new Locale("ro");
                break;
            case FirebaseTranslateLanguage.RU:
                locale = new Locale("ru");
                break;
            case FirebaseTranslateLanguage.SK:
                locale = new Locale("sk");
                break;
            case FirebaseTranslateLanguage.SL:
                locale = new Locale("sl");
                break;
            case FirebaseTranslateLanguage.SQ:
                locale = new Locale("sq");
                break;
            case FirebaseTranslateLanguage.SV:
                locale = new Locale("sv");
                break;
            case FirebaseTranslateLanguage.SW:
                locale = new Locale("sw");
                break;
            case FirebaseTranslateLanguage.TA:
                locale = new Locale("ta");
                break;
            case FirebaseTranslateLanguage.TE:
                locale = new Locale("te");
                break;
            case FirebaseTranslateLanguage.TH:
                locale = new Locale("th");
                break;
            case FirebaseTranslateLanguage.TL:
                locale = new Locale("tl");
                break;
            case FirebaseTranslateLanguage.TR:
                locale = new Locale("tr");
                break;
            case FirebaseTranslateLanguage.UK:
                locale = new Locale("uk");
                break;
            case FirebaseTranslateLanguage.UR:
                locale = new Locale("ur");
                break;
            case FirebaseTranslateLanguage.VI:
                locale = new Locale("vi");
                break;

            default:
                locale = Locale.getDefault();
                break;
        }
        return locale.toString();
    }



    private void translateText(int fromLanguageCode, int toLanguageCode, String source){
        binding.toText.setText("Downloading Model...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(fromLanguageCode).setTargetLanguage(toLanguageCode).build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                binding.toText.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        binding.toText.setText(s);

                        String textToSpeak = binding.toText.getText().toString();
                        if (!TextUtils.isEmpty(textToSpeak)) {
                            textToSpeech.setSpeechRate(0.7f);

                            if (isLanguageSupported(textToSpeak)) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                                }
                            } else {
                                textToSpeech.setLanguage(Locale.ENGLISH);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                                }
                            }
                        } else {
                            Toast.makeText(ConversationsActivity.this, "No text to speak", Toast.LENGTH_SHORT).show();
                        }

                    }
                    private boolean isLanguageSupported(String textToSpeak) {
                        // Check if the desired language is supported
                        int result = textToSpeech.isLanguageAvailable(getLocaleFromText(textToSpeak));
                        return result == TextToSpeech.LANG_AVAILABLE || result == TextToSpeech.LANG_COUNTRY_AVAILABLE;
                    }

                    private Locale getLocaleFromText(String textToSpeak) {
                        String[] parts = textToSpeak.split("-");
                        if (parts.length > 1) {
                            return new Locale(parts[0], parts[1]);
                        } else {
                            return new Locale(parts[0]);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ConversationsActivity.this, "Fail To Translate : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ConversationsActivity.this, "Fail To Download Language Model..."+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported");
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    private void saveSelectedLanguages() {
        sharedPreferences.edit().putInt(FROM_LANGUAGE_KEY, fromLanguageCode).apply();
        sharedPreferences.edit().putInt(TO_LANGUAGE_KEY, toLanguageCode).apply();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            playContent();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void playContent() {
        Intent intent = new Intent(ConversationsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onPause() {
        super.onPause();
        saveSelectedLanguage();
    }
    private void saveSelectedLanguage() {
        Intent resultIntent = new Intent(ConversationsActivity.this,MainActivity.class);
        resultIntent.putExtra("fromLanguageCode", fromLanguageCode);
        resultIntent.putExtra("toLanguageCode", toLanguageCode);
        setResult(Activity.RESULT_OK, resultIntent);
    }

    @Override
    protected void onDestroy() {
        saveSelectedLanguages();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
    public int getLanguageCode(String language) {
        int languageCode = 0;
        switch (language) {
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Afrikanns":
                languageCode = FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                languageCode = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                languageCode = FirebaseTranslateLanguage.BG;
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;
            case "Catalan":
                languageCode = FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languageCode = FirebaseTranslateLanguage.CS;
                break;
            case "Welsh":
                languageCode = FirebaseTranslateLanguage.CY;
                break;
            case "Danish":
                languageCode = FirebaseTranslateLanguage.DA;
                break;
            case "German":
                languageCode = FirebaseTranslateLanguage.DE;
                break;
            case "Greek":
                languageCode = FirebaseTranslateLanguage.EL;
                break;
            case "Esperanto":
                languageCode = FirebaseTranslateLanguage.EO;
                break;
            case "Spanish":
                languageCode = FirebaseTranslateLanguage.ES;
                break;
            case "Estonian":
                languageCode = FirebaseTranslateLanguage.ET;
                break;
            case "Persian":
                languageCode = FirebaseTranslateLanguage.FA;
                break;
            case "Finnish":
                languageCode = FirebaseTranslateLanguage.FI;
                break;
            case "French":
                languageCode = FirebaseTranslateLanguage.FR;
                break;
            case "Irish":
                languageCode = FirebaseTranslateLanguage.GA;
                break;
            case "Galician":
                languageCode = FirebaseTranslateLanguage.GL;
                break;
            case "Gujarati":
                languageCode = FirebaseTranslateLanguage.GU;
                break;
            case "Hebrew":
                languageCode = FirebaseTranslateLanguage.HE;
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Croatian":
                languageCode = FirebaseTranslateLanguage.HR;
                break;
            case "Haitian":
                languageCode = FirebaseTranslateLanguage.HT;
                break;
            case "Hungarian":
                languageCode = FirebaseTranslateLanguage.HU;
                break;
            case "Indonesian":
                languageCode = FirebaseTranslateLanguage.ID;
                break;
            case "Icelandic":
                languageCode = FirebaseTranslateLanguage.IS;
                break;
            case "Italian":
                languageCode = FirebaseTranslateLanguage.IT;
                break;
            case "Japanese":
                languageCode = FirebaseTranslateLanguage.JA;
                break;
            case "Georgian":
                languageCode = FirebaseTranslateLanguage.KA;
                break;
            case "Kannada":
                languageCode = FirebaseTranslateLanguage.KN;
                break;
            case "Korean":
                languageCode = FirebaseTranslateLanguage.KO;
                break;
            case "Lithuanian":
                languageCode = FirebaseTranslateLanguage.LT;
                break;
            case "Latvian":
                languageCode = FirebaseTranslateLanguage.LV;
                break;
            case "Macedonian":
                languageCode = FirebaseTranslateLanguage.MK;
                break;
            case "Marathi":
                languageCode = FirebaseTranslateLanguage.MR;
                break;
            case "Malay":
                languageCode = FirebaseTranslateLanguage.MS;
                break;
            case "Maltese":
                languageCode = FirebaseTranslateLanguage.MT;
                break;
            case "Dutch":
                languageCode = FirebaseTranslateLanguage.NL;
                break;
            case "Norwegian":
                languageCode = FirebaseTranslateLanguage.NO;
                break;
            case "Polish":
                languageCode = FirebaseTranslateLanguage.PL;
                break;
            case "Portuguese":
                languageCode = FirebaseTranslateLanguage.PT;
                break;
            case "Romanian":
                languageCode = FirebaseTranslateLanguage.RO;
                break;
            case "Russian":
                languageCode = FirebaseTranslateLanguage.RU;
                break;
            case "Slovak":
                languageCode = FirebaseTranslateLanguage.SK;
                break;
            case "Slovenian":
                languageCode = FirebaseTranslateLanguage.SL;
                break;
            case "Albanian":
                languageCode = FirebaseTranslateLanguage.SQ;
                break;
            case "Swedish":
                languageCode = FirebaseTranslateLanguage.SV;
                break;
            case "Swahili":
                languageCode = FirebaseTranslateLanguage.SW;
                break;
            case "Tamil":
                languageCode = FirebaseTranslateLanguage.TA;
                break;
            case "Telugu":
                languageCode = FirebaseTranslateLanguage.TE;
                break;
            case "Thai":
                languageCode = FirebaseTranslateLanguage.TH;
                break;
            case "Tagalog":
                languageCode = FirebaseTranslateLanguage.TL;
                break;
            case "Turkish":
                languageCode = FirebaseTranslateLanguage.TR;
                break;
            case "Ukranian":
                languageCode = FirebaseTranslateLanguage.UK;
                break;
            case "Urdu":
                languageCode = FirebaseTranslateLanguage.UR;
                break;
            case "Vietnamese":
                languageCode = FirebaseTranslateLanguage.VI;
                break;


            default:
                languageCode = 0;


        }
        return languageCode;
    }

}