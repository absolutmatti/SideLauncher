package com.example.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.TmdbSearchResult
import com.example.data.local.LauncherDatabase
import com.example.data.model.AppMetadata
import com.example.data.model.InstalledApp
import com.example.data.model.MediaType
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.data.preferences.LauncherSettings
import com.example.data.preferences.PreferencesRepository
import com.example.data.repository.AppsRepository
import com.example.data.repository.WatchlistRepository
import com.example.ui.navigation.Destination
import com.example.ui.screens.WatchlistTab
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class LauncherUiState(
    val currentDestination: Destination = Destination.HOME,
    val settings: LauncherSettings = LauncherSettings(),
    val installedApps: List<InstalledApp> = emptyList(),
    val favorites: List<InstalledApp> = emptyList(),
    val hiddenApps: List<InstalledApp> = emptyList(),
    val watchlist: List<WatchlistItem> = emptyList(),
    val watchlistPreview: List<WatchlistItem> = emptyList(),
    val hasWatchingSeries: Boolean = false,
    val selectedWatchlistTab: WatchlistTab = WatchlistTab.ALL,
    // Overlays state
    val selectedDetailItem: WatchlistItem? = null,
    val showSearchOverlay: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<TmdbSearchResult> = emptyList(),
    val selectedAppContextMenu: InstalledApp? = null,
    val selectedWatchlistContextMenu: WatchlistItem? = null,
    val showSelectFavoritesDialog: Boolean = false,
    // Moving mode state for Apps reorder
    val isMoveMode: Boolean = false,
    val movingAppIndex: Int = -1,
    // Undo deletion snackbar
    val recentlyDeletedItem: WatchlistItem? = null,
    // Active backdrop for dynamic background
    val currentDynamicBackdropUrl: String? = null
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val database = LauncherDatabase.getInstance(application)
    private val preferencesRepository = PreferencesRepository(application)
    private val appsRepository = AppsRepository(application, database.appMetadataDao())
    private val watchlistRepository = WatchlistRepository(database.watchlistDao())

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var undoJob: Job? = null

    init {
        // Collect Settings
        viewModelScope.launch {
            preferencesRepository.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    currentDestination = when (settings.startPage) {
                        "APPS" -> Destination.APPS
                        "WATCHLIST" -> Destination.WATCHLIST
                        else -> Destination.HOME
                    }
                )
            }
        }

        // Collect Installed Apps & Metadata
        viewModelScope.launch {
            appsRepository.installedAppsFlow.collect { allApps ->
                val settings = _uiState.value.settings
                val sorted = appsRepository.sortApps(allApps.filter { !it.isHidden }, settings.appSortMode)
                val favs = allApps.filter { it.isFavorite && !it.isHidden }
                    .sortedBy { it.favoriteOrder }
                    .take(10)
                val hidden = allApps.filter { it.isHidden }

                _uiState.value = _uiState.value.copy(
                    installedApps = sorted,
                    favorites = favs,
                    hiddenApps = hidden
                )
            }
        }

        // Collect Watchlist
        viewModelScope.launch {
            watchlistRepository.watchlistFlow.collect { list ->
                // First 6 entries for preview without already watched:
                // 1. Series currently WATCHING
                // 2. Unwatched items
                val previewList = list.filter { it.status != WatchStatus.WATCHED }.take(6)
                val hasWatching = list.any { it.mediaType == MediaType.SERIES && it.status == WatchStatus.WATCHING }

                _uiState.value = _uiState.value.copy(
                    watchlist = list,
                    watchlistPreview = previewList,
                    hasWatchingSeries = hasWatching
                )
            }
        }

        // Seed initial sample watchlist titles if empty for immediate TV rich experience
        viewModelScope.launch {
            seedSampleWatchlistIfEmpty()
        }
    }

    private suspend fun seedSampleWatchlistIfEmpty() {
        // Only seed once if database is completely empty
        val existing = database.watchlistDao().getById("series_breaking_bad")
        if (existing == null) {
            val sampleItems = listOf(
                WatchlistItem(
                    id = "series_breaking_bad",
                    tmdbId = 1396,
                    mediaType = MediaType.SERIES,
                    title = "Breaking Bad",
                    overview = "Ein an Lungenkrebs erkrankter Chemielehrer beginnt Methamphetamin herzustellen, um die finanzielle Zukunft seiner Familie zu sichern.",
                    posterPath = "/ztkUQFLlC19CCMYHW9o1zWhJAGq.jpg",
                    backdropPath = "/tsRy63Mu5cu8etL1X7ZLyf7UP1M.jpg",
                    releaseYear = "2008",
                    voteAverage = 8.9,
                    status = WatchStatus.WATCHING,
                    providers = "Netflix"
                ),
                WatchlistItem(
                    id = "movie_interstellar",
                    tmdbId = 157336,
                    mediaType = MediaType.MOVIE,
                    title = "Interstellar",
                    overview = "Eine Gruppe von Forschern bricht durch ein neu entdecktes Wurmloch in eine andere Galaxie auf, um das Überleben der Menschheit zu sichern.",
                    posterPath = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                    backdropPath = "/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
                    releaseYear = "2014",
                    voteAverage = 8.4,
                    status = WatchStatus.NOT_WATCHED,
                    providers = "Amazon Prime Video, Paramount Plus"
                ),
                WatchlistItem(
                    id = "series_stranger_things",
                    tmdbId = 66732,
                    mediaType = MediaType.SERIES,
                    title = "Stranger Things",
                    overview = "Nach dem Verschwinden eines Jungen deckt eine Kleinstadt ein Geheimnis auf, zu dem geheime Experimente und übernatürliche Kräfte gehören.",
                    posterPath = "/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
                    backdropPath = "/56v2KjBlU4XaOv9rVYEQypROD7P.jpg",
                    releaseYear = "2016",
                    voteAverage = 8.6,
                    status = WatchStatus.NOT_WATCHED,
                    providers = "Netflix"
                ),
                WatchlistItem(
                    id = "movie_dune_2",
                    tmdbId = 693134,
                    mediaType = MediaType.MOVIE,
                    title = "Dune: Part Two",
                    overview = "Paul Atreides verbündet sich mit Chani und den Fremen auf einem Rachefeldzug gegen die Verschwörer, die seine Familie zerstört haben.",
                    posterPath = "/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg",
                    backdropPath = "/xOMo8BRK7PfcJv9JCnx7s520b4q.jpg",
                    releaseYear = "2024",
                    voteAverage = 8.2,
                    status = WatchStatus.NOT_WATCHED,
                    providers = "Max, WOW"
                )
            )
            watchlistRepository.insertAll(sampleItems)
        }
    }

    // Navigation
    fun setDestination(dest: Destination) {
        _uiState.value = _uiState.value.copy(currentDestination = dest)
    }

    fun setWatchlistTab(tab: WatchlistTab) {
        _uiState.value = _uiState.value.copy(selectedWatchlistTab = tab)
    }

    fun setDynamicBackdrop(url: String?) {
        _uiState.value = _uiState.value.copy(currentDynamicBackdropUrl = url)
    }

    // App Management
    fun launchApp(app: InstalledApp) {
        viewModelScope.launch {
            appsRepository.recordAppLaunch(app.packageName)
            val success = appsRepository.launchApp(app.packageName)
            if (!success) {
                Toast.makeText(getApplication(), "App konnte nicht gestartet werden", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggleFavorite(app: InstalledApp) {
        viewModelScope.launch {
            if (!app.isFavorite && _uiState.value.favorites.size >= 10) {
                Toast.makeText(getApplication(), "Maximal 10 Favoriten möglich", Toast.LENGTH_SHORT).show()
                return@launch
            }
            appsRepository.setFavorite(app.packageName, !app.isFavorite)
        }
    }

    fun hideApp(app: InstalledApp) {
        viewModelScope.launch {
            appsRepository.setHidden(app.packageName, true)
            Toast.makeText(getApplication(), "„${app.label}“ ausgeblendet. In Einstellungen wiederherstellbar.", Toast.LENGTH_SHORT).show()
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            appsRepository.setHidden(packageName, false)
        }
    }

    fun startMoveMode(app: InstalledApp) {
        val index = _uiState.value.installedApps.indexOfFirst { it.packageName == app.packageName }
        _uiState.value = _uiState.value.copy(isMoveMode = true, movingAppIndex = index)
    }

    fun finishMoveMode() {
        val apps = _uiState.value.installedApps
        viewModelScope.launch {
            appsRepository.updateCustomOrder(apps.map { it.packageName })
        }
        _uiState.value = _uiState.value.copy(isMoveMode = false, movingAppIndex = -1)
    }

    // Overlays Management
    fun openDetail(item: WatchlistItem) {
        _uiState.value = _uiState.value.copy(selectedDetailItem = item)
    }

    fun closeDetail() {
        _uiState.value = _uiState.value.copy(selectedDetailItem = null)
    }

    fun openSearch() {
        _uiState.value = _uiState.value.copy(showSearchOverlay = true)
    }

    fun closeSearch() {
        _uiState.value = _uiState.value.copy(
            showSearchOverlay = false,
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    fun openAppContextMenu(app: InstalledApp) {
        _uiState.value = _uiState.value.copy(selectedAppContextMenu = app)
    }

    fun closeAppContextMenu() {
        _uiState.value = _uiState.value.copy(selectedAppContextMenu = null)
    }

    fun openWatchlistContextMenu(item: WatchlistItem) {
        _uiState.value = _uiState.value.copy(selectedWatchlistContextMenu = item)
    }

    fun closeWatchlistContextMenu() {
        _uiState.value = _uiState.value.copy(selectedWatchlistContextMenu = null)
    }

    fun openSelectFavoritesDialog() {
        _uiState.value = _uiState.value.copy(showSelectFavoritesDialog = true)
    }

    fun closeSelectFavoritesDialog() {
        _uiState.value = _uiState.value.copy(showSelectFavoritesDialog = false)
    }

    fun updateWatchStatus(item: WatchlistItem, newStatus: WatchStatus) {
        viewModelScope.launch {
            watchlistRepository.updateStatus(item.id, newStatus)
            if (_uiState.value.selectedDetailItem?.id == item.id) {
                _uiState.value = _uiState.value.copy(
                    selectedDetailItem = item.copy(status = newStatus)
                )
            }
        }
    }

    fun deleteWatchlistItem(item: WatchlistItem) {
        viewModelScope.launch {
            // Soft-Delete with 5-second undo snackbar
            _uiState.value = _uiState.value.copy(
                recentlyDeletedItem = item,
                selectedDetailItem = null,
                selectedWatchlistContextMenu = null
            )
            watchlistRepository.delete(item)

            undoJob?.cancel()
            undoJob = viewModelScope.launch {
                delay(5000)
                _uiState.value = _uiState.value.copy(recentlyDeletedItem = null)
            }
        }
    }

    fun undoDelete() {
        val item = _uiState.value.recentlyDeletedItem ?: return
        viewModelScope.launch {
            watchlistRepository.addToWatchlist(item)
            _uiState.value = _uiState.value.copy(recentlyDeletedItem = null)
            undoJob?.cancel()
        }
    }

    // TMDB Search with 400ms Debounce
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.value = _uiState.value.copy(isSearching = false, searchResults = emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // 400 ms debounce
            _uiState.value = _uiState.value.copy(isSearching = true)
            try {
                val results = watchlistRepository.searchTmdb(
                    query = query,
                    language = _uiState.value.settings.tmdbLanguage
                )
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    searchResults = results
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }

    fun addSearchResultToWatchlist(result: TmdbSearchResult) {
        viewModelScope.launch {
            val isMovie = result.mediaType == "movie"
            val id = "${if (isMovie) "movie" else "tv"}_${result.id}"
            val title = result.title ?: result.name ?: result.originalTitle ?: ""
            val releaseDate = result.releaseDate ?: result.firstAirDate ?: ""
            val year = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else ""

            val newItem = WatchlistItem(
                id = id,
                tmdbId = result.id,
                mediaType = if (isMovie) MediaType.MOVIE else MediaType.SERIES,
                title = title,
                originalTitle = result.originalTitle ?: result.originalName,
                overview = result.overview ?: "",
                posterPath = result.posterPath,
                backdropPath = result.backdropPath,
                releaseYear = year,
                voteAverage = result.voteAverage ?: 0.0,
                status = WatchStatus.NOT_WATCHED
            )

            // Asynchronously fetch watch providers
            val providers = watchlistRepository.fetchWatchProviders(
                mediaType = newItem.mediaType,
                tmdbId = newItem.tmdbId,
                region = _uiState.value.settings.providerRegion
            )
            val itemWithProviders = newItem.copy(providers = providers.joinToString(", "))
            watchlistRepository.addToWatchlist(itemWithProviders)

            Toast.makeText(getApplication(), "„$title“ zur Watchlist hinzugefügt!", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper for Launching Streaming App matching providers
    fun getInstalledStreamingPackage(providersText: String): String? {
        if (providersText.isEmpty()) return null
        val providerList = providersText.split(",").map { it.trim() }
        for (p in providerList) {
            val pkg = appsRepository.providerToPackageMap[p]
            if (pkg != null && appsRepository.isAppInstalled(pkg)) {
                return pkg
            }
        }
        return null
    }

    // Settings Update Actions
    fun updateStartPage(page: String) = viewModelScope.launch { preferencesRepository.updateStartPage(page) }
    fun updateTimeFormat(format: String) = viewModelScope.launch { preferencesRepository.updateTimeFormat(format) }
    fun updateAccentColor(hex: String) = viewModelScope.launch { preferencesRepository.updateAccentColor(hex) }
    fun updateBackgroundType(type: String) = viewModelScope.launch { preferencesRepository.updateBackgroundType(type) }
    fun updateAutoCollapseSidebar(enabled: Boolean) = viewModelScope.launch { preferencesRepository.updateAutoCollapseSidebar(enabled) }
    fun updateAppColumns(cols: Int) = viewModelScope.launch { preferencesRepository.updateAppColumns(cols) }
    fun updateAppSortMode(mode: String) = viewModelScope.launch { preferencesRepository.updateAppSortMode(mode) }
    fun updateTmdbLanguage(lang: String) = viewModelScope.launch { preferencesRepository.updateTmdbLanguage(lang) }
    fun updateProviderRegion(reg: String) = viewModelScope.launch { preferencesRepository.updateProviderRegion(reg) }

    fun resetWatchlist() = viewModelScope.launch {
        watchlistRepository.resetWatchlist()
        Toast.makeText(getApplication(), "Watchlist wurde zurückgesetzt", Toast.LENGTH_SHORT).show()
    }

    fun exportWatchlist() {
        viewModelScope.launch {
            try {
                val list = _uiState.value.watchlist
                val jsonArray = JSONArray()
                for (item in list) {
                    val obj = JSONObject().apply {
                        put("id", item.id)
                        put("tmdbId", item.tmdbId)
                        put("mediaType", item.mediaType.name)
                        put("title", item.title)
                        put("overview", item.overview)
                        put("posterPath", item.posterPath ?: "")
                        put("backdropPath", item.backdropPath ?: "")
                        put("releaseYear", item.releaseYear)
                        put("voteAverage", item.voteAverage)
                        put("status", item.status.name)
                        put("providers", item.providers)
                    }
                    jsonArray.put(obj)
                }
                val file = File(getApplication<Application>().getExternalFilesDir(null), "watchlist_backup.json")
                file.writeText(jsonArray.toString(2))
                Toast.makeText(getApplication(), "Exportiert nach: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Export fehlgeschlagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun importWatchlist() {
        viewModelScope.launch {
            try {
                val file = File(getApplication<Application>().getExternalFilesDir(null), "watchlist_backup.json")
                if (!file.exists()) {
                    Toast.makeText(getApplication(), "Keine Backup-Datei gefunden in: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    return@launch
                }
                val text = file.readText()
                val jsonArray = JSONArray(text)
                val items = mutableListOf<WatchlistItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    items.add(
                        WatchlistItem(
                            id = obj.getString("id"),
                            tmdbId = obj.getLong("tmdbId"),
                            mediaType = MediaType.valueOf(obj.getString("mediaType")),
                            title = obj.getString("title"),
                            overview = obj.optString("overview", ""),
                            posterPath = obj.optString("posterPath").takeIf { it.isNotEmpty() },
                            backdropPath = obj.optString("backdropPath").takeIf { it.isNotEmpty() },
                            releaseYear = obj.optString("releaseYear", ""),
                            voteAverage = obj.optDouble("voteAverage", 0.0),
                            status = WatchStatus.valueOf(obj.getString("status")),
                            providers = obj.optString("providers", "")
                        )
                    )
                }
                watchlistRepository.insertAll(items)
                Toast.makeText(getApplication(), "${items.size} Titel importiert!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Import fehlgeschlagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onHomeKeyPressed() {
        // According to specification: on Home key press, close all overlays and switch to Home destination
        _uiState.value = _uiState.value.copy(
            selectedDetailItem = null,
            showSearchOverlay = false,
            selectedAppContextMenu = null,
            selectedWatchlistContextMenu = null,
            showSelectFavoritesDialog = false,
            isMoveMode = false,
            currentDestination = Destination.HOME
        )
    }

    fun handleBackPress(): Boolean {
        // Return true if handled internally
        val state = _uiState.value
        return when {
            state.selectedDetailItem != null -> {
                closeDetail()
                true
            }
            state.showSearchOverlay -> {
                closeSearch()
                true
            }
            state.selectedAppContextMenu != null -> {
                closeAppContextMenu()
                true
            }
            state.selectedWatchlistContextMenu != null -> {
                closeWatchlistContextMenu()
                true
            }
            state.showSelectFavoritesDialog -> {
                closeSelectFavoritesDialog()
                true
            }
            state.isMoveMode -> {
                finishMoveMode()
                true
            }
            state.currentDestination != Destination.HOME -> {
                // If in Content other than Home, back returns to Home or Sidebar
                setDestination(Destination.HOME)
                true
            }
            else -> {
                // In Home content: never terminate launcher activity!
                true
            }
        }
    }
}
