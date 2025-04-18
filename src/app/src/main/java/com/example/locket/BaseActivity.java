package com.example.locket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LocketPrefs";
    private static final String KEY_LANGUAGE = "language";
    private static final String ACTION_LANGUAGE_CHANGED = "com.example.locket.LANGUAGE_CHANGED";

    private final BroadcastReceiver languageChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BaseActivity", "Received language change broadcast, recreating activity: " + getClass().getSimpleName());
            recreate(); // Làm mới activity khi nhận broadcast
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyLanguage();
        super.onCreate(savedInstanceState);
        // Đăng ký receiver để lắng nghe thay đổi ngôn ngữ
        registerReceiver(languageChangeReceiver, new IntentFilter(ACTION_LANGUAGE_CHANGED), Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy đăng ký receiver để tránh leak
        unregisterReceiver(languageChangeReceiver);
    }

    private void applyLanguage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString(KEY_LANGUAGE, "");
        Log.d("BaseActivity", "Applying language: " + language + " for activity: " + getClass().getSimpleName());
        if (!language.isEmpty()) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }

    public void setLanguage(String languageCode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LANGUAGE, languageCode);
        editor.apply();
        Log.d("BaseActivity", "Set language to: " + languageCode);
        // Gửi broadcast để thông báo thay đổi ngôn ngữ
        Intent intent = new Intent(ACTION_LANGUAGE_CHANGED);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
        recreate();
    }
}