package com.example.hujjah.presentation.screens.tilawah

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hujjah.domain.model.islamic.TilawahSessionLog
import com.example.hujjah.domain.model.islamic.TilawahSourceType
import com.example.hujjah.domain.repository.hujjah.TilawahRepository
import com.example.hujjah.presentation.components.hujjah.HujjahEmptyState
import com.example.hujjah.presentation.components.hujjah.HujjahMenuItem
import com.example.hujjah.presentation.components.hujjah.HujjahSprint2MenuBar
import com.example.hujjah.presentation.components.hujjah.TilawahSharePosterDialog
import com.example.hujjah.presentation.theme.LocalHujjahColors
import com.example.hujjah.presentation.theme.customShapes
import com.example.hujjah.presentation.theme.spacing
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TilawahHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLens: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToHadith: () -> Unit,
    onNavigateToKoleksi: () -> Unit = {},
    onNavigateToProfile: () -> Unit
) {
    val tilawahRepository = koinInject<TilawahRepository>()
    val colors = LocalHujjahColors.current

    val streakSummary by tilawahRepository.getStreakSummary().collectAsStateWithLifecycle(initialValue = null)
    val dailySummaries by tilawahRepository.getDailySummaries().collectAsStateWithLifecycle(initialValue = emptyMap())
    val allLogs by tilawahRepository.getAllLogs().collectAsStateWithLifecycle(initialValue = emptyList())

    val todayDateString = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString() }
    var selectedDateString by remember { mutableStateOf(todayDateString) }
    var selectedPosterLog by remember { mutableStateOf<TilawahSessionLog?>(null) }

    val filteredLogs = remember(selectedDateString, allLogs) {
        if (selectedDateString.isBlank()) allLogs else allLogs.filter { it.dateString == selectedDateString }
    }

    val charcoalColor = MaterialTheme.colorScheme.onBackground
    val warmGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val warmIvoryColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline

    Scaffold(
        containerColor = warmIvoryColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kalender & Jurnal Tilawah",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = charcoalColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = charcoalColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = warmIvoryColor)
            )
        },
        bottomBar = {
            HujjahSprint2MenuBar(
                currentItem = HujjahMenuItem.KOLEKSI,
                onNavigateToHome = onNavigateToHome,
                onNavigateToLens = onNavigateToLens,
                onNavigateToQuran = onNavigateToQuran,
                onNavigateToHadith = onNavigateToHadith,
                onNavigateToKoleksi = onNavigateToKoleksi,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.spacing.extraLarge)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. STREAK SUMMARY HERO CARD
                item {
                    val summary = streakSummary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.customShapes.large,
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        border = BorderStroke(1.dp, colors.goldHighlight.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = colors.goldHighlight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "${summary?.currentStreakDays ?: 0} Hari Istiqamah",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = charcoalColor
                                        )
                                        Text(
                                            text = "Jurnal Keaktifan Mengaji Harian",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = warmGrayColor
                                        )
                                    }
                                }

                                Surface(
                                    color = colors.goldHighlight.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${summary?.totalSessionsAllTime ?: 0} Sesi Selesai",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.goldHighlight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. DAILY HEATMAP CALENDAR SELECTOR
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kalender Keaktifan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = charcoalColor
                            )
                            if (selectedDateString.isNotEmpty()) {
                                TextButton(onClick = { selectedDateString = "" }) {
                                    Text("Lihat Semua", color = colors.goldHighlight, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid of last 14 days
                        val last14Days = remember {
                            val tz = TimeZone.currentSystemDefault()
                            val now = Clock.System.now().toLocalDateTime(tz).date
                            (0 until 14).map { daysAgo ->
                                now.minus(daysAgo, kotlinx.datetime.DateTimeUnit.DAY).toString()
                            }.reversed()
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.height(130.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(last14Days.size) { idx ->
                                val dateStr = last14Days[idx]
                                val hasLogs = dailySummaries.containsKey(dateStr)
                                val isSelected = selectedDateString == dateStr
                                val dayNum = dateStr.takeLast(2)

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isSelected -> colors.goldHighlight
                                                hasLogs -> colors.goldHighlight.copy(alpha = 0.25f)
                                                else -> surfaceColor
                                            }
                                        )
                                        .border(
                                            0.5.dp,
                                            if (isSelected) colors.goldHighlight else outlineColor.copy(alpha = 0.2f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedDateString = dateStr },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNum,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected || hasLogs) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else charcoalColor
                                        )
                                        if (hasLogs && !isSelected) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.goldHighlight)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. DAILY SESSIONS LOG LIST
                item {
                    Text(
                        text = if (selectedDateString.isBlank()) "Semua Sesi Tilawah" else "Sesi Tanggal: $selectedDateString",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = charcoalColor
                    )
                }

                if (filteredLogs.isEmpty()) {
                    item {
                        HujjahEmptyState(
                            title = "Belum Ada Sesi Tilawah",
                            message = "Belum ada catatan aktivitas mengaji pada tanggal ini. Mulai tilawah hari ini untuk mengisi kalender istiqamah Anda!"
                        )
                    }
                } else {
                    items(filteredLogs, key = { it.id }) { log ->
                        val formattedDuration = remember(log.durationSeconds) {
                            val mins = log.durationSeconds / 60
                            val secs = log.durationSeconds % 60
                            if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
                        }

                        val sessionTimeFormatted = remember(log.timestamp) {
                            if (log.timestamp > 0) {
                                val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(log.timestamp)
                                val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                                val hourStr = localDateTime.hour.toString().padStart(2, '0')
                                val minStr = localDateTime.minute.toString().padStart(2, '0')
                                "Pukul $hourStr:$minStr WIB"
                            } else "Sesi Tilawah"
                        }

                        val itemUnit = if (log.sourceType == TilawahSourceType.QURAN) "Ayat" else "Hadits"
                        val paceSecs = remember(log.durationSeconds, log.itemsReadCount) {
                            val count = log.itemsReadCount.coerceAtLeast(1)
                            val pace = log.durationSeconds / count
                            val mins = pace / 60
                            val secs = pace % 60
                            if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.customShapes.large,
                            colors = CardDefaults.cardColors(containerColor = surfaceColor),
                            border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = colors.goldHighlight.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (log.sourceType == TilawahSourceType.QURAN) Icons.Outlined.MenuBook else Icons.Outlined.CollectionsBookmark,
                                                    contentDescription = null,
                                                    tint = colors.goldHighlight,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (log.sourceType == TilawahSourceType.QURAN) "AL-QUR'AN" else "HADITS PERAWI",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.goldHighlight
                                                )
                                            }
                                        }

                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Schedule,
                                                    contentDescription = null,
                                                    tint = warmGrayColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = sessionTimeFormatted,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = warmGrayColor
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = log.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = charcoalColor
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = warmGrayColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Durasi: $formattedDuration • ${log.itemsReadCount} $itemUnit • Pace: $paceSecs /$itemUnit",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = warmGrayColor
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { selectedPosterLog = log },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(colors.goldHighlight.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Bagikan Poster",
                                        tint = colors.goldHighlight
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Share Poster Dialog
    selectedPosterLog?.let { log ->
        TilawahSharePosterDialog(
            sessionLog = log,
            streakDays = streakSummary?.currentStreakDays ?: 1,
            onDismiss = { selectedPosterLog = null }
        )
    }
}
