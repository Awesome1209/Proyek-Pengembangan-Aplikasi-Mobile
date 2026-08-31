package com.example.hujjah.presentation.screens.hadith

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.hujjah.domain.repository.hujjah.TilawahRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hujjah.data.local.datastore.UserPreferences
import com.example.hujjah.domain.model.islamic.HadithBookItem
import com.example.hujjah.domain.model.islamic.IslamicReference
import com.example.hujjah.domain.model.islamic.SourceType
import com.example.hujjah.domain.repository.hujjah.BookmarkRepository
import com.example.hujjah.presentation.components.hujjah.HujjahEmptyState
import com.example.hujjah.presentation.components.hujjah.HujjahErrorState
import com.example.hujjah.presentation.components.hujjah.HujjahMenuItem
import com.example.hujjah.presentation.components.hujjah.HujjahSprint2MenuBar
import com.example.hujjah.presentation.components.hujjah.ShimmerHadithItem
import com.example.hujjah.presentation.components.hujjah.shimmerBrush
import com.example.hujjah.presentation.theme.LocalHujjahColors
import com.example.hujjah.presentation.theme.customShapes
import com.example.hujjah.presentation.theme.hujjahArabicTypography
import com.example.hujjah.presentation.theme.spacing
import kotlin.math.PI
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLens: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToHadith: () -> Unit,
    onNavigateToKoleksi: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    viewModel: HadithViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHujjahColors.current
    val coroutineScope = rememberCoroutineScope()

    val userPreferences = koinInject<UserPreferences>()
    val bookmarkRepository = koinInject<BookmarkRepository>()
    val tilawahRepository = koinInject<TilawahRepository>()
    val arabicFontSize by userPreferences.arabicFontSize.collectAsStateWithLifecycle(initialValue = 22)

    val isViewingBook = uiState.currentBookId != null

    var activeHadithSeconds by remember { mutableStateOf(0) }
    var maxSeenHadithIndex by remember { mutableStateOf(0) }

    LaunchedEffect(isViewingBook, uiState.currentBookId, uiState.hadiths.size) {
        if (isViewingBook && uiState.hadiths.isNotEmpty()) {
            if (maxSeenHadithIndex == 0) {
                maxSeenHadithIndex = 1
                userPreferences.addHadithsRead(1)
            }
        } else {
            maxSeenHadithIndex = 0
        }
    }

    LaunchedEffect(isViewingBook, uiState.currentBookId) {
        if (isViewingBook) {
            activeHadithSeconds = 0
            userPreferences.updateStreak()
            val sessionStartMillis = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            val dateStr = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toString()

            while (true) {
                delay(1000)
                activeHadithSeconds++
                userPreferences.addReadingDuration(1)

                if (activeHadithSeconds >= 3 && activeHadithSeconds % 3 == 0) {
                    val count = uiState.hadiths.size.coerceAtLeast(1)
                    val bookName = uiState.currentBookName.orEmpty()
                    if (bookName.isNotBlank()) {
                        tilawahRepository.saveSessionLog(
                            com.example.hujjah.domain.model.islamic.TilawahSessionLog(
                                id = "hadith-${uiState.currentBookId}-$sessionStartMillis",
                                timestamp = sessionStartMillis,
                                sourceType = com.example.hujjah.domain.model.islamic.TilawahSourceType.HADITH,
                                title = "Hadits $bookName",
                                durationSeconds = activeHadithSeconds.toLong(),
                                itemsReadCount = count,
                                dateString = dateStr
                            )
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(isViewingBook) {
        onDispose {
            if (activeHadithSeconds >= 3 && !uiState.currentBookName.isNullOrBlank()) {
                val sessionStartMillis = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                val dateStr = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toString()
                val count = uiState.hadiths.size.coerceAtLeast(1)
                val bookName = uiState.currentBookName.orEmpty()
                coroutineScope.launch {
                    tilawahRepository.saveSessionLog(
                        com.example.hujjah.domain.model.islamic.TilawahSessionLog(
                            id = "hadith-${uiState.currentBookId}-$sessionStartMillis",
                            timestamp = sessionStartMillis,
                            sourceType = com.example.hujjah.domain.model.islamic.TilawahSourceType.HADITH,
                            title = "Hadits $bookName",
                            durationSeconds = activeHadithSeconds.toLong(),
                            itemsReadCount = count,
                            dateString = dateStr
                        )
                    )
                }
            }
        }
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
                        text = if (isViewingBook) uiState.currentBookName.orEmpty() else "Hadits Ensiklopedia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = charcoalColor
                    )
                },
                navigationIcon = {
                    if (isViewingBook) {
                        IconButton(onClick = { viewModel.selectBook("", "") }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Kembali",
                                tint = charcoalColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = warmIvoryColor
                )
            )
        },
        bottomBar = {
            HujjahSprint2MenuBar(
                currentItem = HujjahMenuItem.HADITH,
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
            if (!isViewingBook) {
                // ==================== 1. GRID VIEW OF 9 NARRATOR BOOKS ====================
                Column(modifier = Modifier.fillMaxSize()) {
                    HorizontalDivider(
                        color = colors.goldHighlight.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MaterialTheme.spacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "9 Kitab Perawi Hadits",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = charcoalColor
                        )
                        Text(
                            text = "Ensiklopedia Hadits",
                            style = MaterialTheme.typography.labelSmall,
                            color = warmGrayColor
                        )
                    }

                    if (uiState.isLoading) {
                        val brush = shimmerBrush()
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(6) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(145.dp)
                                        .background(brush, shape = MaterialTheme.customShapes.large)
                                )
                            }
                        }
                    } else if (uiState.error != null) {
                        HujjahErrorState(
                            message = uiState.error ?: "Gagal memuat kitab hadits",
                            onRetry = { viewModel.selectBook("", "") }
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            itemsIndexed(uiState.books, key = { _, book -> book.id }) { index, book ->
                                AnimatedNarratorBookCard(
                                    book = book,
                                    index = index,
                                    onClick = { viewModel.selectBook(book.id, book.name) },
                                    goldHighlight = colors.goldHighlight,
                                    charcoalColor = charcoalColor,
                                    warmGrayColor = warmGrayColor,
                                    surfaceColor = surfaceColor
                                )
                            }
                        }
                    }
                }
            } else {
                // ==================== 2. PAGINATED HADITH LIST VIEW ====================
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MaterialTheme.spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            placeholder = {
                                Text(
                                    text = "Cari Nomor Hadits...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = warmGrayColor
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = { viewModel.performSearch() }
                            ),
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus Pencarian",
                                            tint = warmGrayColor
                                        )
                                    }
                                }
                            },
                            shape = MaterialTheme.customShapes.pill,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.goldHighlight,
                                unfocusedBorderColor = outlineColor.copy(alpha = 0.3f),
                                focusedContainerColor = surfaceColor,
                                unfocusedContainerColor = surfaceColor
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.performSearch() },
                            colors = ButtonDefaults.buttonColors(containerColor = charcoalColor),
                            shape = MaterialTheme.customShapes.pill,
                            modifier = Modifier.height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari",
                                tint = colors.goldHighlight
                            )
                        }
                    }

                    if (uiState.isLoading) {
                        val brush = shimmerBrush()
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(4) {
                                ShimmerHadithItem(brush = brush)
                            }
                        }
                    } else if (uiState.error != null && uiState.hadiths.isEmpty()) {
                        HujjahErrorState(
                            message = uiState.error ?: "Terjadi kesalahan",
                            onRetry = { viewModel.performSearch() }
                        )
                    } else if (uiState.hadiths.isEmpty()) {
                        HujjahEmptyState(
                            title = "Hadits Tidak Ditemukan",
                            message = "Tidak ada nomor hadits yang cocok dengan pencarian \"${uiState.searchQuery}\" di kitab ini."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(uiState.hadiths, key = { it.number }) { hadith ->
                                val referenceId = "hadith_${uiState.currentBookId}_${hadith.number}"
                                var isBookmarked by remember { mutableStateOf<Boolean?>(null) }

                                LaunchedEffect(referenceId) {
                                    val bookmark = bookmarkRepository.getBookmarkByReferenceId(referenceId).firstOrNull()
                                    isBookmarked = bookmark != null
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.customShapes.large,
                                    colors = CardDefaults.cardColors(
                                        containerColor = surfaceColor
                                    ),
                                    border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.2f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = colors.goldHighlight.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "No. ${hadith.number}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.goldHighlight,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        if (isBookmarked == true) {
                                                            bookmarkRepository.deleteBookmark(referenceId)
                                                            isBookmarked = false
                                                        } else {
                                                            val ref = IslamicReference(
                                                                id = referenceId,
                                                                sourceType = SourceType.HADITH,
                                                                title = "Hadits Perawi ${uiState.currentBookName}: No. ${hadith.number}",
                                                                sourceName = "${uiState.currentBookName} No. ${hadith.number}",
                                                                arabicText = hadith.arab,
                                                                translation = hadith.translation,
                                                                explanation = "",
                                                                topicId = "hadith",
                                                                topicTitle = "Ensiklopedia Hadits"
                                                            )
                                                            bookmarkRepository.saveBookmark(ref, "")
                                                            isBookmarked = true
                                                        }
                                                    }
                                                },
                                                border = BorderStroke(
                                                    0.5.dp,
                                                    if (isBookmarked == true) colors.goldHighlight else outlineColor.copy(alpha = 0.3f)
                                                ),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isBookmarked == true) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                                    contentDescription = "Simpan ke Khazanah Dalil",
                                                    tint = if (isBookmarked == true) colors.goldHighlight else warmGrayColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isBookmarked == true) "Tersimpan" else "Simpan Dalil",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isBookmarked == true) colors.goldHighlight else warmGrayColor
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = hadith.arab,
                                            style = hujjahArabicTypography().preview.copy(
                                                fontSize = arabicFontSize.sp,
                                                lineHeight = (arabicFontSize * 1.7).sp
                                            ),
                                            textAlign = TextAlign.End,
                                            color = charcoalColor,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "\"${hadith.translation}\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontStyle = FontStyle.Italic,
                                            color = warmGrayColor,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== ANIMATED NARRATOR BOOK CARD ====================
