package com.example.hujjah.presentation.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import org.jetbrains.compose.resources.painterResource
import hujjah.composeapp.generated.resources.Res
import hujjah.composeapp.generated.resources.mosque_silhouette
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.LocalFireDepartment
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.hujjah.domain.model.islamic.TilawahStreakSummary
import com.example.hujjah.presentation.components.hujjah.HujjahMenuItem
import com.example.hujjah.presentation.components.hujjah.HujjahSprint2MenuBar
import com.example.hujjah.presentation.theme.LocalHujjahColors
import com.example.hujjah.presentation.theme.customShapes
import com.example.hujjah.presentation.theme.hujjahArabicTypography
import com.example.hujjah.presentation.theme.spacing
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLens: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToHadith: () -> Unit,
    onNavigateToKoleksi: () -> Unit = {},
    onNavigateToTilawahHistory: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    onNavigateToAddNote: () -> Unit, // compatibility
    onNavigateToDetail: (Long) -> Unit, // compatibility
    onNavigateToAI: () -> Unit, // compatibility
    viewModel: HomeViewModel = koinViewModel()
) {
    val durationSeconds by viewModel.readingDurationSeconds.collectAsStateWithLifecycle()
    val itemsReadTodayPref by viewModel.itemsReadToday.collectAsStateWithLifecycle()
    val versesReadToday by viewModel.versesReadToday.collectAsStateWithLifecycle()
    val hadithsReadToday by viewModel.hadithsReadToday.collectAsStateWithLifecycle()
    val tilawahStreakSummary by viewModel.tilawahStreakSummary.collectAsStateWithLifecycle()
    val dailyTargetMinutes by viewModel.dailyTargetMinutes.collectAsStateWithLifecycle()
    val quote by viewModel.quoteOfTheDay.collectAsStateWithLifecycle()
    val lastReadLoc by viewModel.lastReadLocation.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val profileImageBase64 by viewModel.profileImageBase64.collectAsStateWithLifecycle()

    val colors = LocalHujjahColors.current
    
    // Effective total duration & items read combine manual & automatic tracking
    val effectiveDurationSeconds = maxOf(durationSeconds, tilawahStreakSummary.totalDurationTodaySeconds.toInt())
    val effectiveItemsRead = maxOf(itemsReadTodayPref, tilawahStreakSummary.totalItemsToday)
    val targetSeconds = dailyTargetMinutes * 60
    val progress = (effectiveDurationSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600)
    )

    val charcoalColor = MaterialTheme.colorScheme.onBackground
    val warmGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val warmIvoryColor = MaterialTheme.colorScheme.background
    val outlineColor = MaterialTheme.colorScheme.outline

    Scaffold(
        containerColor = warmIvoryColor,
        bottomBar = {
            HujjahSprint2MenuBar(
                currentItem = HujjahMenuItem.HOME,
                onNavigateToHome = onNavigateToHome,
                onNavigateToLens = onNavigateToLens,
                onNavigateToQuran = onNavigateToQuran,
                onNavigateToHadith = onNavigateToHadith,
                onNavigateToKoleksi = onNavigateToKoleksi,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = MaterialTheme.spacing.extraLarge,
                    vertical = MaterialTheme.spacing.large
                ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                // ==================== 1. EDITORIAL HEADER ====================
                EditorialHeader(
                    userName = userName,
                    profileImageBase64 = profileImageBase64,
                    onNavigateToProfile = onNavigateToProfile,
                    goldHighlight = colors.goldHighlight,
                    charcoalColor = charcoalColor,
                    warmGrayColor = warmGrayColor
                )

                // ==================== 2. UNIFIED READING RITUAL HERO ====================
                ReadingRitualSection(
                    durationSeconds = effectiveDurationSeconds,
                    itemsReadToday = effectiveItemsRead,
                    versesReadToday = versesReadToday,
                    hadithsReadToday = hadithsReadToday,
                    targetMinutes = dailyTargetMinutes,
                    animatedProgress = animatedProgress,
                    isTimerRunning = isTimerRunning,
                    streakDays = tilawahStreakSummary.currentStreakDays,
                    onToggleTimer = { viewModel.toggleTimer() },
                    onResetTimer = { viewModel.resetReadingTime() },
                    onCycleTarget = {
                        val nextTarget = when (dailyTargetMinutes) {
                            10 -> 15
                            15 -> 30
                            30 -> 60
                            else -> 10
                        }
                        viewModel.updateDailyTargetMinutes(nextTarget)
                    },
                    onNavigateToKoleksi = onNavigateToKoleksi,
                    onNavigateToTilawahHistory = onNavigateToTilawahHistory,
                    goldHighlight = colors.goldHighlight,
                    charcoalColor = charcoalColor,
                    warmGrayColor = warmGrayColor,
                    warmIvoryColor = warmIvoryColor,
                    outlineColor = outlineColor
                )

                // ==================== 3. EDITORIAL LAST READ SECTION ====================
                LastReadSection(
                    lastReadLoc = lastReadLoc,
                    onNavigateToQuran = onNavigateToQuran,
                    charcoalColor = charcoalColor,
                    warmGrayColor = warmGrayColor,
                    outlineColor = outlineColor
                )

                // ==================== 4. EDITORIAL DAILY QUOTE SECTION ====================
                EditorialDailyQuote(
                    quote = quote,
                    goldHighlight = colors.goldHighlight,
                    charcoalColor = charcoalColor,
                    warmGrayColor = warmGrayColor,
                    outlineColor = outlineColor
                )
            }
    }
}

