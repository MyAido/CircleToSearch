package com.rr.aido.ui.circletosearch.utils

import android.content.Context
import android.content.SharedPreferences

class UIPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_DESKTOP_MODE = "is_desktop_mode"
        private const val KEY_DARK_MODE = "is_dark_mode"
        private const val KEY_SHOW_GRADIENT_BORDER = "show_gradient_border"
        private const val KEY_SHOW_FRIENDLY_MESSAGES = "show_friendly_messages"
        private const val KEY_SEARCH_ENGINE_ORDER = "search_engine_order"
        private const val KEY_USE_GOOGLE_LENS_ONLY = "use_google_lens_only"
    }

    fun isUseGoogleLensOnly(): Boolean {
        return prefs.getBoolean(KEY_USE_GOOGLE_LENS_ONLY, false)
    }

    fun setUseGoogleLensOnly(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_GOOGLE_LENS_ONLY, isEnabled).apply()
    }
    
    fun isDesktopMode(): Boolean {
        return prefs.getBoolean(KEY_DESKTOP_MODE, false)
    }
    
    fun setDesktopMode(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_DESKTOP_MODE, isEnabled).apply()
    }
    
    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }
    
    fun setDarkMode(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isEnabled).apply()
    }
    
    fun isShowGradientBorder(): Boolean {
        return prefs.getBoolean(KEY_SHOW_GRADIENT_BORDER, false)
    }
    
    fun setShowGradientBorder(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_GRADIENT_BORDER, isEnabled).apply()
    }

    fun isShowFriendlyMessages(): Boolean {
        return prefs.getBoolean(KEY_SHOW_FRIENDLY_MESSAGES, true)
    }

    fun setShowFriendlyMessages(isEnabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_FRIENDLY_MESSAGES, isEnabled).apply()
    }

    fun getSearchEngineOrder(): String? {
        return prefs.getString(KEY_SEARCH_ENGINE_ORDER, null)
    }

    fun setSearchEngineOrder(order: String) {
        prefs.edit().putString(KEY_SEARCH_ENGINE_ORDER, order).apply()
    }
}
