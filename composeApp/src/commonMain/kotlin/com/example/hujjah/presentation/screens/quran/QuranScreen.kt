package com.example.hujjah.presentation.screens.quran

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hujjah.domain.model.islamic.SurahItem
import com.example.hujjah.presentation.components.hujjah.HujjahEmptyState
import com.example.hujjah.presentation.components.hujjah.HujjahErrorState
import com.example.hujjah.presentation.components.hujjah.HujjahMenuItem
import com.example.hujjah.presentation.components.hujjah.HujjahSprint2MenuBar
import com.example.hujjah.presentation.components.hujjah.ShimmerSurahItem
import com.example.hujjah.presentation.components.hujjah.shimmerBrush
import com.example.hujjah.presentation.theme.LocalHujjahColors
import com.example.hujjah.presentation.theme.customShapes
import com.example.hujjah.presentation.theme.hujjahArabicTypography
import com.example.hujjah.presentation.theme.spacing
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLens: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToHadith: () -> Unit,
    onNavigateToKoleksi: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    onNavigateToDetail: (Int, String) -> Unit,
    viewModel: QuranViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lastRead by viewModel.lastReadLocation.collectAsStateWithLifecycle()
    val colors = LocalHujjahColors.current

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
                        text = "AL-QUR'AN",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = charcoalColor
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = warmIvoryColor
                )
            )
        },
        bottomBar = {
            HujjahSprint2MenuBar(
                currentItem = HujjahMenuItem.QURAN,
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
                .padding(horizontal = MaterialTheme.spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            // Header Subtle Divider
            HorizontalDivider(
                color = colors.goldHighlight.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )

            // ==================== 1. LAST READ HERO ====================
            QuranLastReadHero(
                lastRead = lastRead,
                onClick = {
                    if (lastRead.isNotBlank()) {
                        val surahName = lastRead.substringBefore(":").replace("QS. ", "").trim()
                        onNavigateToDetail(18, surahName)
                    } else {
                        onNavigateToDetail(1, "Al-Fatihah")
                    }
                },
                goldHighlight = colors.goldHighlight,
                charcoalColor = charcoalColor,
                warmGrayColor = warmGrayColor,
                surfaceColor = surfaceColor
            )

            // ==================== 2. SEARCH BAR ====================
            QuranSearchField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                goldHighlight = colors.goldHighlight,
                warmGrayColor = warmGrayColor,
                surfaceColor = surfaceColor
            )

            // ==================== 3. SECTION HEADER ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Surah",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = charcoalColor
                )
                Text(
                    text = "${uiState.surahs.size} Surah",
                    style = MaterialTheme.typography.labelSmall,
                    color = warmGrayColor
                )
            }

            // ==================== 4. OPEN SURAH LIST (NO OUTER CARD) ====================
            if (uiState.isLoading) {
                val brush = shimmerBrush()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(6) { idx ->
                        ShimmerSurahItem(brush = brush)
                        if (idx < 5) {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = outlineColor.copy(alpha = 0.15f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            } else if (uiState.error != null) {
                HujjahErrorState(
                    message = uiState.error.orEmpty(),
                    onRetry = { viewModel.fetchSurahs(forceRefresh = true) }
                )
            } else if (uiState.surahs.isEmpty()) {
                HujjahEmptyState(
                    title = "Surah Tidak Ditemukan",
                    message = "Tidak ada surah yang cocok dengan pencarian \"${uiState.searchQuery}\". Coba kata kunci lain."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.surahs, key = { it.number }) { surah ->
                        SurahRowItem(
                            surah = surah,
                            onClick = { onNavigateToDetail(surah.number, surah.name) },
                            goldHighlight = colors.goldHighlight,
                            charcoalColor = charcoalColor,
                            warmGrayColor = warmGrayColor,
                            outlineColor = outlineColor
                        )
                    }
                }
            }
        }
    }
}

