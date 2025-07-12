package com.mert.salik.cleaner.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.ServiceManager
import java.util.*

@Service
class LanguageService {
    private var currentLanguage: String = "en"
    private var currentLocale: Locale = Locale.ENGLISH
    private var resourceBundle: ResourceBundle = ResourceBundle.getBundle("messages.strings", currentLocale)
    private val settingsService = SettingsService.getInstance()

    companion object {
        fun getInstance(): LanguageService {
            return ApplicationManager.getApplication().getService(LanguageService::class.java)
        }
    }

    init {
        // Load saved language preference
        loadLanguagePreference()
    }

    fun getCurrentLanguage(): String = currentLanguage

    fun getCurrentLocale(): Locale = currentLocale

    fun setLanguage(language: String) {
        if (currentLanguage != language) {
        currentLanguage = language
            currentLocale = when (language) {
                "tr" -> Locale("tr", "TR")
                else -> Locale.ENGLISH
            }
            
            // Reload resource bundle
            try {
                ResourceBundle.clearCache()
                resourceBundle = ResourceBundle.getBundle("messages.strings", currentLocale)
                
                // Save preference
                settingsService.setLanguage(language)
                
                // Notify listeners about language change
                notifyLanguageChanged()
            } catch (e: Exception) {
                // Fallback to default language
                currentLanguage = "en"
                currentLocale = Locale.ENGLISH
                resourceBundle = ResourceBundle.getBundle("messages.strings", currentLocale)
            }
        }
    }

    private fun loadLanguagePreference() {
        try {
            val savedLanguage = settingsService.getLanguage()
            if (savedLanguage.isNotEmpty()) {
                setLanguage(savedLanguage)
            }
        } catch (e: Exception) {
            // Use default language if loading fails
            currentLanguage = "en"
            currentLocale = Locale.ENGLISH
        }
    }

    fun getString(key: String): String {
        return try {
            resourceBundle.getString(key)
        } catch (e: Exception) {
            // Fallback to key if translation not found
            key
        }
    }

    fun getString(key: String, vararg args: Any): String {
        return try {
            String.format(resourceBundle.getString(key), *args)
        } catch (e: Exception) {
            // Fallback to key if translation not found
            key
        }
    }

    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            "en" to "English",
            "tr" to "Türkçe"
        )
    }

    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "tr" -> "Türkçe"
            else -> languageCode
        }
    }

    fun isCurrentLanguage(languageCode: String): Boolean {
        return currentLanguage == languageCode
    }

    fun getFormattedTime(seconds: Long): String {
        return when {
            seconds < 60 -> getString("time.seconds", seconds)
            else -> getString("time.minutes", seconds / 60)
        }
    }

    fun getFormattedSize(bytes: Long): String {
        return when {
            bytes < 1024 -> getString("size.bytes", bytes)
            bytes < 1024 * 1024 -> getString("size.kilobytes", bytes / 1024)
            bytes < 1024 * 1024 * 1024 -> getString("size.megabytes", bytes / (1024 * 1024))
            else -> getString("size.gigabytes", bytes / (1024 * 1024 * 1024))
        }
    }

    private fun notifyLanguageChanged() {
        // This can be used to notify UI components about language changes
        // For now, we'll just log it
        val logService = LogService.getInstance()
        logService.addLog(LogLevel.INFO, "Language changed to: $currentLanguage", "LanguageService")
    }

    fun reloadResources() {
        try {
            ResourceBundle.clearCache()
            resourceBundle = ResourceBundle.getBundle("messages.strings", currentLocale)
        } catch (e: Exception) {
            // Fallback to default
            resourceBundle = ResourceBundle.getBundle("messages.strings", Locale.ENGLISH)
        }
    }
} 