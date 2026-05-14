package com.example.akashiconline.ui.util

fun formatTimeMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    val amPm = if (h < 12) "AM" else "PM"
    val h12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return "$h12:${m.toString().padStart(2, '0')} $amPm"
}