// ==================== COMPONENT 1: LAST READ HERO ====================
@Composable
private fun QuranLastReadHero(
    lastRead: String,
    onClick: () -> Unit,
    goldHighlight: Color,
    charcoalColor: Color,
    warmGrayColor: Color,
    surfaceColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.customShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        border = BorderStroke(1.dp, goldHighlight.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            // Warm Gold Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                goldHighlight.copy(alpha = 0.12f),
                                surfaceColor
                            )
                        )
                    )
            )

            // Mosque Silhouette Decoration (Canvas)
            MosqueHeroSilhouette(
                goldHighlight = goldHighlight,
                modifier = Modifier
                    .width(160.dp)
                    .fillMaxHeight()
                    .align(Alignment.BottomEnd)
            )

            // Content Row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.spacing.large),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "TERAKHIR DIBACA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = warmGrayColor,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = lastRead.ifBlank { "Mulai Membaca Al-Qur'an" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = charcoalColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (lastRead.isNotBlank()) "Lanjutkan bacaan terakhir" else "Belum ada bacaan terakhir",
                        style = MaterialTheme.typography.bodySmall,
                        color = warmGrayColor
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Buka Bacaan",
                    tint = charcoalColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ==================== COMPONENT 2: SEARCH FIELD ====================
@Composable
private fun QuranSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    goldHighlight: Color,
    warmGrayColor: Color,
    surfaceColor: Color
) {
    Surface(
        shape = MaterialTheme.customShapes.pill,
        color = surfaceColor,
        border = BorderStroke(1.dp, goldHighlight.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = goldHighlight,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Cari surah...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = warmGrayColor
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus Pencarian",
                        tint = warmGrayColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ==================== COMPONENT 3: OPEN SURAH ROW ITEM ====================
@Composable
private fun SurahRowItem(
    surah: SurahItem,
    onClick: () -> Unit,
    goldHighlight: Color,
    charcoalColor: Color,
    warmGrayColor: Color,
    outlineColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact Surah Number Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .border(0.5.dp, goldHighlight.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = "${surah.number}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = charcoalColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Surah Latin Name & Ayah Count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = charcoalColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${surah.revelation} · ${surah.numberOfVerses} Ayat",
                    style = MaterialTheme.typography.bodySmall,
                    color = warmGrayColor
                )
            }

            // Arabic Surah Name (Amiri font)
            Text(
                text = surah.asma,
                style = hujjahArabicTypography().preview.copy(
                    fontSize = 20.sp,
                    color = goldHighlight
                ),
                textAlign = TextAlign.End
            )
        }

        // Hairline Divider Between Rows
        HorizontalDivider(
            color = outlineColor.copy(alpha = 0.2f),
            thickness = 0.5.dp
        )
    }
}

// ==================== SUB-COMPONENT: HERO MOSQUE SILHOUETTE ====================
@Composable
private fun MosqueHeroSilhouette(
    goldHighlight: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val baseLine = h

        val path = Path().apply {
            moveTo(0f, baseLine)
            lineTo(w * 0.2f, baseLine)

            // Minaret
            lineTo(w * 0.2f, h * 0.40f)
            lineTo(w * 0.23f, h * 0.32f) // Spire peak
            lineTo(w * 0.26f, h * 0.40f)
            lineTo(w * 0.26f, baseLine)

            // Side Dome
            lineTo(w * 0.38f, baseLine)
            cubicTo(w * 0.38f, h * 0.55f, w * 0.58f, h * 0.55f, w * 0.58f, baseLine)

            // Main Central Dome
            lineTo(w * 0.60f, baseLine)
            cubicTo(w * 0.60f, h * 0.25f, w * 0.90f, h * 0.25f, w * 0.90f, baseLine)

            // Central Finial Spire
            moveTo(w * 0.75f, h * 0.10f)
            lineTo(w * 0.75f, h * 0.25f)

            lineTo(w, baseLine)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = path,
            color = goldHighlight.copy(alpha = 0.10f)
        )
    }
}
