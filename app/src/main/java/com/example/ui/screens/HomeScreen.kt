package com.example.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.InstalledApp
import com.example.data.model.MediaType
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    watchlistPreview: List<WatchlistItem>,
    hasWatchingSeries: Boolean,
    favorites: List<InstalledApp>,
    timeFormat: String,
    onWatchlistClick: (WatchlistItem) -> Unit,
    onViewAllWatchlist: () -> Unit,
    onEmptyWatchlistClick: () -> Unit,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onAddFavoritesClick: () -> Unit,
    onItemFocused: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 48.dp, vertical = 27.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // 1. Clock & Date Display Widget (Non-focusable)
        ClockDateWidget(timeFormat = timeFormat)

        // 2. Watchlist Preview Section ("Weiterschauen" or "Als Nächstes")
        WatchlistPreviewSection(
            items = watchlistPreview,
            hasWatchingSeries = hasWatchingSeries,
            onWatchlistClick = onWatchlistClick,
            onViewAllWatchlist = onViewAllWatchlist,
            onEmptyWatchlistClick = onEmptyWatchlistClick,
            onItemFocused = onItemFocused
        )

        // 3. Favorites Section (Apps, max 10, plus "+" card)
        FavoritesSection(
            favorites = favorites,
            onAppClick = onAppClick,
            onAppLongClick = onAppLongClick,
            onAddFavoritesClick = onAddFavoritesClick,
            onItemFocused = onItemFocused
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ClockDateWidget(timeFormat: String) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }

    // Register receiver for ACTION_TIME_TICK, TIMEZONE_CHANGED, TIME_CHANGED
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                currentTime = Date()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (ignored: Exception) {}
        }
    }

    val timePattern = when (timeFormat) {
        "12H" -> "hh:mm a"
        "24H" -> "HH:mm"
        else -> if (android.text.format.DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a"
    }

    val timeString = remember(currentTime, timePattern) {
        SimpleDateFormat(timePattern, Locale.getDefault()).format(currentTime)
    }

    val dateString = remember(currentTime) {
        SimpleDateFormat("EEEE, d. MMMM", Locale.getDefault()).format(currentTime)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Light,
                color = TextPrimary
            )
            Text(
                text = dateString,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun WatchlistPreviewSection(
    items: List<WatchlistItem>,
    hasWatchingSeries: Boolean,
    onWatchlistClick: (WatchlistItem) -> Unit,
    onViewAllWatchlist: () -> Unit,
    onEmptyWatchlistClick: () -> Unit,
    onItemFocused: (String) -> Unit
) {
    val sectionTitle = if (hasWatchingSeries) {
        stringResource(R.string.home_watchlist_watching)
    } else {
        stringResource(R.string.home_watchlist_next)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            TvFocusableSurface(
                onClick = onViewAllWatchlist,
                shape = RoundedCornerShape(8.dp),
                focusedScale = 1.05f
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_view_all),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            TvFocusableSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                onClick = onEmptyWatchlistClick,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceDark)
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.home_watchlist_empty_prompt),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Füge deine Lieblingsfilme und -serien zur Liste hinzu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(end = 48.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    WatchlistPreviewCard(
                        item = item,
                        onClick = { onWatchlistClick(item) },
                        onFocused = { onItemFocused(item.backdropPath ?: item.posterPath ?: "") }
                    )
                }

                item(key = "view_all_card") {
                    TvFocusableSurface(
                        modifier = Modifier
                            .width(160.dp)
                            .height(124.dp),
                        onClick = onViewAllWatchlist,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SurfaceDark),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.home_view_all),
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistPreviewCard(
    item: WatchlistItem,
    onClick: () -> Unit,
    onFocused: () -> Unit
) {
    val imageUrl = when {
        !item.backdropPath.isNullOrEmpty() -> "https://image.tmdb.org/t/p/w780${item.backdropPath}"
        !item.posterPath.isNullOrEmpty() -> "https://image.tmdb.org/t/p/w500${item.posterPath}"
        else -> null
    }

    TvFocusableSurface(
        modifier = Modifier
            .width(220.dp)
            .height(124.dp),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        onFocusChanged = { if (it) onFocused() }
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceDark)
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.mediaType == MediaType.MOVIE) Icons.Filled.Movie else Icons.Filled.Tv,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Scrim overlay at the bottom for title readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC000000)),
                            startY = 40f
                        )
                    )
            )

            // Top Badges (Playing / Type)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.status == WatchStatus.WATCHING) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = stringResource(R.string.status_watching_badge),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x99000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (item.mediaType == MediaType.MOVIE) "Film" else "Serie",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }

            // Title at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FavoritesSection(
    favorites: List<InstalledApp>,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onAddFavoritesClick: () -> Unit,
    onItemFocused: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_favorites_title),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 48.dp)
        ) {
            items(favorites, key = { it.packageName }) { app ->
                FavoriteAppCard(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) }
                )
            }

            // "+" Card to add favorites if less than 10
            if (favorites.size < 10) {
                item(key = "add_favorite_card") {
                    TvFocusableSurface(
                        modifier = Modifier
                            .width(184.dp)
                            .height(103.dp),
                        onClick = onAddFavoritesClick,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SurfaceDark),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.home_add_favorites),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteAppCard(
    app: InstalledApp,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val appIcon = remember(app.packageName) {
        try {
            pm.getApplicationIcon(app.packageName)
        } catch (e: Exception) {
            null
        }
    }

    TvFocusableSurface(
        modifier = Modifier
            .width(184.dp)
            .height(103.dp),
        onClick = onClick,
        onLongClick = onLongClick,
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceElevatedDark),
            contentAlignment = Alignment.Center
        ) {
            if (appIcon != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(appIcon)
                        .crossfade(true)
                        .build(),
                    contentDescription = app.label,
                    modifier = Modifier.size(54.dp)
                )
            } else {
                Text(
                    text = app.label.take(2).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Bottom title label
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xCC000000))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
