package com.mert.salik.cleaner.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.mert.salik.cleaner.models.CleaningItem
import com.mert.salik.cleaner.models.CleaningCategory
import com.mert.salik.cleaner.models.CleaningResult
import com.mert.salik.cleaner.models.CategoryCleanStats
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@Service
class CleaningService {
    private val languageService = LanguageService.getInstance()
    private val logService = LogService.getInstance()
    private val os = System.getProperty("os.name").lowercase()
    private val userHome = System.getProperty("user.home")

    companion object {
        fun getInstance(): CleaningService {
            return ApplicationManager.getApplication().getService(CleaningService::class.java)
        }
    }

    fun scanForCleaningItems(): List<CleaningItem> {
        val items = mutableListOf<CleaningItem>()
        
        try {
            logService.addLog(LogLevel.INFO, "Starting comprehensive scan for cleaning items", "CleaningService")
            
            // Android Studio cache
            scanAndroidStudioCache(items)
            
            // Emulator cache
            scanEmulatorCache(items)
            
            // Dart/Flutter cache
            scanDartCache(items)
            
            // Kotlin cache
            scanKotlinCache(items)
            
            // Java cache
            scanJavaCache(items)
            
            // Gradle cache
            scanGradleCache(items)
            
            // System temp files
            scanSystemTempFiles(items)
            
            // Browser cache (optional)
            scanBrowserCache(items)
            
            // IDE specific caches
            scanIDECaches(items)
            
            logService.addLog(LogLevel.INFO, "Comprehensive scan completed. Found ${items.size} items", "CleaningService")
            
        } catch (e: Exception) {
            logService.addLog(LogLevel.ERROR, "Error during scan: ${e.message}", "CleaningService")
        }
        
        return items
    }

    private fun getAndroidStudioPaths(): List<String> = when {
        os.contains("win") -> listOf(
            "$userHome/AppData/Local/Google/AndroidStudio*/caches",
            "$userHome/AppData/Roaming/Google/AndroidStudio*/logs",
            "$userHome/AppData/Local/Google/AndroidStudio*/temp"
        )
        os.contains("mac") -> listOf(
            "$userHome/Library/Caches/Google/AndroidStudio*",
            "$userHome/Library/Logs/Google/AndroidStudio*",
            "$userHome/Library/Application Support/Google/AndroidStudio*/temp"
        )
        else -> listOf(
            "$userHome/.cache/Google/AndroidStudio*",
            "$userHome/.config/Google/AndroidStudio*/logs",
            "$userHome/.config/Google/AndroidStudio*/temp"
        )
    }

    private fun getEmulatorPaths(): List<String> = when {
        os.contains("win") -> listOf(
            "$userHome/.android/avd",
            "$userHome/.android/cache",
            "$userHome/.android/logs"
        )
        os.contains("mac") -> listOf(
            "$userHome/.android/avd",
            "$userHome/.android/cache",
            "$userHome/.android/logs"
        )
        else -> listOf(
            "$userHome/.android/avd",
            "$userHome/.android/cache",
            "$userHome/.android/logs"
        )
    }

    private fun getDartPaths(): List<String> = when {
        os.contains("win") -> listOf(
            "$userHome/AppData/Roaming/Pub/Cache",
            "$userHome/AppData/Local/Pub/Cache",
            "$userHome/AppData/Local/Dart",
            "$userHome/AppData/Local/Flutter"
        )
        os.contains("mac") -> listOf(
            "$userHome/.pub-cache",
            "$userHome/.dart",
            "$userHome/.flutter"
        )
        else -> listOf(
            "$userHome/.pub-cache",
            "$userHome/.dart",
            "$userHome/.flutter"
        )
    }

    private fun getKotlinPaths(): List<String> = when {
        os.contains("win") -> listOf(
            "$userHome/.kotlin",
            "$userHome/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin"
        )
        else -> listOf(
            "$userHome/.kotlin",
            "$userHome/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin",
            "$userHome/.kotlin/cache",
            "$userHome/.kotlin/temp"
        )
    }

    private fun getJavaPaths(): List<String> = when {
        os.contains("win") -> listOf(
            "$userHome/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin",
            "$userHome/.m2/repository"
        )
        else -> listOf(
            "$userHome/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin",
            "$userHome/.m2/repository",
            "$userHome/.ivy2/cache",
            "$userHome/.sbt",
            "$userHome/.cache/coursier"
        )
    }

