package com.example.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.MediaType
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WatchlistDetailOverlay(
    item: WatchlistItem,
    matchingAppPackage: String?,
    onClose: () -> Unit,
    onStatusChange: (WatchStatus) -> Unit,
    onOpenApp: (String) -> Unit,
    onDelete: (WatchlistItem) -> Unit
) {
    val backdropUrl = if (!item.backdropPath.isNullOrEmpty()) {
        "https://image.tmdb.org/t/p/w1280${item.backdropPath}"
    } else null

    val posterUrl = if (!item.posterPath.isNullOrEmpty()) {
        "https://image.tmdb.org/t/p/w500${item.posterPath}"
    } else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
            .background(Color(0xE6000000))
            .clickable(onClick = onClose) // Click backdrop closes overlay
    ) {
        // Subtle backdrop image
        if (backdropUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(backdropUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xBB0F1115))
            )
        }

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xF50F1115), Color(0xCC0F1115), Color(0x880F1115))
                    )
                )
        )

        // Modal Content Card
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 64.dp, vertical = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster Column
            if (posterUrl != null) {
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(330.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(posterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Details Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                // Title and Release Year
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${if (item.mediaType == MediaType.MOVIE) "Film" else "Serie"} · ${item.releaseYear}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    if (item.voteAverage > 0.0) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", item.voteAverage),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Overview text
                if (item.overview.isNotEmpty()) {
                    Text(
                        text = item.overview,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Watch Providers info
                if (item.providers.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.available_on, item.providers),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Action Buttons Row (Status, Open App, Delete)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Toggle / Selector
                    if (item.mediaType == MediaType.MOVIE) {
                        val isWatched = item.status == WatchStatus.WATCHED
                        TvFocusableSurface(
                            onClick = {
                                onStatusChange(if (isWatched) WatchStatus.NOT_WATCHED else WatchStatus.WATCHED)
                            },
                            shape = RoundedCornerShape(10.dp),
                            focusedScale = 1.05f
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        if (isWatched) MaterialTheme.colorScheme.primaryContainer
                                        else SurfaceElevatedDark
                                    )
                                    .padding(horizontal = 18.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isWatched) Icons.Filled.Check else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isWatched) MaterialTheme.colorScheme.primary else TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isWatched) stringResource(R.string.status_watched)
                                    else stringResource(R.string.status_not_watched),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWatched) MaterialTheme.colorScheme.primary else TextPrimary
                                )
                            }
                        }
                    } else {
                        // Series: 3-Way status options
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                WatchStatus.NOT_WATCHED to stringResource(R.string.status_not_watched),
                                WatchStatus.WATCHING to stringResource(R.string.status_watching),
                                WatchStatus.WATCHED to stringResource(R.string.status_watched)
                            ).forEach { (status, label) ->
                                val isSelected = item.status == status
                                TvFocusableSurface(
                                    onClick = { onStatusChange(status) },
                                    shape = RoundedCornerShape(8.dp),
                                    focusedScale = 1.05f
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else SurfaceElevatedDark
                                            )
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.Black else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Open App Button if streaming app installed
                    if (!matchingAppPackage.isNullOrEmpty()) {
                        TvFocusableSurface(
                            onClick = { onOpenApp(matchingAppPackage) },
                            shape = RoundedCornerShape(10.dp),
                            focusedScale = 1.05f
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 18.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.action_open_app),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Delete Button
                    TvFocusableSurface(
                        onClick = { onDelete(item) },
                        shape = RoundedCornerShape(10.dp),
                        focusedScale = 1.05f
                    ) {
                        Box(
                            modifier = Modifier
                                .background(DangerRed.copy(alpha = 0.2f))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = DangerRed
                            )
                        }
                    }
                }
            }
        }
    }
}
