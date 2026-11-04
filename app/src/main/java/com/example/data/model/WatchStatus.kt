package com.example.data.model

enum class MediaType {
    MOVIE,
    SERIES
}

enum class WatchStatus(val rank: Int) {
    WATCHING(0),     // Am Ansehen (only for series)
    NOT_WATCHED(1),  // Nicht gesehen
    WATCHED(2)       // Gesehen
}
