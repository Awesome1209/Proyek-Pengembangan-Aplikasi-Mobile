package com.example.hujjah.presentation.screens.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hujjah.domain.model.NoteCategory
import com.example.hujjah.presentation.components.LoadingIndicator
import com.example.hujjah.presentation.components.NoteCard
import com.example.hujjah.presentation.components.hujjah.HujjahEmptyState
import com.example.hujjah.presentation.theme.LocalHujjahColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddNote: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: NotesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalHujjahColors.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NotesEvent.Error -> snackbarHostState.showSnackbar(event.message)
                is NotesEvent.NotePinnedToggled -> snackbarHostState.showSnackbar("Status pin diubah")
                is NotesEvent.NoteDeleted -> snackbarHostState.showSnackbar("Catatan berhasil dihapus")
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Catatan Harian",
                        fontWeight = FontWeight.Bold,
                        color = colors.goldHighlight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = colors.goldHighlight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddNote,
                containerColor = colors.goldHighlight,
                contentColor = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Catatan")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // ==================== SEARCH BAR WITH iOS FAST DELETE (X) ====================
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Cari Catatan...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.goldHighlight) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus Pencarian",
                                tint = colors.goldHighlight
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.goldHighlight,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ==================== CATEGORIES FILTER CHIPS ROW ====================
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    val isAllSelected = uiState.selectedCategory == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { viewModel.onCategorySelected(null) },
                        label = { Text("Semua") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.goldHighlight,
                            selectedLabelColor = MaterialTheme.colorScheme.background
                        )
                    )
                }

                items(NoteCategory.entries) { category ->
                    val isSelected = uiState.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.goldHighlight,
                            selectedLabelColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==================== LIST OF NOTES ====================
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            } else if (uiState.notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    HujjahEmptyState(
                        title = "Belum Ada Catatan",
                        message = if (uiState.searchQuery.isNotEmpty()) {
                            "Tidak ada catatan yang cocok dengan pencarian \"${uiState.searchQuery}\""
                        } else {
                            "Mulai catat pemikiran Anda atau simpan nasihat spiritual dari AI Hujjah Lens."
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNavigateToDetail(note.id) },
                            onPinClick = { viewModel.togglePin(note.id) },
                            onDeleteClick = { viewModel.deleteNote(note.id) }
                        )
                    }
                }
            }
        }
    }
}
