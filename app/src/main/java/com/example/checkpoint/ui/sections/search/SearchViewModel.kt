package com.example.checkpoint.ui.sections.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.BuildConfig
import com.example.checkpoint.data.remote.NetworkClient
import com.example.checkpoint.data.repository.IgdbRepository
import com.example.checkpoint.ui.sections.search.components.filter.FilterTag
import com.example.checkpoint.ui.sections.search.components.filter.SearchFilterState
import com.example.checkpoint.ui.sections.search.components.filter.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    private val igdbRepository: IgdbRepository = IgdbRepository(
        authApiService = NetworkClient.twitchAuthApiService,
        igdbApiService = NetworkClient.igdbApiService,
        clientId = BuildConfig.IGDB_CLIENT_ID,
        clientSecret = BuildConfig.IGDB_CLIENT_SECRET
    )
) : ViewModel() {

    // Query text state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter criteria state
    private val _filterState = MutableStateFlow(SearchFilterState())
    val filterState: StateFlow<SearchFilterState> = _filterState.asStateFlow()

    // UI result state
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        setupSearchPipeline()
    }

    // Reactive search pipeline for query and filter changes
    @OptIn(FlowPreview::class)
    private fun setupSearchPipeline() {
        viewModelScope.launch {
            combine(
                _searchQuery.map { it.trim() }.debounce(350).distinctUntilChanged(),
                _filterState
            ) { query, filters ->
                Pair(query, filters)
            }
                .flowOn(Dispatchers.Default)
                .collectLatest { (cleanQuery, filters) ->
                    if (cleanQuery.length >= 2 || filters.hasActiveFilters()) {
                        performSearch(cleanQuery, filters)
                    } else {
                        _uiState.value = SearchUiState.Idle
                    }
                }
        }
    }

    // Manual search trigger
    fun performExplicitSearch() {
        viewModelScope.launch {
            val query = _searchQuery.value.trim()
            val filters = _filterState.value
            if (query.length >= 2 || filters.hasActiveFilters()) {
                performSearch(query, filters)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

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
    }

    // Execute search request via repository
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

    private fun <T> Set<T>.toggle(item: T): Set<T> =
        if (contains(item)) this - item else this + item
}