// ==================== COMPONENT 1: EDITORIAL HEADER ====================
@Composable
private fun EditorialHeader(
    userName: String,
    profileImageBase64: String,
    onNavigateToProfile: () -> Unit,
    goldHighlight: Color,
    charcoalColor: Color,
    warmGrayColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = MaterialTheme.spacing.medium)) {
            Text(
                text = "Assalamualaikum,",
                style = MaterialTheme.typography.bodyMedium,
                color = warmGrayColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = charcoalColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifikasi",
                    tint = charcoalColor
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(goldHighlight)
                    .border(1.5.dp, goldHighlight.copy(alpha = 0.6f), CircleShape)
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageBase64.isNotEmpty()) {
                    @OptIn(ExperimentalEncodingApi::class)
                    val imageBytes = try {
                        Base64.decode(profileImageBase64)
                    } catch (e: Exception) {
                        null
                    }
                    AsyncImage(
                        model = imageBytes,
                        contentDescription = "Profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "H",
                        color = charcoalColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== COMPONENT 2: UNIFIED READING RITUAL HERO ====================
@Composable
private fun ReadingRitualSection(
    durationSeconds: Int,
    itemsReadToday: Int,
    versesReadToday: Int = 0,
    hadithsReadToday: Int = 0,
    targetMinutes: Int,
    animatedProgress: Float,
    isTimerRunning: Boolean,
    streakDays: Int,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onCycleTarget: () -> Unit,
    onNavigateToKoleksi: () -> Unit,
    onNavigateToTilawahHistory: () -> Unit = {},
    goldHighlight: Color,
    charcoalColor: Color,
    warmGrayColor: Color,
    warmIvoryColor: Color,
    outlineColor: Color
) {
    val colors = LocalHujjahColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.customShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, goldHighlight.copy(alpha = 0.20f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 1. Soft Warm Ambient Glow Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                goldHighlight.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )

            // 2. Mosque Silhouette Image Background (Subtle Luxury Gold Background Watermark)
            Image(
                painter = painterResource(Res.drawable.mosque_silhouette),
                contentDescription = "Mosque Silhouette",
                contentScale = ContentScale.FillWidth,
                colorFilter = ColorFilter.tint(
                    if (colors.isDarkTheme) Color(0xFFC5A059).copy(alpha = 0.28f)
                    else Color(0xFFD4AF37).copy(alpha = 0.24f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .align(Alignment.BottomCenter)
            )

            // 3. Hero Content Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                // Top Header Row: Streak Display & Kalender Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = goldHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "$streakDays Hari Streak",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = charcoalColor
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToTilawahHistory() }
                    ) {
                        Text(
                            text = "Kalender",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldHighlight
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Kalender Tilawah",
                            tint = goldHighlight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Custom Circular Ritual Dial
                CustomRitualDial(
                    durationSeconds = durationSeconds,
                    itemsReadToday = itemsReadToday,
                    versesReadToday = versesReadToday,
                    hadithsReadToday = hadithsReadToday,
                    animatedProgress = animatedProgress,
                    goldHighlight = goldHighlight,
                    baseOutlineColor = outlineColor,
                    charcoalColor = charcoalColor,
                    warmGrayColor = warmGrayColor
                )

                // Adjustable Target Label (Clickable to change target)
                Surface(
                    color = goldHighlight.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { onCycleTarget() }
                ) {
                    Text(
                        text = "Target harian: $targetMinutes menit ✎",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = goldHighlight,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Play / Pause Control Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(charcoalColor)
                            .clickable(onClick = onToggleTimer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isTimerRunning) "Jeda Mengaji" else "Mulai Mengaji",
                            tint = warmIvoryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = if (isTimerRunning) "Timer Aktif..." else "Mulai / Jeda Manual",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = charcoalColor
                    )
                }

                // Reset Action Button with Frame
                OutlinedButton(
                    onClick = onResetTimer,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.35f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        contentColor = warmGrayColor
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔄 Reset Progres",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = warmGrayColor
                        )
                    }
                }
            }
        }
    }
}

