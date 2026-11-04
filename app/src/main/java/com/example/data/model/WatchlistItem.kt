package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistItem(
    @PrimaryKey
    val id: String, // e.g. "movie_123" or "tv_456"
    val tmdbId: Long,
    val mediaType: MediaType,
    val title: String,
    val originalTitle: String? = null,
    val overview: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val releaseYear: String = "",
    val voteAverage: Double = 0.0,
    val status: WatchStatus = WatchStatus.NOT_WATCHED,
    val addedAt: Long = System.currentTimeMillis(),
    val statusChangedAt: Long = System.currentTimeMillis(),
    val providers: String = "" // Comma-separated list of watch providers (e.g. "Netflix, Disney+")
)
