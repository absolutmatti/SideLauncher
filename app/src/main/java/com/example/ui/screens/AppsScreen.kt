package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.InstalledApp
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppsScreen(
    apps: List<InstalledApp>,
    sortMode: String,
    columns: Int,
    isMoveMode: Boolean,
    movingAppIndex: Int,
    onSortModeChange: (String) -> Unit,
    onAppClick: (InstalledApp, Int) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 27.dp)
    ) {
        // Header with Title, Move Mode Hint or Sort selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.apps_title),
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )

            if (isMoveMode) {
                Text(
                    text = stringResource(R.string.apps_move_mode_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Box {
                    TvFocusableSurface(
                        onClick = { showSortMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        focusedScale = 1.05f
                    ) {
                        Row(
                            modifier = Modifier
                                .background(SurfaceDark)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val sortLabel = when (sortMode) {
                                "LAST_USED" -> stringResource(R.string.sort_last_used)
                                "CUSTOM" -> stringResource(R.string.sort_custom)
                                else -> stringResource(R.string.sort_az)
                            }
                            Text(
                                text = "${stringResource(R.string.apps_sort_label)}: $sortLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = TextPrimary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(SurfaceElevatedDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_az), color = TextPrimary) },
                            onClick = {
                                onSortModeChange("AZ")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_last_used), color = TextPrimary) },
                            onClick = {
                                onSortModeChange("LAST_USED")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_custom), color = TextPrimary) },
                            onClick = {
                                onSortModeChange("CUSTOM")
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid of Apps
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 40.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                items = apps,
                key = { _, app -> app.packageName }
            ) { index, app ->
                val isBeingMoved = isMoveMode && movingAppIndex == index
                AppGridCard(
                    app = app,
                    isBeingMoved = isBeingMoved,
                    onClick = { onAppClick(app, index) },
                    onLongClick = { onAppLongClick(app) }
                )
            }
        }
    }
}

@Composable
fun AppGridCard(
    app: InstalledApp,
    isBeingMoved: Boolean,
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TvFocusableSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(103.dp),
            onClick = onClick,
            onLongClick = onLongClick,
            shape = RoundedCornerShape(14.dp),
            focusBorderColor = if (isBeingMoved) MaterialTheme.colorScheme.primary else Color.White
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isBeingMoved) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        else SurfaceDark
                    ),
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

                // Favorite Star indicator
                if (app.isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Favorit",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