// ==================== SUB-COMPONENT: MOSQUE SILHOUETTE CANVAS ====================
@Composable
private fun MosqueSilhouetteCanvas(
    goldHighlight: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val baseLine = h * 0.96f

        val path = Path().apply {
            moveTo(0f, baseLine)
            lineTo(w * 0.04f, baseLine)
            lineTo(w * 0.04f, h * 0.72f)

            // 1. Far Left Outer Small Dome
            cubicTo(w * 0.03f, h * 0.60f, w * 0.12f, h * 0.60f, w * 0.11f, h * 0.72f)

            // 2. Main Left Minaret
            lineTo(w * 0.14f, h * 0.72f)
            lineTo(w * 0.14f, h * 0.38f)
            lineTo(w * 0.125f, h * 0.38f)
            lineTo(w * 0.125f, h * 0.35f)
            lineTo(w * 0.175f, h * 0.35f)
            lineTo(w * 0.175f, h * 0.38f)
            lineTo(w * 0.16f, h * 0.38f)
            lineTo(w * 0.15f, h * 0.18f)
            lineTo(w * 0.15f, h * 0.72f)

            // 3. Left Inner Secondary Minaret
            lineTo(w * 0.20f, h * 0.70f)
            lineTo(w * 0.20f, h * 0.44f)
            lineTo(w * 0.215f, h * 0.38f)
            lineTo(w * 0.23f, h * 0.44f)
            lineTo(w * 0.23f, h * 0.70f)

            // 4. Left Medium Flanking Onion Dome
            lineTo(w * 0.25f, h * 0.64f)
            cubicTo(w * 0.22f, h * 0.46f, w * 0.38f, h * 0.46f, w * 0.35f, h * 0.64f)

            // 5. GRAND CENTRAL MAIN ONION DOME
            lineTo(w * 0.36f, h * 0.58f)
            cubicTo(w * 0.30f, h * 0.35f, w * 0.40f, h * 0.16f, w * 0.50f, h * 0.14f)
            lineTo(w * 0.50f, h * 0.02f)
            lineTo(w * 0.50f, h * 0.14f)
            cubicTo(w * 0.60f, h * 0.16f, w * 0.70f, h * 0.35f, w * 0.64f, h * 0.58f)

            // 6. Right Medium Flanking Onion Dome
            lineTo(w * 0.65f, h * 0.64f)
            cubicTo(w * 0.62f, h * 0.46f, w * 0.78f, h * 0.46f, w * 0.75f, h * 0.64f)

            // 7. Right Inner Secondary Minaret
            lineTo(w * 0.77f, h * 0.70f)
            lineTo(w * 0.77f, h * 0.44f)
            lineTo(w * 0.785f, h * 0.38f)
            lineTo(w * 0.80f, h * 0.44f)
            lineTo(w * 0.80f, h * 0.70f)

            // 8. Main Right Minaret
            lineTo(w * 0.84f, h * 0.72f)
            lineTo(w * 0.84f, h * 0.38f)
            lineTo(w * 0.825f, h * 0.38f)
            lineTo(w * 0.825f, h * 0.35f)
            lineTo(w * 0.875f, h * 0.35f)
            lineTo(w * 0.875f, h * 0.38f)
            lineTo(w * 0.86f, h * 0.38f)
            lineTo(w * 0.85f, h * 0.18f)
            lineTo(w * 0.85f, h * 0.72f)

            // 9. Far Right Outer Small Dome
            lineTo(w * 0.89f, h * 0.72f)
            cubicTo(w * 0.88f, h * 0.60f, w * 0.97f, h * 0.60f, w * 0.96f, h * 0.72f)

            lineTo(w * 0.96f, baseLine)
            lineTo(w, baseLine)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = path,
            color = goldHighlight.copy(alpha = 0.05f)
        )
    }
}