    private fun getGradlePaths(): List<String> = listOf(
        "$userHome/.gradle/caches",
        "$userHome/.gradle/wrapper/dists",
        "$userHome/.gradle/daemon",
        "$userHome/.gradle/native",
        "$userHome/.gradle/notifications",
        "$userHome/.gradle/workers",
        "$userHome/.gradle/buildOutputCleanup"
    )

    private fun getSystemTempPaths(): List<String> = when {
        os.contains("win") -> listOf(System.getProperty("java.io.tmpdir"))
        os.contains("mac") -> listOf("/tmp", System.getProperty("java.io.tmpdir"))
        else -> listOf("/tmp", "/var/tmp", System.getProperty("java.io.tmpdir"))
    }

    private fun getBrowserPaths(): List<String> = when {
        os.contains("win") -> listOf(
            "$userHome/AppData/Local/Google/Chrome/User Data/Default/Cache",
            "$userHome/AppData/Local/Mozilla/Firefox/Profiles/*.default*/cache2"
        )
        os.contains("mac") -> listOf(
            "$userHome/Library/Caches/Google/Chrome/Default/Cache",
            "$userHome/Library/Caches/Firefox/Profiles/*.default*/cache2"
        )
        else -> listOf(
            "$userHome/.cache/google-chrome",
            "$userHome/.cache/mozilla",
            "$userHome/.cache/chromium",
            "$userHome/.mozilla/firefox/*.default*/cache2",
            "$userHome/.config/google-chrome/Default/Cache"
        )
    }

    private fun getIDEPaths(): List<String> = when {
        os.contains("win") -> listOf(
            "$userHome/AppData/Roaming/JetBrains/IntelliJIdea*/caches",
            "$userHome/AppData/Local/JetBrains/IntelliJIdea*/system/caches"
        )
        os.contains("mac") -> listOf(
            "$userHome/Library/Caches/JetBrains/IntelliJIdea*",
            "$userHome/Library/Logs/JetBrains/IntelliJIdea*"
        )
        else -> listOf(
            "$userHome/.IntelliJIdea*/config/caches",
            "$userHome/.IntelliJIdea*/system/caches",
            "$userHome/.IntelliJIdea*/config/logs",
            "$userHome/.IntelliJIdea*/system/logs"
        )
    }

    private fun scanAndroidStudioCache(items: MutableList<CleaningItem>) {
        getAndroidStudioPaths().forEach { path ->
            scanPathWithWildcards(path, items, CleaningCategory.ANDROID_STUDIO, "Android Studio")
        }
    }

    private fun scanEmulatorCache(items: MutableList<CleaningItem>) {
        getEmulatorPaths().forEach { path ->
            scanPathWithWildcards(path, items, CleaningCategory.EMULATOR, "Emulator")
        }
    }

    private fun scanDartCache(items: MutableList<CleaningItem>) {
        getDartPaths().forEach { path ->
            scanPathWithWildcards(path, items, CleaningCategory.DART, "Dart/Flutter")
        }
    }

    private fun scanKotlinCache(items: MutableList<CleaningItem>) {
        getKotlinPaths().forEach { path ->
            scanPathWithWildcards(path, items, CleaningCategory.KOTLIN, "Kotlin")
        }
    }

    private fun scanJavaCache(items: MutableList<CleaningItem>) {
        getJavaPaths().forEach { path ->
            scanPathWithWildcards(path, items, CleaningCategory.JAVA, "Java")
        }
    }

    private fun scanGradleCache(items: MutableList<CleaningItem>) {
        getGradlePaths().forEach { path ->
            scanPathWithWildcards(path, items, CleaningCategory.GRADLE, "Gradle")
        }
    }

    private fun scanSystemTempFiles(items: MutableList<CleaningItem>) {
        getSystemTempPaths().forEach { path ->
            val tempDir = File(path)
            if (tempDir.exists() && tempDir.isDirectory) {
                val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                scanDirectoryForOldFiles(tempDir, items, CleaningCategory.SYSTEM, "System Temp", cutoffTime)
            }
        }
    }

    private fun scanBrowserCache(items: MutableList<CleaningItem>) {
        getBrowserPaths().forEach { path ->
            scanPathWithWildcards(path, items, CleaningCategory.BROWSER, "Browser")
        }
    }

    private fun scanIDECaches(items: MutableList<CleaningItem>) {
        getIDEPaths().forEach { path ->
            scanPathWithWildcards(path, items, CleaningCategory.IDE, "IDE")
        }
    }

