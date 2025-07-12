package com.mert.salik.cleaner.models

import java.io.File

data class CleaningItem(
    val id: String,
    val name: String,
    val category: CleaningCategory,
    val path: String,
    val size: Long = 0,
    val isSelected: Boolean = false,
    val isDirectory: Boolean = false,
    val description: String = "",
    val icon: String = ""
) {
    fun getFormattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    fun getFile(): File = File(path)
}

enum class CleaningCategory(val displayName: String, val icon: String) {
    ANDROID_STUDIO("Android Studio", "android"),
    EMULATOR("Emulator", "emulator"),
    DART("Dart", "dart"),
    KOTLIN("Kotlin", "kotlin"),
    JAVA("Java", "java"),
    GRADLE("Gradle", "gradle"),
    SYSTEM("System", "system"),
    BROWSER("Browser", "browser"),
    IDE("IDE", "ide"),
    OTHER("Other", "other")
} 