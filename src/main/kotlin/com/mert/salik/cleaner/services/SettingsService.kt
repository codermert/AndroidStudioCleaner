package com.mert.salik.cleaner.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@Service
@State(
    name = "AndroidStudioCleanerSettingsService",
    storages = [Storage("androidStudioCleanerSettings.xml")]
)
class SettingsService : PersistentStateComponent<SettingsService> {
    // Language and Theme
    private var language: String = "en"
    private var theme: String = "light"
    
    // Scan Settings
    private var autoScan: Boolean = false
    private var scanDepth: String = "normal" // shallow, normal, deep
    private var minFileSize: String = "medium" // small (1MB), medium (10MB), large (100MB)
    private var lastScanDate: Long = 0
    private var lastCleanDate: Long = 0
    
    // Cleaning Rules
    private var selectedCategories: MutableSet<String> = mutableSetOf()
    private var cleaningRules: MutableMap<String, Boolean> = mutableMapOf()
    
    // Statistics
    private var totalItemsCleaned: Long = 0
    private var totalSpaceFreed: Long = 0
    private var totalCleanings: Long = 0
    
    // Advanced Settings
    private var showConfetti: Boolean = true
    private var detailedLogging: Boolean = false
    private var backupBeforeClean: Boolean = false
    private var confirmBeforeClean: Boolean = false

    // Custom User Fields
    private var greeting: String = ""
    private var shortcut: String = ""

    companion object {
        fun getInstance(): SettingsService {
            return ApplicationManager.getApplication().getService(SettingsService::class.java)
        }
    }

    // Language and Theme
    fun getLanguage(): String = language
    fun setLanguage(lang: String) { language = lang }

    fun getTheme(): String = theme
    fun setTheme(thm: String) { theme = thm }

    // Scan Settings
    fun isAutoScan(): Boolean = autoScan
    fun setAutoScan(auto: Boolean) { autoScan = auto }

    fun getScanDepth(): String = scanDepth
    fun setScanDepth(depth: String) { scanDepth = depth }

    fun getMinFileSize(): String = minFileSize
    fun setMinFileSize(size: String) { minFileSize = size }

    fun getLastScanDate(): Long = lastScanDate
    fun setLastScanDate(date: Long) { lastScanDate = date }

    fun getLastCleanDate(): Long = lastCleanDate
    fun setLastCleanDate(date: Long) { lastCleanDate = date }

    // Cleaning Rules
    fun getSelectedCategories(): Set<String> = selectedCategories
    fun setSelectedCategories(categories: Set<String>) { selectedCategories = categories.toMutableSet() }

    fun addSelectedCategory(category: String) { selectedCategories.add(category) }
    fun removeSelectedCategory(category: String) { selectedCategories.remove(category) }
    fun clearSelectedCategories() { selectedCategories.clear() }
    fun hasSelectedCategory(category: String): Boolean = selectedCategories.contains(category)

    fun getCleaningRules(): Map<String, Boolean> = cleaningRules.toMap()
    fun setCleaningRules(rules: Map<String, Boolean>) { cleaningRules = rules.toMutableMap() }
    fun getCleaningRule(key: String): Boolean = cleaningRules[key] ?: true
    fun setCleaningRule(key: String, enabled: Boolean) { cleaningRules[key] = enabled }

    // Statistics
    fun getTotalItemsCleaned(): Long = totalItemsCleaned
    fun addItemsCleaned(count: Long) { totalItemsCleaned += count }

    fun getTotalSpaceFreed(): Long = totalSpaceFreed
    fun addSpaceFreed(bytes: Long) { totalSpaceFreed += bytes }

    fun getTotalCleanings(): Long = totalCleanings
    fun incrementCleanings() { totalCleanings++ }

    // Advanced Settings
    fun isShowConfetti(): Boolean = showConfetti
    fun setShowConfetti(show: Boolean) { showConfetti = show }

    fun isDetailedLogging(): Boolean = detailedLogging
    fun setDetailedLogging(detailed: Boolean) { detailedLogging = detailed }

    fun isBackupBeforeClean(): Boolean = backupBeforeClean
    fun setBackupBeforeClean(backup: Boolean) { backupBeforeClean = backup }

    fun isConfirmBeforeClean(): Boolean = confirmBeforeClean
    fun setConfirmBeforeClean(confirm: Boolean) { confirmBeforeClean = confirm }

    // Custom User Fields
    fun getGreeting(): String = greeting
    fun setGreeting(value: String) { greeting = value }
    fun getShortcut(): String = shortcut
    fun setShortcut(value: String) { shortcut = value }

    // Utility Methods
    fun getMinFileSizeBytes(): Long {
        return when (minFileSize) {
            "small" -> 1024 * 1024L // 1MB
            "large" -> 100 * 1024 * 1024L // 100MB
            else -> 10 * 1024 * 1024L // 10MB (medium)
        }
    }

    fun getScanDepthLevel(): Int {
        return when (scanDepth) {
            "shallow" -> 1
            "deep" -> 3
            else -> 2 // normal
        }
    }

    fun isCategoryEnabled(category: String): Boolean {
        return selectedCategories.isEmpty() || selectedCategories.contains(category)
    }

    fun resetToDefaults() {
        language = "en"
        theme = "light"
        autoScan = false
        scanDepth = "normal"
        minFileSize = "medium"
        selectedCategories.clear()
        cleaningRules.clear()
        showConfetti = true
        detailedLogging = false
        backupBeforeClean = false
        confirmBeforeClean = false
    }

    fun getStatistics(): Map<String, Any> {
        return mapOf(
            "totalItemsCleaned" to totalItemsCleaned,
            "totalSpaceFreed" to totalSpaceFreed,
            "totalCleanings" to totalCleanings,
            "lastScanDate" to lastScanDate,
            "lastCleanDate" to lastCleanDate
        )
    }

    override fun getState(): SettingsService = this

    override fun loadState(state: SettingsService) {
        XmlSerializerUtil.copyBean(state, this)
    }
} 