// ==================== SUB-COMPONENT: CUSTOM RITUAL DIAL ====================
@Composable
private fun CustomRitualDial(
    durationSeconds: Int,
    itemsReadToday: Int,
    versesReadToday: Int = 0,
    hadithsReadToday: Int = 0,
    animatedProgress: Float,
    goldHighlight: Color,
    baseOutlineColor: Color,
    charcoalColor: Color,
    warmGrayColor: Color,
    modifier: Modifier = Modifier
) {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val minutesRead = minutes.toString().padStart(2, '0')
    val secondsRead = seconds.toString().padStart(2, '0')

    val detailLabel = when {
        versesReadToday > 0 && hadithsReadToday > 0 -> "$versesReadToday AYAT • $hadithsReadToday HADITS"
        versesReadToday > 0 -> "$versesReadToday AYAT"
        hadithsReadToday > 0 -> "$hadithsReadToday HADITS"
        itemsReadToday > 0 -> "$itemsReadToday ITEM"
        else -> "0 AYAT & HADITS"
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(200.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (minOf(size.width, size.height) / 2f) - 12.dp.toPx()

            // 1. Thin Base Circle
            drawCircle(
                color = baseOutlineColor.copy(alpha = 0.4f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 2. 12 Tick Marks Around Dial
            for (i in 0 until 12) {
                val angleInDegrees = i * 30f
                val angleInRadians = angleInDegrees * (PI / 180f).toFloat()
                val tickLength = if (i % 3 == 0) 8.dp.toPx() else 4.dp.toPx()
                val tickColor = if (i % 3 == 0) goldHighlight.copy(alpha = 0.7f) else baseOutlineColor.copy(alpha = 0.5f)
                val strokeWidth = if (i % 3 == 0) 1.5.dp.toPx() else 1.dp.toPx()

                val outerX = center.x + (radius - 4.dp.toPx()) * cos(angleInRadians)
                val outerY = center.y + (radius - 4.dp.toPx()) * sin(angleInRadians)
                val innerX = center.x + (radius - 4.dp.toPx() - tickLength) * cos(angleInRadians)
                val innerY = center.y + (radius - 4.dp.toPx() - tickLength) * sin(angleInRadians)

                drawLine(
                    color = tickColor,
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 3. Muted Gold Progress Arc
            val arcPadding = 12.dp.toPx()
            val arcDiameter = size.width - (arcPadding * 2f)
            drawArc(
                color = goldHighlight,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = Offset(arcPadding, arcPadding),
                size = Size(arcDiameter, arcDiameter),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Center Time Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$minutesRead:$secondsRead",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = charcoalColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detailLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = goldHighlight
            )
        }
    }
}

// ==================== COMPONENT 3: EDITORIAL LAST READ SECTION ====================
@Composable
private fun LastReadSection(
    lastReadLoc: String,
    onNavigateToQuran: () -> Unit,
    charcoalColor: Color,
    warmGrayColor: Color,
    outlineColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToQuran() },
        shape = MaterialTheme.customShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TERAKHIR DIBACA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = warmGrayColor,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (lastReadLoc.isNotEmpty()) lastReadLoc else "QS. Al-Kahfi: Ayat 10",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = charcoalColor
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Lanjutkan Membaca",
                tint = warmGrayColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ==================== COMPONENT 4: EDITORIAL DAILY QUOTE SECTION ====================
@Composable
private fun EditorialDailyQuote(
    quote: QuoteData,
    goldHighlight: Color,
    charcoalColor: Color,
    warmGrayColor: Color,
    outlineColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.customShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KUTIPAN HARI INI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = warmGrayColor,
                    letterSpacing = 1.2.sp
                )
                Surface(
                    color = goldHighlight.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "⏳ Rotasi 1m",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldHighlight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Text(
                text = quote.arabic,
                style = hujjahArabicTypography().verse,
                color = charcoalColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Text(
                text = "\"${quote.translation}\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = charcoalColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Text(
                text = quote.reference,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = goldHighlight
            )
        }
    }
}
