package com.mert.salik.cleaner.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import java.text.SimpleDateFormat
import java.util.*

@Service
class LogService {
    private val logs = mutableListOf<LogEntry>()
    private val maxLogs = 1000

    companion object {
        fun getInstance(): LogService {
            return ApplicationManager.getApplication().getService(LogService::class.java)
        }
    }

    fun addLog(level: LogLevel, message: String, category: String = "General") {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            message = message,
            category = category
        )
        
        synchronized(logs) {
            logs.add(entry)
            if (logs.size > maxLogs) {
                logs.removeAt(0)
            }
        }
    }

    fun getLogs(): List<LogEntry> = synchronized(logs) { logs.toList() }

    fun getLogsByLevel(level: LogLevel): List<LogEntry> {
        return synchronized(logs) { logs.filter { it.level == level } }
    }

    fun getLogsByCategory(category: String): List<LogEntry> {
        return synchronized(logs) { logs.filter { it.category == category } }
    }

    fun clearLogs() {
        synchronized(logs) { logs.clear() }
    }

    fun getLogsSince(timestamp: Long): List<LogEntry> {
        return synchronized(logs) { logs.filter { it.timestamp >= timestamp } }
    }
}

enum class LogLevel {
    INFO, WARNING, ERROR, DEBUG
}

data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val message: String,
    val category: String
) {
    fun getFormattedTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    fun getFormattedMessage(): String {
        return "[${getFormattedTimestamp()}] [${level.name}] [$category] $message"
    }
} 