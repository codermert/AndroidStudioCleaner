package com.mert.salik.cleaner.models

data class CategoryCleanStats(val itemsCleaned: Int, val spaceFreed: Long)

data class CleaningResult(
    val totalItems: Int = 0,
    val cleanedItems: Int = 0,
    val totalSize: Long = 0,
    val freedSpace: Long = 0,
    val duration: Long = 0,
    val errors: List<String> = emptyList(),
    val success: Boolean = true,
    val categoryStats: Map<String, CategoryCleanStats> = emptyMap()
) {
    fun getFormattedTotalSize(): String = formatSize(totalSize)
    fun getFormattedFreedSpace(): String = formatSize(freedSpace)
    fun getFormattedDuration(): String = formatDuration(duration)

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        return when {
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
} 