package com.example.hujjah.data.repository.hujjah

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.hujjah.data.local.NoteDatabase
import com.example.hujjah.domain.model.islamic.TilawahDailySummary
import com.example.hujjah.domain.model.islamic.TilawahSessionLog
import com.example.hujjah.domain.model.islamic.TilawahSourceType
import com.example.hujjah.domain.model.islamic.TilawahStreakSummary
import com.example.hujjah.domain.repository.hujjah.TilawahRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class TilawahRepositoryImpl(
    private val database: NoteDatabase
) : TilawahRepository {

    private val queries = database.hujjahQueries

    override fun getStreakSummary(): Flow<TilawahStreakSummary> {
        return queries.getAllTilawahLogs()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                val logs = entities.map { entity ->
                    TilawahSessionLog(
                        id = entity.id,
                        timestamp = entity.timestamp,
                        sourceType = try { TilawahSourceType.valueOf(entity.sourceType) } catch (e: Exception) { TilawahSourceType.QURAN },
                        title = entity.title,
                        durationSeconds = entity.durationSeconds,
                        itemsReadCount = entity.itemsReadCount.toInt(),
                        dateString = entity.dateString,
                        photoPath = entity.photoPath,
                        note = entity.note
                    )
                }

                val tz = TimeZone.currentSystemDefault()
                val todayDate = Clock.System.now().toLocalDateTime(tz).date
                val todayDateString = todayDate.toString()

                val logsToday = logs.filter { it.dateString == todayDateString }
                val hasReadToday = logsToday.isNotEmpty()
                val totalDurationTodaySeconds = logsToday.sumOf { it.durationSeconds }
                val totalItemsToday = logsToday.sumOf { it.itemsReadCount }

                val dateSet = logs.map { it.dateString }.toSet()

                var streakDays = 0
                var checkDate = if (hasReadToday) todayDate else todayDate.minus(1, DateTimeUnit.DAY)

                while (dateSet.contains(checkDate.toString())) {
                    streakDays++
                    checkDate = checkDate.minus(1, DateTimeUnit.DAY)
                }

                TilawahStreakSummary(
                    currentStreakDays = streakDays,
                    totalDurationTodaySeconds = totalDurationTodaySeconds,
                    totalItemsToday = totalItemsToday,
                    hasReadToday = hasReadToday,
                    totalSessionsAllTime = logs.size
                )
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getAllLogs(): Flow<List<TilawahSessionLog>> {
        return queries.getAllTilawahLogs()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                entities.map { entity ->
                    TilawahSessionLog(
                        id = entity.id,
                        timestamp = entity.timestamp,
                        sourceType = try { TilawahSourceType.valueOf(entity.sourceType) } catch (e: Exception) { TilawahSourceType.QURAN },
                        title = entity.title,
                        durationSeconds = entity.durationSeconds,
                        itemsReadCount = entity.itemsReadCount.toInt(),
                        dateString = entity.dateString,
                        photoPath = entity.photoPath,
                        note = entity.note
                    )
                }
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getLogsByDate(dateString: String): Flow<List<TilawahSessionLog>> {
        return queries.getTilawahLogsByDate(dateString)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                entities.map { entity ->
                    TilawahSessionLog(
                        id = entity.id,
                        timestamp = entity.timestamp,
                        sourceType = try { TilawahSourceType.valueOf(entity.sourceType) } catch (e: Exception) { TilawahSourceType.QURAN },
                        title = entity.title,
                        durationSeconds = entity.durationSeconds,
                        itemsReadCount = entity.itemsReadCount.toInt(),
                        dateString = entity.dateString,
                        photoPath = entity.photoPath,
                        note = entity.note
                    )
                }
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getDailySummaries(): Flow<Map<String, TilawahDailySummary>> {
        return getAllLogs().map { logs ->
            logs.groupBy { it.dateString }.mapValues { (dateStr, dateLogs) ->
                TilawahDailySummary(
                    dateString = dateStr,
                    totalDurationSeconds = dateLogs.sumOf { it.durationSeconds },
                    totalItemsRead = dateLogs.sumOf { it.itemsReadCount },
                    logs = dateLogs
                )
            }
        }
    }

    override suspend fun saveSessionLog(log: TilawahSessionLog) = withContext(Dispatchers.Default) {
        queries.insertTilawahLog(
            id = log.id,
            timestamp = log.timestamp,
            sourceType = log.sourceType.name,
            title = log.title,
            durationSeconds = log.durationSeconds,
            itemsReadCount = log.itemsReadCount.toLong(),
            dateString = log.dateString,
            photoPath = log.photoPath,
            note = log.note
        )
    }

    override suspend fun deleteSessionLog(id: String) = withContext(Dispatchers.Default) {
        queries.deleteTilawahLogById(id)
    }
}