    private fun scanPathWithWildcards(pathPattern: String, items: MutableList<CleaningItem>, category: CleaningCategory, categoryName: String) {
        try {
        val userHome = System.getProperty("user.home")
            val expandedPath = pathPattern.replace("~", userHome)
            
            if (expandedPath.contains("*")) {
                // Handle wildcards
                val parentDir = File(expandedPath.substringBeforeLast("/"))
                val pattern = expandedPath.substringAfterLast("/")
                
                if (parentDir.exists()) {
                    parentDir.listFiles()?.forEach { file ->
                        if (file.name.matches(pattern.replace("*", ".*").toRegex())) {
                            addCleaningItem(file, items, category, categoryName)
                        }
                    }
                }
            } else {
                val file = File(expandedPath)
            if (file.exists()) {
                    addCleaningItem(file, items, category, categoryName)
                }
            }
        } catch (e: Exception) {
            logService.addLog(LogLevel.WARNING, "Error scanning path $pathPattern: ${e.message}", "CleaningService")
        }
    }

    private fun scanDirectoryForOldFiles(directory: File, items: MutableList<CleaningItem>, category: CleaningCategory, categoryName: String, cutoffTime: Long) {
        try {
            directory.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime && file.length() > 1024 * 1024) { // Only files larger than 1MB
                    addCleaningItem(file, items, category, categoryName)
                }
            }
        } catch (e: Exception) {
            logService.addLog(LogLevel.WARNING, "Error scanning directory ${directory.path}: ${e.message}", "CleaningService")
        }
    }

    private fun addCleaningItem(file: File, items: MutableList<CleaningItem>, category: CleaningCategory, categoryName: String) {
        try {
                val size = calculateDirectorySize(file)
            // Add all items, even if size is 0
                items.add(CleaningItem(
                id = "${category.name.lowercase()}_${file.name}_${System.currentTimeMillis()}",
                name = "${categoryName}: ${file.name}",
                category = category,
                    path = file.absolutePath,
                    size = size,
                isDirectory = file.isDirectory,
                description = "${categoryName} cache files"
                ))
        } catch (e: Exception) {
            logService.addLog(LogLevel.WARNING, "Error adding cleaning item for ${file.path}: ${e.message}", "CleaningService")
        }
    }

    private fun calculateDirectorySize(directory: File): Long {
        return try {
            if (directory.isFile) {
                directory.length()
            } else {
            Files.walk(directory.toPath())
                .filter { Files.isRegularFile(it) }
                .mapToLong { Files.size(it) }
                .sum()
            }
        } catch (e: Exception) {
            0L
        }
    }

    fun cleanItems(items: List<CleaningItem>): CleaningResult {
        val startTime = System.currentTimeMillis()
        var cleanedItems = 0
        var freedSpace = 0L
        val errors = mutableListOf<String>()
        val categoryStats = mutableMapOf<String, CategoryCleanStats>()
        
        logService.addLog(LogLevel.INFO, "Starting cleaning process for ${items.size} items", "CleaningService")
        
        items.forEach { item ->
            try {
                val file = File(item.path)
                if (file.exists()) {
                    val size = calculateDirectorySize(file)
                    if (deleteFileOrDirectory(file)) {
                        cleanedItems++
                        freedSpace += size

                        val currentStats = categoryStats.getOrDefault(item.category.displayName, CategoryCleanStats(0, 0L))
                        categoryStats[item.category.displayName] = CategoryCleanStats(
                            itemsCleaned = currentStats.itemsCleaned + 1,
                            spaceFreed = currentStats.spaceFreed + size
                        )

                        logService.addLog(LogLevel.INFO, "Cleaned: ${item.path} (${formatSize(size)})", "CleaningService")
                    } else {
                        errors.add("Failed to delete: ${item.path}")
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Error cleaning ${item.path}: ${e.message}"
                errors.add(errorMsg)
                logService.addLog(LogLevel.ERROR, errorMsg, "CleaningService")
            }
        }
        
        val duration = System.currentTimeMillis() - startTime
        logService.addLog(LogLevel.INFO, "Cleaning completed. Cleaned $cleanedItems items, freed ${formatSize(freedSpace)}", "CleaningService")
        
        return CleaningResult(
            totalItems = items.size,
            cleanedItems = cleanedItems,
            totalSize = items.sumOf { it.size },
            freedSpace = freedSpace,
            duration = duration,
            errors = errors,
            success = errors.isEmpty(),
            categoryStats = categoryStats
        )
    }

    private fun deleteFileOrDirectory(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.listFiles()?.forEach { deleteFileOrDirectory(it) }
            }
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
} 