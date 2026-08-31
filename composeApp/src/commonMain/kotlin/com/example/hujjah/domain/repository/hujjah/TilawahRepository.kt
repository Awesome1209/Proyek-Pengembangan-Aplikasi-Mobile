package com.example.hujjah.domain.repository.hujjah

import com.example.hujjah.domain.model.islamic.TilawahDailySummary
import com.example.hujjah.domain.model.islamic.TilawahSessionLog
import com.example.hujjah.domain.model.islamic.TilawahStreakSummary
import kotlinx.coroutines.flow.Flow

interface TilawahRepository {
    fun getStreakSummary(): Flow<TilawahStreakSummary>
    fun getAllLogs(): Flow<List<TilawahSessionLog>>
    fun getLogsByDate(dateString: String): Flow<List<TilawahSessionLog>>
    fun getDailySummaries(): Flow<Map<String, TilawahDailySummary>>
    suspend fun saveSessionLog(log: TilawahSessionLog)
    suspend fun deleteSessionLog(id: String)
}