@Composable
private fun AnimatedNarratorBookCard(
    book: HadithBookItem,
    index: Int,
    onClick: () -> Unit,
    goldHighlight: Color,
    charcoalColor: Color,
    warmGrayColor: Color,
    surfaceColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.customShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        border = BorderStroke(1.dp, goldHighlight.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Ambient Canvas Islamic Ornament Graphic Animation mapped 100% by book.id!
            BookAmbientAnimationCanvas(
                bookId = book.id,
                goldHighlight = goldHighlight,
                modifier = Modifier.fillMaxSize()
            )

            // Content Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MaterialTheme.spacing.large),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Number Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(goldHighlight.copy(alpha = 0.12f))
                        .border(0.5.dp, goldHighlight.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = charcoalColor
                    )
                }

                Column {
                    Text(
                        text = book.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = charcoalColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${book.totalHadith} Hadits",
                        style = MaterialTheme.typography.bodySmall,
                        color = warmGrayColor
                    )
                }
            }
        }
    }
}

// ==================== 9 DISTINCT NEAT ISLAMIC GEOMETRIC ORNAMENT MOTIONS BY BOOK ID ====================
@Composable
private fun BookAmbientAnimationCanvas(
    bookId: String,
    goldHighlight: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val id = bookId.lowercase()

    when {
        // 1. Shahih Bukhari ("bukhari") — KAABA SILHOUETTE & EXPANDING RADIANT TAWAF RINGS
        id.contains("bukhari") -> {
            val waveScale by infiniteTransition.animateFloat(
                initialValue = 0.5f, targetValue = 1.7f,
                animationSpec = infiniteRepeatable(animation = tween(2800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart)
            )
            val waveAlpha by infiniteTransition.animateFloat(
                initialValue = 0.25f, targetValue = 0f,
                animationSpec = infiniteRepeatable(animation = tween(2800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart)
            )
            val haloPulse by infiniteTransition.animateFloat(
                initialValue = 0.85f, targetValue = 1.15f,
                animationSpec = infiniteRepeatable(animation = tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )

            Canvas(modifier = modifier) {
                val cx = size.width * 0.80f
                val cy = size.height * 0.35f
                val kw = 16.dp.toPx()
                val kh = 20.dp.toPx()

                // Expanding Tawaf Waves
                drawCircle(
                    color = goldHighlight.copy(alpha = waveAlpha),
                    radius = kw * 1.8f * waveScale,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Kaaba Structure Aura Halo
                drawRect(
                    color = goldHighlight.copy(alpha = 0.08f * haloPulse),
                    topLeft = Offset(cx - kw * 1.3f * haloPulse, cy - kh * 1.3f * haloPulse),
                    size = Size(kw * 2.6f * haloPulse, kh * 2.6f * haloPulse)
                )

                // Kaaba Cubic Silhouette
                drawRect(
                    color = goldHighlight.copy(alpha = 0.18f),
                    topLeft = Offset(cx - kw, cy - kh),
                    size = Size(kw * 2, kh * 2),
                    style = Stroke(width = 1.6.dp.toPx())
                )

                // Golden Kiswah Trim Line
                drawLine(
                    color = goldHighlight.copy(alpha = 0.35f),
                    start = Offset(cx - kw, cy - kh * 0.4f),
                    end = Offset(cx + kw, cy - kh * 0.4f),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // 2. Shahih Muslim ("muslim") — ILLUMINATED AL-QURAN BOOK STAND (REHAL) & NOOR STAR
        id.contains("muslim") -> {
            val rehalPulse by infiniteTransition.animateFloat(
                initialValue = 0.90f, targetValue = 1.10f,
                animationSpec = infiniteRepeatable(animation = tween(2200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )
            val noorGlow by infiniteTransition.animateFloat(
                initialValue = 0.12f, targetValue = 0.32f,
                animationSpec = infiniteRepeatable(animation = tween(1800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )

            Canvas(modifier = modifier) {
                val cx = size.width * 0.80f
                val cy = size.height * 0.38f
                val rw = 18.dp.toPx() * rehalPulse
                val rh = 14.dp.toPx() * rehalPulse

                // Crossed X-Frame Rehal Book Stand
                drawLine(color = goldHighlight.copy(alpha = 0.18f), start = Offset(cx - rw, cy + rh), end = Offset(cx + rw, cy - rh * 0.5f), strokeWidth = 1.5.dp.toPx())
                drawLine(color = goldHighlight.copy(alpha = 0.18f), start = Offset(cx + rw, cy + rh), end = Offset(cx - rw, cy - rh * 0.5f), strokeWidth = 1.5.dp.toPx())

                // Open Manuscript Book Pages Contour
                val bookPath = Path().apply {
                    moveTo(cx - rw, cy - rh * 0.5f)
                    cubicTo(cx - rw * 0.5f, cy - rh * 0.8f, cx, cy - rh * 0.4f, cx, cy - rh * 0.4f)
                    cubicTo(cx, cy - rh * 0.4f, cx + rw * 0.5f, cy - rh * 0.8f, cx + rw, cy - rh * 0.5f)
                    lineTo(cx + rw * 0.9f, cy - rh * 0.1f)
                    cubicTo(cx + rw * 0.4f, cy - rh * 0.3f, cx, cy, cx, cy)
                    cubicTo(cx, cy, cx - rw * 0.4f, cy - rh * 0.3f, cx - rw * 0.9f, cy - rh * 0.1f)
                    close()
                }
                drawPath(path = bookPath, color = goldHighlight.copy(alpha = 0.22f), style = Stroke(width = 1.4.dp.toPx()))

                // Glowing Noor Star Above Rehal
                drawCircle(color = goldHighlight.copy(alpha = noorGlow), radius = 4.dp.toPx(), center = Offset(cx, cy - rh * 1.4f))
            }
        }

        // 3. Sunan Abu Daud ("abu-daud" / "daud") — SUSPENDED FILIGREE FANOUS LANTERN & BREATHING GOLD RADIANCE
        id.contains("daud") -> {
            val swayAngle by infiniteTransition.animateFloat(
                initialValue = -5f, targetValue = 5f,
                animationSpec = infiniteRepeatable(animation = tween(3200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )
            val auraPulse by infiniteTransition.animateFloat(
                initialValue = 0.85f, targetValue = 1.20f,
                animationSpec = infiniteRepeatable(animation = tween(2200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )
            val auraAlpha by infiniteTransition.animateFloat(
                initialValue = 0.06f, targetValue = 0.22f,
                animationSpec = infiniteRepeatable(animation = tween(2200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )

            Canvas(modifier = modifier) {
                val cx = size.width * 0.82f
                val cy = size.height * 0.35f
                val lw = 14.dp.toPx()
                val lh = 22.dp.toPx()

                withTransform({ rotate(swayAngle, Offset(cx, 0f)) }) {
                    drawLine(
                        color = goldHighlight.copy(alpha = 0.18f),
                        start = Offset(cx, 0f),
                        end = Offset(cx, cy - lh),
                        strokeWidth = 1.2.dp.toPx()
                    )

                    drawCircle(
                        color = goldHighlight.copy(alpha = auraAlpha),
                        radius = 28.dp.toPx() * auraPulse,
                        center = Offset(cx, cy)
                    )

                    val lanternPath = Path().apply {
                        moveTo(cx, cy - lh)
                        lineTo(cx - lw * 0.6f, cy - lh * 0.5f)
                        lineTo(cx + lw * 0.6f, cy - lh * 0.5f)
                        close()

                        moveTo(cx - lw * 0.6f, cy - lh * 0.5f)
                        lineTo(cx - lw, cy)
                        lineTo(cx - lw * 0.6f, cy + lh * 0.6f)
                        lineTo(cx + lw * 0.6f, cy + lh * 0.6f)
                        lineTo(cx + lw, cy)
                        lineTo(cx + lw * 0.6f, cy - lh * 0.5f)
                        close()

                        moveTo(cx - lw * 0.3f, cy + lh * 0.6f)
                        lineTo(cx, cy + lh * 0.95f)
                        lineTo(cx + lw * 0.3f, cy + lh * 0.6f)
                        close()
                    }

                    drawPath(
                        path = lanternPath,
                        color = goldHighlight.copy(alpha = 0.20f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    drawCircle(
                        color = goldHighlight.copy(alpha = 0.28f * auraPulse),
                        radius = 4.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }
        }

        // 4. Sunan Tirmidzi ("tirmidzi") — ROYAL OTTOMAN MOSQUE DOME & CRESCENT SPIRE
        id.contains("tirmidzi") -> {
            val domePulse by infiniteTransition.animateFloat(
                initialValue = 0.92f, targetValue = 1.08f,
                animationSpec = infiniteRepeatable(animation = tween(2200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )
            val haloAlpha by infiniteTransition.animateFloat(
                initialValue = 0.08f, targetValue = 0.24f,
                animationSpec = infiniteRepeatable(animation = tween(2200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )

            Canvas(modifier = modifier) {
                val cx = size.width * 0.80f
                val cy = size.height * 0.40f
                val dw = 22.dp.toPx() * domePulse
                val dh = 26.dp.toPx() * domePulse

                drawCircle(color = goldHighlight.copy(alpha = haloAlpha), radius = 24.dp.toPx() * domePulse, center = Offset(cx, cy - dh * 0.8f))

                val domePath = Path().apply {
                    moveTo(cx - dw, cy + dh * 0.4f)
                    lineTo(cx - dw, cy)
                    cubicTo(cx - dw, cy - dh * 0.7f, cx - dw * 0.4f, cy - dh, cx, cy - dh * 1.15f)
                    cubicTo(cx + dw * 0.4f, cy - dh, cx + dw, cy - dh * 0.7f, cx + dw, cy)
                    lineTo(cx + dw, cy + dh * 0.4f)
                }
                drawPath(path = domePath, color = goldHighlight.copy(alpha = 0.18f), style = Stroke(width = 1.5.dp.toPx()))

                drawCircle(color = goldHighlight.copy(alpha = 0.30f), radius = 3.dp.toPx(), center = Offset(cx, cy - dh * 1.15f))
            }
        }

        // 5. Sunan An-Nasa'i ("nasai") — ELEGANT TASBIH PRAYER BEADS & REVOLVING HILAL ARC
        id.contains("nasai") -> {
            val tasbihRotate by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(animation = tween(10000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
            )
            val hilalAlpha by infiniteTransition.animateFloat(
                initialValue = 0.10f, targetValue = 0.26f,
                animationSpec = infiniteRepeatable(animation = tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )

            Canvas(modifier = modifier) {
                val cx = size.width * 0.80f
                val cy = size.height * 0.35f
                val trackRadius = 24.dp.toPx()

                drawCircle(
                    color = goldHighlight.copy(alpha = 0.10f),
                    radius = trackRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx())
                )

                withTransform({ rotate(tasbihRotate, Offset(cx, cy)) }) {
                    for (i in 0 until 12) {
                        val angleRad = (i * 30) * (PI.toFloat() / 180f)
                        val bx = cx + trackRadius * kotlin.math.cos(angleRad)
                        val by = cy + trackRadius * kotlin.math.sin(angleRad)
                        drawCircle(
                            color = goldHighlight.copy(alpha = 0.22f),
                            radius = 2.5.dp.toPx(),
                            center = Offset(bx, by)
                        )
                    }
                }

                val mr = 9.dp.toPx()
                val crescent = Path().apply {
                    moveTo(cx, cy - mr)
                    cubicTo(cx - mr * 1.3f, cy - mr, cx - mr * 1.3f, cy + mr, cx, cy + mr)
                    cubicTo(cx - mr * 0.5f, cy + mr, cx - mr * 0.5f, cy - mr, cx, cy - mr)
                    close()
                }
                drawPath(path = crescent, color = goldHighlight.copy(alpha = hilalAlpha))
                drawCircle(color = goldHighlight.copy(alpha = hilalAlpha), radius = 2.5.dp.toPx(), center = Offset(cx + 4.dp.toPx(), cy - 2.dp.toPx()))
            }
        }

        // 6. Sunan Ibnu Majah ("ibnu-majah" / "majah") — INTRICATE PERSIAN ISLIMI ARABESQUE FLORAL LATTICE
        id.contains("majah") -> {
            val rot1 by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(animation = tween(14000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
            )
            val rot2 by infiniteTransition.animateFloat(
                initialValue = 360f, targetValue = 0f,
                animationSpec = infiniteRepeatable(animation = tween(14000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
            )

            Canvas(modifier = modifier) {
                val cx = size.width * 0.80f
                val cy = size.height * 0.32f
                val pr = 24.dp.toPx()

                withTransform({ rotate(rot1, Offset(cx, cy)) }) {
                    for (deg in listOf(0f, 45f, 90f, 135f)) {
                        withTransform({ rotate(deg, Offset(cx, cy)) }) {
                            val petalPath = Path().apply {
                                moveTo(cx, cy - pr)
                                cubicTo(cx + pr * 0.6f, cy - pr * 0.6f, cx + pr * 0.6f, cy + pr * 0.6f, cx, cy + pr)
                                cubicTo(cx - pr * 0.6f, cy + pr * 0.6f, cx - pr * 0.6f, cy - pr * 0.6f, cx, cy - pr)
                                close()
                            }
                            drawPath(path = petalPath, color = goldHighlight.copy(alpha = 0.11f), style = Stroke(width = 1.3.dp.toPx()))
                        }
                    }
                }

                val pir = 14.dp.toPx()
                withTransform({ rotate(rot2, Offset(cx, cy)) }) {
                    for (deg in listOf(22.5f, 67.5f)) {
                        withTransform({ rotate(deg, Offset(cx, cy)) }) {
                            val innerPetal = Path().apply {
                                moveTo(cx, cy - pir)
                                cubicTo(cx + pir * 0.5f, cy - pir * 0.5f, cx + pir * 0.5f, cy + pir * 0.5f, cx, cy + pir)
                                cubicTo(cx - pir * 0.5f, cy + pir * 0.5f, cx - pir * 0.5f, cy - pir * 0.5f, cx, cy - pir)
                                close()
                            }
                            drawPath(path = innerPetal, color = goldHighlight.copy(alpha = 0.15f), style = Stroke(width = 1.2.dp.toPx()))
                        }
                    }
                }

                drawCircle(color = goldHighlight.copy(alpha = 0.25f), radius = 4.dp.toPx(), center = Offset(cx, cy))
            }
        }

        // 7. Musnad Ahmad ("ahmad") — DUAL COUNTER-ROTATING 8-FOLD GIRIH STAR LATTICE
        id.contains("ahmad") -> {
            val rRot1 by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(animation = tween(8000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
            )
            val rRot2 by infiniteTransition.animateFloat(
                initialValue = 360f, targetValue = 0f,
                animationSpec = infiniteRepeatable(animation = tween(8000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
            )

            Canvas(modifier = modifier) {
                val cx = size.width * 0.80f
                val cy = size.height * 0.35f
                val r1 = 24.dp.toPx()
                val r2 = 14.dp.toPx()

                withTransform({ rotate(rRot1, Offset(cx, cy)) }) {
                    drawRect(color = goldHighlight.copy(alpha = 0.12f), topLeft = Offset(cx - r1, cy - r1), size = Size(r1 * 2, r1 * 2), style = Stroke(width = 1.5.dp.toPx()))
                }
                withTransform({ rotate(rRot2, Offset(cx, cy)) }) {
                    drawRect(color = goldHighlight.copy(alpha = 0.10f), topLeft = Offset(cx - r2, cy - r2), size = Size(r2 * 2, r2 * 2), style = Stroke(width = 1.2.dp.toPx()))
                }
            }
        }

        // 8. Muwatta Malik ("malik") — ANDALUSIAN HORSESHOE ARCH & 16-RAY SUNBURST STAR
        id.contains("malik") -> {
            val rotSunburst by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(animation = tween(16000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
            )
            val archPulse by infiniteTransition.animateFloat(
                initialValue = 0.92f, targetValue = 1.08f,
                animationSpec = infiniteRepeatable(animation = tween(2200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )

            Canvas(modifier = modifier) {
                val cx = size.width * 0.80f
                val cy = size.height * 0.35f
                val aw = 20.dp.toPx() * archPulse
                val ah = 25.dp.toPx() * archPulse

                val horseshoeArch = Path().apply {
                    moveTo(cx - aw, cy + ah)
                    lineTo(cx - aw, cy + ah * 0.2f)
                    cubicTo(cx - aw * 1.25f, cy - ah * 0.3f, cx - aw * 0.5f, cy - ah * 1.1f, cx, cy - ah * 1.15f)
                    cubicTo(cx + aw * 0.5f, cy - ah * 1.1f, cx + aw * 1.25f, cy - ah * 0.3f, cx + aw, cy + ah * 0.2f)
                    lineTo(cx + aw, cy + ah)
                }
                drawPath(path = horseshoeArch, color = goldHighlight.copy(alpha = 0.16f), style = Stroke(width = 1.5.dp.toPx()))

                val sr = 12.dp.toPx()
                withTransform({ rotate(rotSunburst, Offset(cx, cy - ah * 0.1f)) }) {
                    for (deg in listOf(0f, 22.5f, 45f, 67.5f)) {
                        withTransform({ rotate(deg, Offset(cx, cy - ah * 0.1f)) }) {
                            drawLine(
                                color = goldHighlight.copy(alpha = 0.20f),
                                start = Offset(cx - sr, cy - ah * 0.1f),
                                end = Offset(cx + sr, cy - ah * 0.1f),
                                strokeWidth = 1.2.dp.toPx()
                            )
                        }
                    }
                }
            }
        }

        // 9. Sunan Darimi ("darimi") — MASHRABIYA / JALI GEOMETRIC WINDOW LATTICE
        id.contains("darimi") -> {
            val shimmerOffset by infiniteTransition.animateFloat(
                initialValue = -0.6f, targetValue = 1.6f,
                animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
            )
            val starPulse by infiniteTransition.animateFloat(
                initialValue = 0.85f, targetValue = 1.15f,
                animationSpec = infiniteRepeatable(animation = tween(1800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
            )

            Canvas(modifier = modifier) {
                val cx = size.width * 0.80f
                val cy = size.height * 0.32f
                val gw = 22.dp.toPx()

                for (offset in listOf(-gw, -gw * 0.5f, 0f, gw * 0.5f, gw)) {
                    drawLine(color = goldHighlight.copy(alpha = 0.10f), start = Offset(cx + offset, cy - gw), end = Offset(cx + offset, cy + gw), strokeWidth = 1.dp.toPx())
                    drawLine(color = goldHighlight.copy(alpha = 0.10f), start = Offset(cx - gw, cy + offset), end = Offset(cx + gw, cy + offset), strokeWidth = 1.dp.toPx())
                }

                val brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, goldHighlight.copy(alpha = 0.16f), Color.Transparent),
                    start = Offset(size.width * shimmerOffset, 0f),
                    end = Offset(size.width * (shimmerOffset + 0.35f), size.height)
                )
                drawRect(brush = brush)

                val sr = 8.dp.toPx() * starPulse
                withTransform({ rotate(45f, Offset(cx, cy)) }) {
                    drawRect(color = goldHighlight.copy(alpha = 0.22f), topLeft = Offset(cx - sr, cy - sr), size = Size(sr * 2, sr * 2), style = Stroke(width = 1.4.dp.toPx()))
                }
            }
        }

        else -> {
            // Default Fallback
            val rot by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(animation = tween(12000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
            )
            Canvas(modifier = modifier) {
                val cx = size.width * 0.80f
                val cy = size.height * 0.32f
                val r = 20.dp.toPx()
                withTransform({ rotate(rot, Offset(cx, cy)) }) {
                    drawRect(color = goldHighlight.copy(alpha = 0.12f), topLeft = Offset(cx - r, cy - r), size = Size(r * 2, r * 2), style = Stroke(width = 1.2.dp.toPx()))
                }
            }
        }
    }
}
