package com.example.hujjah.domain.model.islamic

enum class TilawahSourceType {
    QURAN,
    HADITH
}

data class TilawahSessionLog(
    val id: String,
    val timestamp: Long,
    val sourceType: TilawahSourceType,
    val title: String,
    val durationSeconds: Long,
    val itemsReadCount: Int,
    val dateString: String, // "YYYY-MM-DD"
    val photoPath: String? = null,
    val note: String? = null
)

data class TilawahDailySummary(
    val dateString: String,
    val totalDurationSeconds: Long,
    val totalItemsRead: Int,
    val logs: List<TilawahSessionLog>
)

data class TilawahStreakSummary(
    val currentStreakDays: Int,
    val totalDurationTodaySeconds: Long,
    val totalItemsToday: Int,
    val hasReadToday: Boolean,
    val totalSessionsAllTime: Int
)
