package com.example.checkpoint.ui.sections.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.checkpoint.ui.sections.search.components.filter.SearchFilterPanel
import com.example.checkpoint.ui.sections.search.components.SearchGameCard
import com.example.checkpoint.ui.sections.search.components.SearchInputRow

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onGameClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var isFilterPanelOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search field and filter panel
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SearchInputRow(
                    query = searchQuery,
                    onQueryChange = viewModel::onQueryChange,
                    isPanelOpen = isFilterPanelOpen,
                    activeFiltersCount = filterState.selectedConsoles.size +
                            filterState.selectedGenres.size +
                            filterState.selectedGameplay.size,
                    onTogglePanel = { isFilterPanelOpen = !isFilterPanelOpen }
                )

                AnimatedVisibility(
                    visible = isFilterPanelOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        SearchFilterPanel(
                            filterState = filterState,
                            onConsoleToggle = viewModel::toggleConsole,
                            onGenreToggle = viewModel::toggleGenre,
                            onGameplayToggle = viewModel::toggleGameplay,
                            onSortSelected = viewModel::onSortSelected,
                            onReset = viewModel::resetFilters,
                            onApplySearch = {
                                viewModel.performExplicitSearch()
                                isFilterPanelOpen = false
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Result list
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                SearchUiState.Idle -> {
                    Text(
                        text = "Digita almeno 2 caratteri o seleziona un filtro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SearchUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is SearchUiState.Error -> {
                    Text(
                        text = "Errore: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is SearchUiState.Success -> {
                    if (state.games.isEmpty()) {
                        Text(
                            text = "Nessun gioco trovato.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.games, key = { it.id }) { game ->
                                SearchGameCard(
                                    game = game,
                                    onClick = { onGameClick(game.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}