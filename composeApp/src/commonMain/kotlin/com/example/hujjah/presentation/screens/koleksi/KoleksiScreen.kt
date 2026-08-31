package com.example.hujjah.presentation.screens.koleksi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hujjah.data.sample.SampleIslamicReferences
import com.example.hujjah.domain.model.islamic.TopicOption
import com.example.hujjah.presentation.components.hujjah.HujjahMenuItem
import com.example.hujjah.presentation.components.hujjah.HujjahSprint2MenuBar
import com.example.hujjah.presentation.theme.LocalHujjahColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KoleksiScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLens: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToHadith: () -> Unit,
    onNavigateToKoleksi: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToTilawahHistory: () -> Unit = {},
    onNavigateToResult: (String) -> Unit
) {
    val colors = LocalHujjahColors.current
    var isKhazanahExpanded by remember { mutableStateOf(false) }
    val topics = remember { SampleIslamicReferences.topics }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Koleksi Saya",
                            fontWeight = FontWeight.Bold,
                            color = colors.goldHighlight
                        )
                        Text(
                            text = "Simpan, baca kembali, dan lanjutkan refleksimu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            HujjahSprint2MenuBar(
                currentItem = HujjahMenuItem.KOLEKSI,
                onNavigateToHome = onNavigateToHome,
                onNavigateToLens = onNavigateToLens,
                onNavigateToQuran = onNavigateToQuran,
                onNavigateToHadith = onNavigateToHadith,
                onNavigateToKoleksi = onNavigateToKoleksi
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==================== 1. JURNAL & KALENDER TILAWAH ====================
            item {
                KoleksiHubCard(
                    title = "Jurnal & Kalender Tilawah",
                    subtitle = "Statistik istiqamah, streak, dan riwayat mengaji",
                    icon = Icons.Outlined.Explore,
                    onClick = onNavigateToTilawahHistory,
                    goldHighlight = colors.goldHighlight
                )
            }

            // ==================== 2. DALIL TERSIMPAN ====================
            item {
                KoleksiHubCard(
                    title = "Dalil Tersimpan",
                    subtitle = "Khazanah ayat dan hadis yang sudah disimpan",
                    icon = Icons.Outlined.BookmarkBorder,
                    onClick = onNavigateToBookmarks,
                    goldHighlight = colors.goldHighlight
                )
            }

            // ==================== 2. KHAZANAH DALIL (TOPIK) ====================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, colors.goldHighlight.copy(alpha = 0.2f))
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isKhazanahExpanded = !isKhazanahExpanded }
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Explore,
                                    contentDescription = null,
                                    tint = colors.goldHighlight,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Khazanah Dalil",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (colors.isDarkTheme) MaterialTheme.colorScheme.onSurface else colors.islamicGreen
                                    )
                                    Text(
                                        text = "Eksplorasi dalil berdasarkan topik kehidupan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isKhazanahExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = colors.goldHighlight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        AnimatedVisibility(visible = isKhazanahExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                topics.chunked(2).forEach { rowTopics ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowTopics.forEach { topic ->
                                            TopicChipCard(
                                                topic = topic,
                                                onClick = { onNavigateToResult(topic.id) },
                                                modifier = Modifier.weight(1f),
                                                goldHighlight = colors.goldHighlight
                                            )
                                        }
                                        if (rowTopics.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // ==================== 3. CATATAN HARIAN ====================
            item {
                KoleksiHubCard(
                    title = "Catatan Harian Saya",
                    subtitle = "Jurnal spiritual dan catatan pribadi",
                    icon = Icons.Outlined.Description,
                    onClick = onNavigateToNotes,
                    goldHighlight = colors.goldHighlight
                )
            }

            // ==================== 4. RIWAYAT KONSELING LENS ====================
            item {
                KoleksiHubCard(
                    title = "Riwayat Konseling Lens",
                    subtitle = "Lanjutkan percakapan dengan AI spiritual counselor",
                    icon = Icons.Outlined.ChatBubbleOutline,
                    onClick = onNavigateToLens,
                    goldHighlight = colors.goldHighlight
                )
            }
        }
    }
}

@Composable
private fun KoleksiHubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    goldHighlight: androidx.compose.ui.graphics.Color
) {
    val colors = LocalHujjahColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, goldHighlight.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = goldHighlight,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (colors.isDarkTheme) MaterialTheme.colorScheme.onSurface else colors.islamicGreen
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TopicChipCard(
    topic: TopicOption,
    onClick: () -> Unit,
    goldHighlight: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val topicIconVector = when(topic.id) {
        "anger" -> Icons.Outlined.LocalFireDepartment
        "calm" -> Icons.Outlined.Spa
        "sabr" -> Icons.Outlined.SelfImprovement
        "taubah" -> Icons.Outlined.AutoAwesome
        "syukur" -> Icons.Outlined.WbSunny
        "shalat" -> Icons.Outlined.AccessTime
        "tawakkal_cemas" -> Icons.Outlined.Shield
        "ilmu" -> Icons.Outlined.MenuBook
        "parents" -> Icons.Outlined.Home
        "rezeki" -> Icons.Outlined.AccountBalanceWallet
        else -> Icons.Outlined.AutoAwesome
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, goldHighlight.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = topicIconVector,
                contentDescription = topic.title,
                tint = goldHighlight,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = topic.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}
