package com.example.patientrecords.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    var themeMode: Int
        get() = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) {
            prefs.edit().putInt("theme_mode", value).apply()
        }
}
