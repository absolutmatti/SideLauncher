package com.example.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.api.TmdbSearchResult
import com.example.data.model.MediaType
import com.example.data.model.WatchlistItem
import com.example.ui.components.TvFocusableSurface
import com.example.ui.screens.TabButton
import com.example.ui.screens.WatchlistTab
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TmdbSearchOverlay(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    searchResults: List<TmdbSearchResult>,
    existingWatchlistIds: Set<Long>,
    onAddResult: (TmdbSearchResult) -> Unit,
    onClose: () -> Unit
) {
    var searchFilterTab by remember { mutableStateOf(WatchlistTab.ALL) }

    val filteredResults = remember(searchResults, searchFilterTab) {
        when (searchFilterTab) {
            WatchlistTab.ALL -> searchResults
            WatchlistTab.MOVIES -> searchResults.filter { it.mediaType == "movie" }
            WatchlistTab.SERIES -> searchResults.filter { it.mediaType == "tv" }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(Color(0xF00F1115))
            .padding(horizontal = 48.dp, vertical = 27.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Row: Search Input & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input Field Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceDark)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.search_hint),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextMuted
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onQueryChange,
                                textStyle = TextStyle(
                                    fontSize = 18.sp,
                                    color = TextPrimary
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Close Button
                TvFocusableSurface(
                    onClick = onClose,
                    shape = RoundedCornerShape(12.dp),
                    focusedScale = 1.05f
                ) {
                    Box(
                        modifier = Modifier
                            .background(SurfaceDark)
                            .padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Schließen",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Tabs (All / Movies / Series)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabButton(
                    label = stringResource(R.string.tab_all),
                    isSelected = searchFilterTab == WatchlistTab.ALL,
                    onClick = { searchFilterTab = WatchlistTab.ALL }
                )
                TabButton(
                    label = stringResource(R.string.tab_movies),
                    isSelected = searchFilterTab == WatchlistTab.MOVIES,
                    onClick = { searchFilterTab = WatchlistTab.MOVIES }
                )
                TabButton(
                    label = stringResource(R.string.tab_series),
                    isSelected = searchFilterTab == WatchlistTab.SERIES,
                    onClick = { searchFilterTab = WatchlistTab.SERIES }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content Area: Spinner, Empty state, or Grid of results
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (searchQuery.trim().length >= 2 && filteredResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.search_no_results),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMuted
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 40.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredResults,
                        key = { "${it.mediaType}_${it.id}" }
                    ) { result ->
                        val isAlreadyInWatchlist = existingWatchlistIds.contains(result.id)
                        SearchResultCard(
                            result = result,
                            isInWatchlist = isAlreadyInWatchlist,
                            onAdd = { if (!isAlreadyInWatchlist) onAddResult(result) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    result: TmdbSearchResult,
    isInWatchlist: Boolean,
    onAdd: () -> Unit
) {
    val title = result.title ?: result.name ?: result.originalTitle ?: ""
    val releaseDate = result.releaseDate ?: result.firstAirDate ?: ""
    val year = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else ""
    val isMovie = result.mediaType == "movie"

    val posterUrl = if (!result.posterPath.isNullOrEmpty()) {
        "https://image.tmdb.org/t/p/w500${result.posterPath}"
    } else null

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TvFocusableSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            onClick = onAdd,
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                if (posterUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(posterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (isMovie) Icons.Filled.Movie else Icons.Filled.Tv,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // In Watchlist Badge
                if (isInWatchlist) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "In Watchlist",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "${if (isMovie) "Film" else "Serie"} · $year",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
