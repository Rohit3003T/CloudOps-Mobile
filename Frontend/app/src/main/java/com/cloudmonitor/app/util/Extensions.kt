package com.cloudmonitor.app.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Format an ISO-8601 timestamp string to a readable local date/time.
 * e.g. "2024-03-14T10:30:00.000Z" → "Mar 14, 2024 10:30"
 */
fun String?.formatDateTime(): String {
    if (this.isNullOrBlank()) return "—"
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        input.timeZone = TimeZone.getTimeZone("UTC")
        val date = input.parse(this.take(19)) ?: return this.take(19)
        val output = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        output.format(date)
    } catch (e: Exception) {
        this.take(19).replace("T", " ")
    }
}

/**
 * Format an ISO-8601 date string to just the date portion.
 * e.g. "2024-03-14T10:30:00Z" → "Mar 14, 2024"
 */
fun String?.formatDate(): String {
    if (this.isNullOrBlank()) return "—"
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = input.parse(this.take(10)) ?: return this.take(10)
        val output = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        output.format(date)
    } catch (e: Exception) {
        this.take(10)
    }
}

/**
 * Format bytes into a human-readable string.
 * e.g. 1536 → "1.5 KB", 2097152 → "2.0 MB"
 */
fun Long?.formatBytes(): String {
    if (this == null || this == 0L) return "0 B"
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> "${"%.1f".format(gb)} GB"
        mb >= 1.0 -> "${"%.1f".format(mb)} MB"
        kb >= 1.0 -> "${"%.1f".format(kb)} KB"
        else      -> "$this B"
    }
}

fun Double?.formatBytes(): String = this?.toLong().formatBytes()

/**
 * Format a duration in milliseconds.
 * e.g. 125000 → "2m 5s", 45000 → "45s"
 */
fun Long?.formatDuration(): String {
    if (this == null || this <= 0) return "—"
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return when {
        minutes > 0 -> "${minutes}m ${seconds}s"
        else        -> "${seconds}s"
    }
}

/**
 * Truncate a string and append ellipsis if longer than [maxLength].
 */
fun String?.truncate(maxLength: Int = 40): String {
    if (this.isNullOrBlank()) return "—"
    return if (this.length > maxLength) this.take(maxLength) + "…" else this
}

/**
 * Return the first N characters of a commit SHA (default 8).
 */
fun String?.shortSha(length: Int = 8): String {
    if (this.isNullOrBlank()) return "—"
    return this.take(length)
}

/**
 * Map an AWS service state string to a user-friendly label.
 */
fun String?.stateLabel(): String = when (this?.lowercase()) {
    "running"   -> "Running"
    "stopped"   -> "Stopped"
    "pending"   -> "Pending"
    "stopping"  -> "Stopping"
    "terminated"-> "Terminated"
    "in-use", "in_use" -> "In Use"
    "available" -> "Available"
    "active"    -> "Active"
    "success"   -> "Success"
    "failure"   -> "Failed"
    "in_progress" -> "In Progress"
    "queued"    -> "Queued"
    "completed" -> "Completed"
    null, ""    -> "Unknown"
    else        -> this.replaceFirstChar { it.uppercase() }
}
