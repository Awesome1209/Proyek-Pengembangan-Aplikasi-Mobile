package com.example.hujjah.presentation.components.hujjah

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.example.hujjah.core.util.rememberImagePickerLauncher
import com.example.hujjah.domain.model.islamic.TilawahSessionLog
import com.example.hujjah.domain.model.islamic.TilawahSourceType
import com.example.hujjah.domain.repository.hujjah.TilawahRepository
import com.example.hujjah.presentation.theme.LocalHujjahColors
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

enum class SharePosterMode(val label: String, val icon: String) {
    SESI("Sesi Ini", "📌"),
    TOTAL_HARIAN("Total Harian", "📊")
}

enum class PosterTheme(
    val title: String,
    val icon: String,
    val quote: String,
    val labelItems: String,
    val labelDuration: String,
    val labelPace: String,
    val gradientColors: List<Color>
) {
    POHON(
        title = "Pohon Surga",
        icon = "🌳",
        quote = "Setiap ayat yang dibaca kelak tumbuh menjadi pohon rindang di Surga. (HR. Tirmidzi)",
        labelItems = "BANYAK AYAT & HADITS",
        labelDuration = "BANYAK WAKTU",
        labelPace = "AYAT / WAKTU",
        gradientColors = listOf(Color(0xFF143E32), Color(0xFF0B241D), Color(0xFF1A4A3C))
    ),
    NUR(
        title = "Rasi Nur",
        icon = "🌌",
        quote = "Al-Qur'an adalah cahaya di bumi dan simpanan di langit. (HR. Ath-Thabrani)",
        labelItems = "PANCARAN AYAT & HADITS",
        labelDuration = "BANYAK WAKTU BERSINAR",
        labelPace = "AYAT / WAKTU",
        gradientColors = listOf(Color(0xFF1E2D4A), Color(0xFF0F1829), Color(0xFF283B5E))
    ),
    SAMUDRA(
        title = "Samudra Wave",
        icon = "📜",
        quote = "Sekiranya lautan menjadi tinta untuk kalimat Tuhanku... (QS. Al-Kahf: 109)",
        labelItems = "BANYAK AYAT & HADITS",
        labelDuration = "BANYAK WAKTU SELAM",
        labelPace = "AYAT / WAKTU",
        gradientColors = listOf(Color(0xFF125464), Color(0xFF0A303A), Color(0xFF1A6B7F))
    ),
    TANGGA(
        title = "Tangga Manazil",
        icon = "🪜",
        quote = "Bacalah dan naiklah! Kedudukanmu pada akhir ayat yang dibaca. (HR. Abu Daud)",
        labelItems = "BANYAK TANGGA AYAT",
        labelDuration = "BANYAK WAKTU PENDAKIAN",
        labelPace = "AYAT / WAKTU",
        gradientColors = listOf(Color(0xFF2F442E), Color(0xFF1A281A), Color(0xFF3E583C))
    )
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun TilawahSharePosterDialog(
    sessionLog: TilawahSessionLog,
    streakDays: Int,
    onDismiss: () -> Unit
) {
    val colors = LocalHujjahColors.current
    val tilawahRepository = koinInject<TilawahRepository>()
    val coroutineScope = rememberCoroutineScope()

    var selectedShareMode by remember { mutableStateOf(SharePosterMode.SESI) }
    var selectedTheme by remember { mutableStateOf(PosterTheme.POHON) }
    var isSavedOrShared by remember { mutableStateOf(false) }
    var isSavedToGallery by remember { mutableStateOf(false) }
    var photoBase64 by remember { mutableStateOf(sessionLog.photoPath) }

    // Session-specific formatted data
    val sessionTimeFormatted = remember(sessionLog.timestamp) {
        if (sessionLog.timestamp > 0) {
            val instant = Instant.fromEpochMilliseconds(sessionLog.timestamp)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val hourStr = localDateTime.hour.toString().padStart(2, '0')
            val minStr = localDateTime.minute.toString().padStart(2, '0')
            "$hourStr:$minStr WIB"
        } else "WIB"
    }

    val sessionDurationFormatted = remember(sessionLog.durationSeconds) {
        val mins = sessionLog.durationSeconds / 60
        val secs = sessionLog.durationSeconds % 60
        if (mins > 0) "${mins}m ${secs}d" else "${secs}d"
    }

    val sessionPaceFormatted = remember(sessionLog.durationSeconds, sessionLog.itemsReadCount) {
        val count = sessionLog.itemsReadCount.coerceAtLeast(1)
        val paceSecs = sessionLog.durationSeconds / count
        val mins = paceSecs / 60
        val secs = paceSecs % 60
        val unit = if (sessionLog.sourceType == TilawahSourceType.QURAN) "Ayat" else "Hadits"
        if (mins > 0) "${mins}m ${secs}s /$unit" else "${secs}s /$unit"
    }

    val sessionItemLabel = remember(sessionLog.itemsReadCount, sessionLog.sourceType) {
        val unit = if (sessionLog.sourceType == TilawahSourceType.QURAN) "Ayat" else "Hadits"
        "${sessionLog.itemsReadCount} $unit"
    }

    // Aggregate Daily Totals for the specified date
    val allLogsForDate by tilawahRepository.getLogsByDate(sessionLog.dateString).collectAsState(initial = emptyList())

    val dailyTotalDuration = remember(allLogsForDate, sessionLog) {
        val sum = allLogsForDate.sumOf { it.durationSeconds }
        if (sum > 0) sum else sessionLog.durationSeconds
    }

    val versesToday = remember(allLogsForDate, sessionLog) {
        val quranLogs = allLogsForDate.filter { it.sourceType == TilawahSourceType.QURAN }
        if (quranLogs.isNotEmpty()) {
            quranLogs.sumOf { it.itemsReadCount }
        } else if (sessionLog.sourceType == TilawahSourceType.QURAN) {
            sessionLog.itemsReadCount
        } else 0
    }

    val hadithsToday = remember(allLogsForDate, sessionLog) {
        val hadithLogs = allLogsForDate.filter { it.sourceType == TilawahSourceType.HADITH }
        if (hadithLogs.isNotEmpty()) {
            hadithLogs.sumOf { it.itemsReadCount }
        } else if (sessionLog.sourceType == TilawahSourceType.HADITH) {
            sessionLog.itemsReadCount
        } else 0
    }

    val dailyTotalItems = remember(versesToday, hadithsToday) {
        val total = versesToday + hadithsToday
        if (total > 0) total else 1
    }

    val dailyDurationFormatted = remember(dailyTotalDuration) {
        val mins = dailyTotalDuration / 60
        val secs = dailyTotalDuration % 60
        if (mins > 0) "${mins}m ${secs}d" else "${secs}d"
    }

    val dailyPaceFormatted = remember(dailyTotalDuration, dailyTotalItems) {
        val paceSecsTotal = dailyTotalDuration / dailyTotalItems
        val mins = paceSecsTotal / 60
        val secs = paceSecsTotal % 60
        if (mins > 0) "${mins}m ${secs}s /Item" else "${secs}s /Item"
    }

    val dailyItemSummaryLabel = remember(versesToday, hadithsToday) {
        when {
            versesToday > 0 && hadithsToday > 0 -> "$versesToday Ayat • $hadithsToday Hadits"
            hadithsToday > 0 -> "$hadithsToday Hadits"
            else -> "$versesToday Ayat"
        }
    }

    // Active Metrics based on selected Share Mode (Sesi Ini vs Total Harian)
    val activeTitleBadge = if (selectedShareMode == SharePosterMode.SESI) "📌 SESI TILAWAH • $sessionTimeFormatted" else "✨ TOTAL HARIAN ${sessionLog.dateString}"
    val activeTitleText = if (selectedShareMode == SharePosterMode.SESI) sessionLog.title else "Jurnal ${sessionLog.dateString}"
    val activeItemsText = if (selectedShareMode == SharePosterMode.SESI) sessionItemLabel else dailyItemSummaryLabel
    val activeDurationText = if (selectedShareMode == SharePosterMode.SESI) sessionDurationFormatted else dailyDurationFormatted
    val activePaceText = if (selectedShareMode == SharePosterMode.SESI) sessionPaceFormatted else dailyPaceFormatted

    val imagePickerLauncher = rememberImagePickerLauncher { bytes ->
        if (bytes != null) {
            val base64 = Base64.encode(bytes)
            photoBase64 = base64
            coroutineScope.launch {
                tilawahRepository.saveSessionLog(sessionLog.copy(photoPath = base64))
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bagikan Story Tilawah",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ==================== 1. MODE SELECTOR (SESI INI vs TOTAL HARIAN) ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SharePosterMode.entries.forEach { mode ->
                        val isSelected = selectedShareMode == mode
                        Button(
                            onClick = { selectedShareMode = mode },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) colors.goldHighlight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
                        ) {
                            Text(
                                text = "${mode.icon} ${mode.label}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ==================== 2. HORIZONTAL THEME SELECTOR CHIPS ====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PosterTheme.entries.forEach { theme ->
                        val isSelected = selectedTheme == theme
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTheme = theme },
                            label = {
                                Text(
                                    text = "${theme.icon} ${theme.title}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.goldHighlight,
                                selectedLabelColor = Color.Black,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = colors.goldHighlight.copy(alpha = 0.4f),
                                selectedBorderColor = colors.goldHighlight
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==================== 3. INSTAGRAM STORY (9:16 VERTICAL STRAVA POSTER) ====================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = selectedTheme.gradientColors
                            )
                        )
                        .border(1.5.dp, colors.goldHighlight.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                ) {
                    // A. Background Activity Photo (if present)
                    if (!photoBase64.isNullOrEmpty()) {
                        val imageBytes = remember(photoBase64) {
                            try {
                                Base64.decode(photoBase64!!)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (imageBytes != null) {
                            AsyncImage(
                                model = imageBytes,
                                contentDescription = "Foto Kegiatan Tilawah",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // B. Soft Scrim Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = if (photoBase64.isNullOrEmpty()) 0.20f else 0.45f),
                                        Color.Black.copy(alpha = if (photoBase64.isNullOrEmpty()) 0.45f else 0.70f)
                                    )
                                )
                            )
                    )

                    // C. Non-Intrusive Background Ornaments (Corner Watermarks)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val goldColor = colors.goldHighlight
                        val hasPhoto = !photoBase64.isNullOrEmpty()
                        val opacityMultiplier = if (hasPhoto) 0.5f else 1.0f

                        when (selectedTheme) {
                            PosterTheme.POHON -> {
                                val cx = w * 0.85f
                                val cy = h * 0.12f
                                drawCircle(color = goldColor.copy(alpha = 0.08f * opacityMultiplier), radius = 60.dp.toPx(), center = Offset(cx, cy))
                                drawCircle(color = goldColor.copy(alpha = 0.12f * opacityMultiplier), radius = 40.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 1.dp.toPx()))
                            }

                            PosterTheme.NUR -> {
                                val cx = w * 0.85f
                                val cy = h * 0.12f
                                val r = 24.dp.toPx()
                                drawCircle(color = goldColor.copy(alpha = 0.06f * opacityMultiplier), radius = 45.dp.toPx(), center = Offset(cx, cy))
                                withTransform({ rotate(45f, Offset(cx, cy)) }) {
                                    drawRect(
                                        color = goldColor.copy(alpha = 0.15f * opacityMultiplier),
                                        topLeft = Offset(cx - r, cy - r),
                                        size = Size(r * 2, r * 2),
                                        style = Stroke(width = 1.2.dp.toPx())
                                    )
                                }
                            }

                            PosterTheme.SAMUDRA -> {
                                val path = Path()
                                val startY = h * 0.91f
                                path.moveTo(0f, startY)
                                var x = 0f
                                val step = w / 20f
                                while (x <= w) {
                                    val y = startY + sin(x * 0.02f) * 12.dp.toPx()
                                    path.lineTo(x, y)
                                    x += step
                                }
                                drawPath(path = path, color = goldColor.copy(alpha = 0.20f * opacityMultiplier), style = Stroke(width = 1.5.dp.toPx()))
                            }

                            PosterTheme.TANGGA -> {
                                val offset = 16.dp.toPx()
                                val arm = 24.dp.toPx()
                                drawLine(color = goldColor.copy(alpha = 0.25f * opacityMultiplier), start = Offset(offset, offset), end = Offset(offset + arm, offset), strokeWidth = 2.dp.toPx())
                                drawLine(color = goldColor.copy(alpha = 0.25f * opacityMultiplier), start = Offset(offset, offset), end = Offset(offset, offset + arm), strokeWidth = 2.dp.toPx())
                            }
                        }
                    }

                    // D. Instagram Story Content Layout (STRAVA STYLE POSTER)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Branding & Streak Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "HUJJAH",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = 2.sp,
                                    color = colors.goldHighlight
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = colors.goldHighlight.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (selectedShareMode == SharePosterMode.SESI) "SESSION" else "DAILY TOTAL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.goldHighlight,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Surface(
                                color = colors.goldHighlight.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(0.5.dp, colors.goldHighlight.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🔥", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$streakDays Hari Istiqamah",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.goldHighlight
                                    )
                                }
                            }
                        }

                        // Center Section: HIGH-CONTRAST METRICS STACK
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = activeTitleBadge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = activeTitleText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // --- METRIK 1: BANYAK AYAT / HADITS ---
                            Text(
                                text = selectedTheme.labelItems,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.goldHighlight.copy(alpha = 0.95f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeItemsText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(modifier = Modifier.width(60.dp), color = Color.White.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // --- METRIK 2: BANYAK WAKTU ---
                            Text(
                                text = selectedTheme.labelDuration,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.goldHighlight.copy(alpha = 0.95f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeDurationText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(modifier = Modifier.width(60.dp), color = Color.White.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // --- METRIK 3: AYAT / WAKTU (PACE TILAWAH) ---
                            Text(
                                text = selectedTheme.labelPace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.goldHighlight.copy(alpha = 0.95f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activePaceText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        // Bottom Section: Quote Card & Footer
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, colors.goldHighlight.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "\"${selectedTheme.quote}\"",
                                    fontSize = 10.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = colors.goldHighlight,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    lineHeight = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (selectedShareMode == SharePosterMode.SESI) "SESSION STORY • HUJJAH" else "DAILY TOTAL STORY • HUJJAH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ==================== 4. PHOTO ACTION BUTTONS ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher() },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.goldHighlight.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Foto Kegiatan",
                            tint = colors.goldHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (photoBase64.isNullOrEmpty()) "Foto Kegiatan" else "Ganti Foto",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.goldHighlight
                        )
                    }

                    if (!photoBase64.isNullOrEmpty()) {
                        IconButton(
                            onClick = {
                                photoBase64 = null
                                coroutineScope.launch {
                                    tilawahRepository.saveSessionLog(sessionLog.copy(photoPath = null))
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Foto",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ==================== 5. SAVE TO GALLERY & SHARE BUTTONS ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Simpan ke Galeri
                    OutlinedButton(
                        onClick = {
                            isSavedToGallery = true
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isSavedToGallery) colors.islamicGreen else colors.goldHighlight),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSavedToGallery) colors.islamicGreen.copy(alpha = 0.15f) else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isSavedToGallery) Icons.Default.Check else Icons.Default.FileDownload,
                            contentDescription = "Simpan Galeri",
                            tint = if (isSavedToGallery) colors.islamicGreen else colors.goldHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSavedToGallery) "Tersimpan!" else "Simpan Galeri",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSavedToGallery) colors.islamicGreen else colors.goldHighlight
                        )
                    }

                    // Tombol Bagikan Story
                    Button(
                        onClick = {
                            isSavedOrShared = true
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSavedOrShared) colors.islamicGreen else MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Icon(
                            imageVector = if (isSavedOrShared) Icons.Default.Check else Icons.Default.Share,
                            contentDescription = "Bagikan",
                            tint = colors.goldHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSavedOrShared) "Siap Bagikan!" else "Bagikan Story",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
