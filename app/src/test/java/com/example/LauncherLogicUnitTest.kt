package com.example

import com.example.data.model.MediaType
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherLogicUnitTest {

    @Test
    fun watchlistSorting_ranksWatchingAboveNotWatchedAndWatched() {
        val seriesWatching = WatchlistItem(
            id = "1",
            tmdbId = 100,
            mediaType = MediaType.SERIES,
            title = "Breaking Bad",
            status = WatchStatus.WATCHING,
            addedAt = 1000L
        )
        val movieNotWatched = WatchlistItem(
            id = "2",
            tmdbId = 200,
            mediaType = MediaType.MOVIE,
            title = "Interstellar",
            status = WatchStatus.NOT_WATCHED,
            addedAt = 2000L
        )
        val movieWatched = WatchlistItem(
            id = "3",
            tmdbId = 300,
            mediaType = MediaType.MOVIE,
            title = "Inception",
            status = WatchStatus.WATCHED,
            addedAt = 500L,
            statusChangedAt = 3000L
        )

        val unsorted = listOf(movieWatched, movieNotWatched, seriesWatching)
        val sorted = unsorted.sortedWith { a, b ->
            if (a.status.rank != b.status.rank) {
                a.status.rank.compareTo(b.status.rank)
            } else if (a.status == WatchStatus.WATCHED) {
                b.statusChangedAt.compareTo(a.statusChangedAt)
            } else {
                b.addedAt.compareTo(a.addedAt)
            }
        }

        assertEquals(seriesWatching.id, sorted[0].id)
        assertEquals(movieNotWatched.id, sorted[1].id)
        assertEquals(movieWatched.id, sorted[2].id)
    }

    @Test
    fun watchStatusRanks_areCorrect() {
        assertTrue(WatchStatus.WATCHING.rank < WatchStatus.NOT_WATCHED.rank)
        assertTrue(WatchStatus.NOT_WATCHED.rank < WatchStatus.WATCHED.rank)
    }
}
