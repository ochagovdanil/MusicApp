package com.example.musicapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesApp {

    private SharedPreferences sharedPreferences;

    public PreferencesApp(Context context) {
        sharedPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
    }

    // repeat = true
    public boolean getRepeatedMode() {
        return sharedPreferences.getBoolean("repeat", false);
    }

    public void setRepeatedMode(boolean val) {
        sharedPreferences.edit().putBoolean("repeat", val).apply();
    }

}
