package com.example.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.data.model.MediaType
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WatchlistContextMenuOverlay(
    item: WatchlistItem,
    onDismiss: () -> Unit,
    onStatusChange: (WatchStatus) -> Unit,
    onDelete: (WatchlistItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(120f)
            .background(Color(0xCC000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${if (item.mediaType == MediaType.MOVIE) "Film" else "Serie"} · ${item.releaseYear}",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                fontSize = 12.sp
            )

            HorizontalDivider(color = SurfaceElevatedDark)

            if (item.mediaType == MediaType.MOVIE) {
                val isWatched = item.status == WatchStatus.WATCHED
                ContextMenuItem(
                    title = if (isWatched) stringResource(R.string.action_mark_not_watched)
                    else stringResource(R.string.action_mark_watched),
                    icon = if (isWatched) Icons.Filled.PlayArrow else Icons.Filled.Check,
                    onClick = {
                        onStatusChange(if (isWatched) WatchStatus.NOT_WATCHED else WatchStatus.WATCHED)
                        onDismiss()
                    }
                )
            } else {
                ContextMenuItem(
                    title = stringResource(R.string.status_not_watched),
                    icon = Icons.Filled.PlayArrow,
                    onClick = {
                        onStatusChange(WatchStatus.NOT_WATCHED)
                        onDismiss()
                    }
                )
                ContextMenuItem(
                    title = stringResource(R.string.status_watching),
                    icon = Icons.Filled.PlayArrow,
                    onClick = {
                        onStatusChange(WatchStatus.WATCHING)
                        onDismiss()
                    }
                )
                ContextMenuItem(
                    title = stringResource(R.string.status_watched),
                    icon = Icons.Filled.Check,
                    onClick = {
                        onStatusChange(WatchStatus.WATCHED)
                        onDismiss()
                    }
                )
            }

            HorizontalDivider(color = SurfaceElevatedDark)

            ContextMenuItem(
                title = stringResource(R.string.action_delete),
                icon = Icons.Filled.Delete,
                tint = DangerRed,
                onClick = {
                    onDelete(item)
                    onDismiss()
                }
            )
        }
    }
}
