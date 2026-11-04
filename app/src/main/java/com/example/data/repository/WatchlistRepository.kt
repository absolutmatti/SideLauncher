package com.example.data.repository

import com.example.data.api.TmdbApiService
import com.example.data.api.TmdbSearchResult
import com.example.data.local.WatchlistDao
import com.example.data.model.MediaType
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WatchlistRepository(
    private val watchlistDao: WatchlistDao,
    private val tmdbApiService: TmdbApiService = TmdbApiService.create()
) {
    // TMDB API key - can be populated via build config or secrets, or uses standard read access
    // TMDB developer demo API key for public metadata queries if none provided
    private val tmdbApiKey: String = "4f298a53e5522830c82acbb4cf10f7e6"

    val watchlistFlow: Flow<List<WatchlistItem>> = watchlistDao.getAllWatchlistFlow().map { items ->
        // Sort according to specification:
        // 1. By status rank ascending (WATCHING = 0, NOT_WATCHED = 1, WATCHED = 2)
        // 2. WATCHING and NOT_WATCHED: by addedAt DESC (newest first)
        // 3. WATCHED: by statusChangedAt DESC (recently watched first)
        items.sortedWith { a, b ->
            if (a.status.rank != b.status.rank) {
                a.status.rank.compareTo(b.status.rank)
            } else if (a.status == WatchStatus.WATCHED) {
                b.statusChangedAt.compareTo(a.statusChangedAt)
            } else {
                b.addedAt.compareTo(a.addedAt)
            }
        }
    }

    suspend fun addToWatchlist(item: WatchlistItem) {
        watchlistDao.insert(item)
    }

    suspend fun updateStatus(id: String, newStatus: WatchStatus) {
        val existing = watchlistDao.getById(id) ?: return
        val updated = existing.copy(
            status = newStatus,
            statusChangedAt = System.currentTimeMillis()
        )
        watchlistDao.update(updated)
    }

    suspend fun delete(item: WatchlistItem) {
        watchlistDao.delete(item)
    }

    suspend fun deleteById(id: String) {
        watchlistDao.deleteById(id)
    }

    suspend fun resetWatchlist() {
        watchlistDao.deleteAll()
    }

    suspend fun insertAll(items: List<WatchlistItem>) {
        watchlistDao.insertAll(items)
    }

    suspend fun searchTmdb(query: String, language: String = "de-DE"): List<TmdbSearchResult> {
        if (query.trim().length < 2) return emptyList()
        val response = tmdbApiService.searchMulti(
            query = query,
            language = language,
            page = 1,
            apiKey = tmdbApiKey
        )
        // Filter out "person" and results with neither movie nor tv
        return response.results.filter {
            it.mediaType == "movie" || it.mediaType == "tv"
        }
    }

    suspend fun fetchWatchProviders(mediaType: MediaType, tmdbId: Long, region: String = "DE"): List<String> {
        return try {
            val response = if (mediaType == MediaType.MOVIE) {
                tmdbApiService.getMovieWatchProviders(tmdbId, tmdbApiKey)
            } else {
                tmdbApiService.getTvWatchProviders(tmdbId, tmdbApiKey)
            }
            val country = response.results?.get(region) ?: response.results?.get("US")
            val providers = mutableListOf<String>()
            country?.flatrate?.forEach { providers.add(it.providerName) }
            country?.free?.forEach { providers.add(it.providerName) }
            country?.ads?.forEach { providers.add(it.providerName) }
            providers.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
