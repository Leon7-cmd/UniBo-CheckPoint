package com.example.checkpoint.ui.sections.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.BuildConfig
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.remote.NetworkClient
import com.example.checkpoint.data.repository.IgdbRepository
import com.example.checkpoint.ui.sections.search.components.filter.FilterTag
import com.example.checkpoint.ui.sections.search.components.filter.SearchFilterState
import com.example.checkpoint.ui.sections.search.components.filter.SortOption
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val games: List<Game>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    private val igdbRepository: IgdbRepository = IgdbRepository(
        authApiService = NetworkClient.twitchAuthApiService,
        igdbApiService = NetworkClient.igdbApiService,
        clientId = BuildConfig.IGDB_CLIENT_ID,
        clientSecret = BuildConfig.IGDB_CLIENT_SECRET
    )
) : ViewModel() {

    // Text to search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter status
    private val _filterState = MutableStateFlow(SearchFilterState())
    val filterState: StateFlow<SearchFilterState> = _filterState.asStateFlow()

    // Interface status
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        setupSearchPipeline()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchPipeline() {
        viewModelScope.launch {
            // Apply debounce to the text field
            _searchQuery
                .map { it.trim() }
                .debounce(350)
                .distinctUntilChanged()
                .collectLatest { cleanQuery ->
                    if (cleanQuery.length >= 2 || _filterState.value.hasActiveFilters()) {
                        performSearch(cleanQuery, _filterState.value)
                    } else if (cleanQuery.isEmpty() && !_filterState.value.hasActiveFilters()) {
                        _uiState.value = SearchUiState.Idle
                    }
                }
        }
    }

    // Explicit search trigger
    fun performExplicitSearch() {
        viewModelScope.launch {
            val query = _searchQuery.value.trim()
            if (query.length >= 2 || _filterState.value.hasActiveFilters()) {
                performSearch(query, _filterState.value)
            }
        }
    }

    // Text field update
    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // Action update
    fun toggleConsole(tag: FilterTag) {
        _filterState.update { it.copy(selectedConsoles = it.selectedConsoles.toggle(tag)) }
    }
    fun toggleGenre(tag: FilterTag) {
        _filterState.update { it.copy(selectedGenres = it.selectedGenres.toggle(tag)) }
    }
    fun toggleGameplay(tag: FilterTag) {
        _filterState.update { it.copy(selectedGameplay = it.selectedGameplay.toggle(tag)) }
    }
    fun onSortSelected(sortOption: SortOption) {
        _filterState.update { it.copy(sortBy = sortOption) }
    }
    fun resetFilters() {
        _filterState.value = SearchFilterState()
        performExplicitSearch()
    }

    // Call the repository to perform the search
    private suspend fun performSearch(query: String, filters: SearchFilterState) {
        _uiState.value = SearchUiState.Loading

        igdbRepository.searchGames(queryText = query, filters = filters)
            .onSuccess { games ->
                _uiState.value = SearchUiState.Success(games)
            }
            .onFailure { error ->
                _uiState.value = SearchUiState.Error(
                    error.localizedMessage ?: "Errore durante la ricerca"
                )
            }
    }

    // Private utility function to toggle a tag in a set
    private fun <T> Set<T>.toggle(item: T): Set<T> =
        if (contains(item)) this - item else this + item
}