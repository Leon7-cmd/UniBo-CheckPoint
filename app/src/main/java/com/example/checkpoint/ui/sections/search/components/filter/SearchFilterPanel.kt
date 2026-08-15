package com.example.checkpoint.ui.sections.search.components.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchFilterPanel(
    filterState: SearchFilterState,
    onConsoleToggle: (FilterTag) -> Unit,
    onGenreToggle: (FilterTag) -> Unit,
    onGameplayToggle: (FilterTag) -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onReset: () -> Unit,
    onApplySearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title and Reset Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filtri di Ricerca",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(
                onClick = onReset,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text("Ripristina", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 1. CONSOLE
        ExpandableFilterSection(
            title = "Console",
            tags = IgdbFilterTags.CONSOLES,
            selectedTags = filterState.selectedConsoles,
            onTagToggle = onConsoleToggle
        )

        // 2. GENRE
        ExpandableFilterSection(
            title = "Genere",
            tags = IgdbFilterTags.GENRES,
            selectedTags = filterState.selectedGenres,
            onTagToggle = onGenreToggle
        )

        // 3. GAMEPLAY & THEMATIC
        ExpandableFilterSection(
            title = "Gameplay & Tematiche",
            tags = IgdbFilterTags.GAMEPLAY_AND_THEMES,
            selectedTags = filterState.selectedGameplay,
            onTagToggle = onGameplayToggle
        )

        // 4. ORDER
        Text(
            text = "Ordina per",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            SortOption.entries.forEach { option ->
                val label = when (option) {
                    SortOption.NAME_ASC -> "Nome (A-Z)"
                    SortOption.RATING_DESC -> "Voto"
                    SortOption.RELEASE_DATE -> "Data Uscita"
                }
                FilterChip(
                    selected = filterState.sortBy == option,
                    onClick = { onSortSelected(option) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        // Apply and Search Button
        Button(
            onClick = onApplySearch,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Applica e Cerca")
        }
    }
}

// Reusable component for expandable filter sections
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpandableFilterSection(
    title: String,
    tags: List<FilterTag>,
    selectedTags: Set<FilterTag>,
    onTagToggle: (FilterTag) -> Unit,
    initialCount: Int = 8
) {
    var isExpanded by remember { mutableStateOf(false) }
    val visibleTags = if (isExpanded) tags else tags.take(initialCount)

    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        visibleTags.forEach { tag ->
            FilterChip(
                selected = selectedTags.contains(tag),
                onClick = { onTagToggle(tag) },
                label = { Text(tag.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }

    if (tags.size > initialCount) {
        Box(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    text = if (isExpanded) "Meno" else "Tutti (${tags.size})",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
}