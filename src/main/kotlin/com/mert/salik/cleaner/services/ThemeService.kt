package com.mert.salik.cleaner.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@Service
@State(
    name = "AndroidStudioCleanerThemeService",
    storages = [Storage("androidStudioCleanerTheme.xml")]
)
class ThemeService : PersistentStateComponent<ThemeService> {
    private var currentTheme: String = "light"

    companion object {
        fun getInstance(): ThemeService {
            return ApplicationManager.getApplication().getService(ThemeService::class.java)
        }
    }

    fun getCurrentTheme(): String = currentTheme

    fun setTheme(theme: String) {
        currentTheme = theme
    }

    fun isDarkTheme(): Boolean = currentTheme == "dark"

    fun getThemeColors(): ThemeColors {
        return if (isDarkTheme()) {
            ThemeColors(
                background = "#2B2B2B",
                foreground = "#FFFFFF",
                primary = "#4CAF50",
                secondary = "#2196F3",
                accent = "#FF9800",
                surface = "#3C3F41",
                border = "#555555"
            )
        } else {
            ThemeColors(
                background = "#FFFFFF",
                foreground = "#000000",
                primary = "#4CAF50",
                secondary = "#2196F3",
                accent = "#FF9800",
                surface = "#F5F5F5",
                border = "#E0E0E0"
            )
        }
    }

    fun getBackgroundColor(): java.awt.Color {
        return java.awt.Color.decode(getThemeColors().background)
    }

    fun getPrimaryTextColor(): java.awt.Color {
        return java.awt.Color.decode(getThemeColors().foreground)
    }

    fun getSecondaryTextColor(): java.awt.Color {
        return java.awt.Color.decode(getThemeColors().secondary)
    }

    override fun getState(): ThemeService = this

    override fun loadState(state: ThemeService) {
        XmlSerializerUtil.copyBean(state, this)
    }
}

data class ThemeColors(
    val background: String,
    val foreground: String,
    val primary: String,
    val secondary: String,
    val accent: String,
    val surface: String,
    val border: String
) 