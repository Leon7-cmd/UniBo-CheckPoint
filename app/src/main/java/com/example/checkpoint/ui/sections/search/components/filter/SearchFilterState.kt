package com.example.checkpoint.ui.sections.search.components.filter

enum class SortOption {
    NAME_ASC,
    RATING_DESC,
    RELEASE_DATE
}

data class SearchFilterState(
    val selectedConsoles: Set<FilterTag> = emptySet(),
    val selectedGenres: Set<FilterTag> = emptySet(),
    val selectedGameplay: Set<FilterTag> = emptySet(),
    val sortBy: SortOption = SortOption.NAME_ASC
) {
    fun hasActiveFilters(): Boolean {
        return selectedConsoles.isNotEmpty() ||
                selectedGenres.isNotEmpty() ||
                selectedGameplay.isNotEmpty() ||
                sortBy != SortOption.NAME_ASC
    }
}