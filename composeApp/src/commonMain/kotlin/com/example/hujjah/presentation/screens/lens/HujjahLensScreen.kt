package com.example.hujjah.presentation.screens.lens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hujjah.domain.model.islamic.ChatMessage
import com.example.hujjah.domain.model.islamic.IslamicReference
import com.example.hujjah.domain.model.islamic.Sender
import com.example.hujjah.presentation.components.hujjah.HujjahMenuItem
import com.example.hujjah.presentation.components.hujjah.HujjahSprint2MenuBar
import com.example.hujjah.presentation.theme.LocalHujjahColors
import com.example.hujjah.presentation.theme.customShapes
import com.example.hujjah.presentation.theme.hujjahArabicTypography
import com.example.hujjah.presentation.theme.spacing
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HujjahLensScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLens: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToQuranDetail: (surahNumber: Int, surahName: String, verseNumber: Int?) -> Unit,
    onNavigateToHadith: () -> Unit,
    onNavigateToKoleksi: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToResult: (String) -> Unit,
    onNavigateToAddNote: (Long?, String?) -> Unit,
    viewModel: HujjahLensViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHujjahColors.current
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    var selectedMessageForAction by remember { mutableStateOf<ChatMessage?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }

    val charcoalColor = MaterialTheme.colorScheme.onBackground
    val warmGrayColor = MaterialTheme.colorScheme.onSurfaceVariant
    val warmIvoryColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = warmIvoryColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Hujjah Lens",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = charcoalColor
                            )
                            Text(
                                text = "AI Spiritual Counselor",
                                style = MaterialTheme.typography.bodySmall,
                                color = warmGrayColor
                            )
                        }
                    }
                },
                actions = {
                    LensHeaderOrnament(
                        goldHighlight = colors.goldHighlight,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = warmIvoryColor
                )
            )
        },
        bottomBar = {
            HujjahSprint2MenuBar(
                currentItem = HujjahMenuItem.LENS,
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
        ) {
            // Header Subtle Ornamental Underline
            HorizontalDivider(
                color = colors.goldHighlight.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )

            if (uiState.isOffline) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                    shape = MaterialTheme.customShapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "☁️ Mode Luring: Anda hanya dapat membaca riwayat chat.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ==================== CHAT HISTORY AREA ====================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Low-opacity Background Watermark Ornament
                LensChatBackgroundOrnament(
                    goldHighlight = colors.goldHighlight,
                    modifier = Modifier.fillMaxSize()
                )

                if (uiState.messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.goldHighlight)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = MaterialTheme.spacing.large),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            ChatBubble(
                                message = message,
                                colors = colors,
                                charcoalColor = charcoalColor,
                                warmGrayColor = warmGrayColor,
                                surfaceColor = surfaceColor,
                                onLongPressMessage = {
                                    selectedMessageForAction = message
                                    showActionDialog = true
                                },
                                onClickReference = { ref ->
                                    if (ref.sourceType == com.example.hujjah.domain.model.islamic.SourceType.QURAN && ref.surahNumber != null) {
                                        onNavigateToQuranDetail(
                                            ref.surahNumber,
                                            ref.sourceName.substringBefore(":").trim(),
                                            ref.verseNumber
                                        )
                                    }
                                }
                            )
                        }

                        if (uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = surfaceColor
                                        ),
                                        border = BorderStroke(0.5.dp, colors.goldHighlight.copy(alpha = 0.3f)),
                                        modifier = Modifier.padding(end = 40.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = colors.goldHighlight
                                            )
                                            Text(
                                                text = "Hujjah Lens sedang merenung...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = warmGrayColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==================== BOTTOM PANEL (INPUT COMPOSER) ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(warmIvoryColor)
                    .padding(horizontal = MaterialTheme.spacing.large, vertical = 8.dp)
            ) {
                Surface(
                    shape = MaterialTheme.customShapes.pill,
                    color = surfaceColor,
                    border = BorderStroke(1.dp, colors.goldHighlight.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = viewModel::onInputTextChanged,
                            placeholder = {
                                Text(
                                    if (uiState.isOffline) "Mode luring" else "Curhat spiritual di sini...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = warmGrayColor
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            shape = CircleShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            maxLines = 3,
                            singleLine = false,
                            enabled = !uiState.isOffline
                        )

                        IconButton(
                            onClick = {
                                if (uiState.inputText.isNotBlank()) {
                                    viewModel.sendUserMessage(uiState.inputText)
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (uiState.inputText.isBlank() || uiState.isOffline) warmGrayColor.copy(alpha = 0.2f) else charcoalColor),
                            enabled = uiState.inputText.isNotBlank() && !uiState.isLoading && !uiState.isOffline
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Kirim",
                                tint = if (uiState.inputText.isBlank() || uiState.isOffline) warmGrayColor else colors.goldHighlight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // ==================== ACTIONS DIALOG (Long press popup) ====================
    if (showActionDialog && selectedMessageForAction != null) {
        AlertDialog(
            onDismissRequest = {
                showActionDialog = false
            },
            title = {
                Text(
                    text = "Opsi Pesan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.goldHighlight
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Pilih tindakan untuk pesan ini:",
                        style = MaterialTheme.typography.bodySmall,
                        color = warmGrayColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Opsi Simpan ke Catatan
                    Surface(
                        onClick = {
                            showActionDialog = false
                            onNavigateToAddNote(null, selectedMessageForAction!!.text)
                        },
                        shape = MaterialTheme.customShapes.medium,
                        color = colors.goldHighlight.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, colors.goldHighlight.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = colors.goldHighlight
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Simpan ke Catatan Saya",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = charcoalColor
                            )
                        }
                    }

                    // Opsi Hapus Pesan
                    Surface(
                        onClick = {
                            viewModel.deleteMessage(selectedMessageForAction!!.id)
                            showActionDialog = false
                        },
                        shape = MaterialTheme.customShapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Hapus Pesan",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showActionDialog = false }) {
                    Text("Batal", color = warmGrayColor)
                }
            }
        )
    }
}

// ==================== CHAT BUBBLE ====================
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubble(
    message: ChatMessage,
    colors: com.example.hujjah.presentation.theme.HujjahColors,
    charcoalColor: Color,
    warmGrayColor: Color,
    surfaceColor: Color,
    onLongPressMessage: () -> Unit,
    onClickReference: (IslamicReference) -> Unit
) {
    val isUser = message.sender == Sender.USER

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            // Main Bubble Container
            Card(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isUser) 20.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 20.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) {
                        if (colors.isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else charcoalColor
                    } else {
                        if (colors.isDarkTheme) MaterialTheme.colorScheme.surface else surfaceColor
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = if (!isUser) {
                    BorderStroke(1.dp, colors.goldHighlight.copy(alpha = 0.25f))
                } else {
                    null
                },
                modifier = Modifier.combinedClickable(
                    onLongClick = onLongPressMessage,
                    onClick = {}
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // AI Assistant Subtle Top Gold Accent Indicator
                    if (!isUser) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(colors.goldHighlight)
                            )
                            Text(
                                text = "Hujjah Lens",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.goldHighlight
                            )
                        }
                    }

                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) {
                            if (colors.isDarkTheme) MaterialTheme.colorScheme.onSurface else Color.White
                        } else {
                            charcoalColor
                        },
                        textAlign = TextAlign.Start,
                        lineHeight = 22.sp
                    )

                    // Render "Solusi Berdalil" if present
                    if (message.solutions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Solusi Berdalil:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.goldHighlight
                        )
                        message.solutions.forEachIndexed { index, solution ->
                            Text(
                                text = "${index + 1}. $solution",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (colors.isDarkTheme) Color.White.copy(alpha = 0.9f) else charcoalColor.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }
                }
            }

            // Render Dalil References below AI bubble
            if (message.references.isNotEmpty()) {
                message.references.forEach { reference ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = MaterialTheme.customShapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = surfaceColor
                        ),
                        border = BorderStroke(1.dp, colors.goldHighlight.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clickable { onClickReference(reference) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .fillMaxHeight()
                                    .background(colors.goldHighlight)
                            )
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkBorder,
                                    contentDescription = null,
                                    tint = colors.goldHighlight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "📖 Baca Surah Penuh: ${reference.sourceName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = charcoalColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== SUB-COMPONENT: HEADER ARABIC ORNAMENT ====================
@Composable
private fun LensHeaderOrnament(
    goldHighlight: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = size.width / 3f

        // Draw small gold diamond geometric motif
        val path = Path().apply {
            moveTo(center.x, center.y - r)
            lineTo(center.x + r, center.y)
            lineTo(center.x, center.y + r)
            lineTo(center.x - r, center.y)
            close()
        }

        drawPath(
            path = path,
            color = goldHighlight.copy(alpha = 0.25f),
            style = Stroke(width = 1.2.dp.toPx())
        )

        drawCircle(
            color = goldHighlight,
            radius = 2.dp.toPx(),
            center = center
        )
    }
}

// ==================== SUB-COMPONENT: BACKGROUND WATERMARK ORNAMENT ====================
@Composable
private fun LensChatBackgroundOrnament(
    goldHighlight: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Subtle geometric arch watermark in top right background
        val path = Path().apply {
            val cx = w * 0.85f
            val cy = h * 0.15f
            val r = w * 0.22f

            moveTo(cx - r, cy)
            cubicTo(
                cx - r, cy - r,
                cx + r, cy - r,
                cx + r, cy
            )
        }

        drawPath(
            path = path,
            color = goldHighlight.copy(alpha = 0.04f),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}
