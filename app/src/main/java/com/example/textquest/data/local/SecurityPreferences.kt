package com.example.textquest.data.local

import android.content.Context

class SecurityPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    fun isBiometricEnabled(): Boolean = prefs.getBoolean("bio_enabled", false)
    fun setBiometricEnabled(enabled: Boolean) = prefs.edit().putBoolean("bio_enabled", enabled).apply()
}