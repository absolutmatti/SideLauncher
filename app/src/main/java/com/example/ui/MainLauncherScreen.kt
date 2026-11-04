package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.components.TvFocusableSurface
import com.example.ui.components.TvSidebar
import com.example.ui.navigation.Destination
import com.example.ui.overlays.AppContextMenuOverlay
import com.example.ui.overlays.SelectFavoritesDialog
import com.example.ui.overlays.TmdbSearchOverlay
import com.example.ui.overlays.WatchlistContextMenuOverlay
import com.example.ui.overlays.WatchlistDetailOverlay
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.LauncherUiState
import com.example.ui.viewmodel.LauncherViewModel

@Composable
fun MainLauncherScreen(viewModel: LauncherViewModel, uiState: LauncherUiState) {
    var isSidebarFocused by remember { mutableStateOf(false) }

    // Intercept Back button: Overlays -> Destinations -> Home (Launcher never closes!)
    BackHandler {
        viewModel.handleBackPress()
    }

    // Dynamic background or solid / gradient
    val backgroundBrush = remember(uiState.settings.backgroundType, uiState.settings.accentColorHex) {
        val accent = try {
            Color(android.graphics.Color.parseColor(uiState.settings.accentColorHex))
        } catch (e: Exception) {
            Color(0xFF6C8CFF)
        }

        when (uiState.settings.backgroundType) {
            "GRADIENT" -> Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.22f), BackgroundDark),
                radius = 1200f
            )
            else -> Brush.verticalGradient(
                colors = listOf(BackgroundDark, BackgroundDark)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Dynamic Backdrop if set and enabled
        if (uiState.settings.backgroundType == "DYNAMIC" && !uiState.currentDynamicBackdropUrl.isNullOrEmpty()) {
            Crossfade(
                targetState = uiState.currentDynamicBackdropUrl,
                animationSpec = tween(400),
                label = "dynamic_backdrop"
            ) { path ->
                val fullUrl = "https://image.tmdb.org/t/p/w1280$path"
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(fullUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Scrim over dynamic backdrop for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDD0F1115))
            )
        }

        // Layout with Sidebar and Content
        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar on Left
            TvSidebar(
                currentDestination = uiState.currentDestination,
                onDestinationSelected = { dest ->
                    viewModel.setDestination(dest)
                },
                autoCollapse = uiState.settings.autoCollapseSidebar,
                onSidebarFocusChanged = { focused ->
                    isSidebarFocused = focused
                }
            )

            // Content Area on Right
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Crossfade page switching (200ms)
                Crossfade(
                    targetState = uiState.currentDestination,
                    animationSpec = tween(durationMillis = 200),
                    label = "page_crossfade"
                ) { destination ->
                    when (destination) {
                        Destination.HOME -> HomeScreen(
                            watchlistPreview = uiState.watchlistPreview,
                            hasWatchingSeries = uiState.hasWatchingSeries,
                            favorites = uiState.favorites,
                            timeFormat = uiState.settings.timeFormat,
                            onWatchlistClick = { item -> viewModel.openDetail(item) },
                            onViewAllWatchlist = { viewModel.setDestination(Destination.WATCHLIST) },
                            onEmptyWatchlistClick = { viewModel.openSearch() },
                            onAppClick = { app -> viewModel.launchApp(app) },
                            onAppLongClick = { app -> viewModel.openAppContextMenu(app) },
                            onAddFavoritesClick = { viewModel.openSelectFavoritesDialog() },
                            onItemFocused = { backdrop -> viewModel.setDynamicBackdrop(backdrop) }
                        )

                        Destination.APPS -> AppsScreen(
                            apps = uiState.installedApps,
                            sortMode = uiState.settings.appSortMode,
                            columns = uiState.settings.appColumns,
                            isMoveMode = uiState.isMoveMode,
                            movingAppIndex = uiState.movingAppIndex,
                            onSortModeChange = { mode -> viewModel.updateAppSortMode(mode) },
                            onAppClick = { app, index ->
                                if (uiState.isMoveMode) {
                                    viewModel.finishMoveMode()
                                } else {
                                    viewModel.launchApp(app)
                                }
                            },
                            onAppLongClick = { app -> viewModel.openAppContextMenu(app) }
                        )

                        Destination.WATCHLIST -> WatchlistScreen(
                            items = uiState.watchlist,
                            selectedTab = uiState.selectedWatchlistTab,
                            onTabSelected = { tab -> viewModel.setWatchlistTab(tab) },
                            onAddClick = { viewModel.openSearch() },
                            onItemClick = { item -> viewModel.openDetail(item) },
                            onItemLongClick = { item -> viewModel.openWatchlistContextMenu(item) },
                            onItemFocused = { backdrop -> viewModel.setDynamicBackdrop(backdrop) }
                        )

                        Destination.SETTINGS -> SettingsScreen(
                            settings = uiState.settings,
                            hiddenApps = uiState.hiddenApps,
                            onUpdateStartPage = { viewModel.updateStartPage(it) },
                            onUpdateTimeFormat = { viewModel.updateTimeFormat(it) },
                            onUpdateAccentColor = { viewModel.updateAccentColor(it) },
                            onUpdateBackgroundType = { viewModel.updateBackgroundType(it) },
                            onUpdateAutoCollapseSidebar = { viewModel.updateAutoCollapseSidebar(it) },
                            onUpdateAppColumns = { viewModel.updateAppColumns(it) },
                            onUpdateAppSortMode = { viewModel.updateAppSortMode(it) },
                            onUpdateTmdbLanguage = { viewModel.updateTmdbLanguage(it) },
                            onUpdateProviderRegion = { viewModel.updateProviderRegion(it) },
                            onUnhideApp = { viewModel.unhideApp(it) },
                            onExportWatchlist = { viewModel.exportWatchlist() },
                            onImportWatchlist = { viewModel.importWatchlist() },
                            onResetWatchlist = { viewModel.resetWatchlist() }
                        )
                    }
                }

                // Scrim when Sidebar is focused (50% opacity dimming)
                if (isSidebarFocused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000))
                            .zIndex(30f)
                    )
                }
            }
        }

        // ==================== OVERLAYS ====================

        // 1. Watchlist Item Detail Overlay
        if (uiState.selectedDetailItem != null) {
            val matchingPkg = viewModel.getInstalledStreamingPackage(uiState.selectedDetailItem.providers)
            WatchlistDetailOverlay(
                item = uiState.selectedDetailItem,
                matchingAppPackage = matchingPkg,
                onClose = { viewModel.closeDetail() },
                onStatusChange = { newStatus ->
                    viewModel.updateWatchStatus(uiState.selectedDetailItem, newStatus)
                },
                onOpenApp = { pkg ->
                    val app = uiState.installedApps.find { it.packageName == pkg }
                    if (app != null) viewModel.launchApp(app)
                },
                onDelete = { item ->
                    viewModel.deleteWatchlistItem(item)
                }
            )
        }

        // 2. TMDB Search Overlay
        if (uiState.showSearchOverlay) {
            val existingIds = remember(uiState.watchlist) {
                uiState.watchlist.map { it.tmdbId }.toSet()
            }
            TmdbSearchOverlay(
                searchQuery = uiState.searchQuery,
                onQueryChange = { q -> viewModel.onSearchQueryChanged(q) },
                isLoading = uiState.isSearching,
                searchResults = uiState.searchResults,
                existingWatchlistIds = existingIds,
                onAddResult = { res -> viewModel.addSearchResultToWatchlist(res) },
                onClose = { viewModel.closeSearch() }
            )
        }

        // 3. App Context Menu
        if (uiState.selectedAppContextMenu != null) {
            AppContextMenuOverlay(
                app = uiState.selectedAppContextMenu,
                favoriteCount = uiState.favorites.size,
                onDismiss = { viewModel.closeAppContextMenu() },
                onToggleFavorite = { app -> viewModel.toggleFavorite(app) },
                onStartMoveMode = { app -> viewModel.startMoveMode(app) },
                onHideApp = { app -> viewModel.hideApp(app) }
            )
        }

        // 4. Watchlist Context Menu
        if (uiState.selectedWatchlistContextMenu != null) {
            WatchlistContextMenuOverlay(
                item = uiState.selectedWatchlistContextMenu,
                onDismiss = { viewModel.closeWatchlistContextMenu() },
                onStatusChange = { status ->
                    viewModel.updateWatchStatus(uiState.selectedWatchlistContextMenu, status)
                },
                onDelete = { item ->
                    viewModel.deleteWatchlistItem(item)
                }
            )
        }

        // 5. Select Favorites Dialog
        if (uiState.showSelectFavoritesDialog) {
            SelectFavoritesDialog(
                allApps = uiState.installedApps,
                onToggleFavorite = { app -> viewModel.toggleFavorite(app) },
                onDismiss = { viewModel.closeSelectFavoritesDialog() }
            )
        }

        // 6. Undo Deletion Snackbar (5 Seconds duration)
        if (uiState.recentlyDeletedItem != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .zIndex(200f)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevatedDark)
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.item_deleted_msg, uiState.recentlyDeletedItem.title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    TvFocusableSurface(
                        onClick = { viewModel.undoDelete() },
                        shape = RoundedCornerShape(8.dp),
                        focusedScale = 1.05f
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.action_undo),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
