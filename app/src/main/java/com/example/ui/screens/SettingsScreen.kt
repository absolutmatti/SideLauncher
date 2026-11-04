package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.model.InstalledApp
import com.example.data.preferences.LauncherSettings
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class SettingsCategory {
    GENERAL,
    APPEARANCE,
    APPS,
    WATCHLIST,
    SYSTEM,
    ABOUT
}

@Composable
fun SettingsScreen(
    settings: LauncherSettings,
    hiddenApps: List<InstalledApp>,
    onUpdateStartPage: (String) -> Unit,
    onUpdateTimeFormat: (String) -> Unit,
    onUpdateAccentColor: (String) -> Unit,
    onUpdateBackgroundType: (String) -> Unit,
    onUpdateAutoCollapseSidebar: (Boolean) -> Unit,
    onUpdateAppColumns: (Int) -> Unit,
    onUpdateAppSortMode: (String) -> Unit,
    onUpdateTmdbLanguage: (String) -> Unit,
    onUpdateProviderRegion: (String) -> Unit,
    onUnhideApp: (String) -> Unit,
    onExportWatchlist: () -> Unit,
    onImportWatchlist: () -> Unit,
    onResetWatchlist: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(SettingsCategory.GENERAL) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 27.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Left Column: Categories
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.nav_settings),
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsCategoryItem(
                title = stringResource(R.string.settings_cat_general),
                isSelected = selectedCategory == SettingsCategory.GENERAL,
                onClick = { selectedCategory = SettingsCategory.GENERAL }
            )
            SettingsCategoryItem(
                title = stringResource(R.string.settings_cat_appearance),
                isSelected = selectedCategory == SettingsCategory.APPEARANCE,
                onClick = { selectedCategory = SettingsCategory.APPEARANCE }
            )
            SettingsCategoryItem(
                title = stringResource(R.string.settings_cat_apps),
                isSelected = selectedCategory == SettingsCategory.APPS,
                onClick = { selectedCategory = SettingsCategory.APPS }
            )
            SettingsCategoryItem(
                title = stringResource(R.string.settings_cat_watchlist),
                isSelected = selectedCategory == SettingsCategory.WATCHLIST,
                onClick = { selectedCategory = SettingsCategory.WATCHLIST }
            )
            SettingsCategoryItem(
                title = stringResource(R.string.settings_cat_system),
                isSelected = selectedCategory == SettingsCategory.SYSTEM,
                onClick = { selectedCategory = SettingsCategory.SYSTEM }
            )
            SettingsCategoryItem(
                title = stringResource(R.string.settings_cat_about),
                isSelected = selectedCategory == SettingsCategory.ABOUT,
                onClick = { selectedCategory = SettingsCategory.ABOUT }
            )
        }

        // Right Column: Options Panel
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(24.dp)
        ) {
            when (selectedCategory) {
                SettingsCategory.GENERAL -> GeneralSettingsPanel(
                    settings = settings,
                    onUpdateStartPage = onUpdateStartPage,
                    onUpdateTimeFormat = onUpdateTimeFormat
                )
                SettingsCategory.APPEARANCE -> AppearanceSettingsPanel(
                    settings = settings,
                    onUpdateAccentColor = onUpdateAccentColor,
                    onUpdateBackgroundType = onUpdateBackgroundType,
                    onUpdateAutoCollapseSidebar = onUpdateAutoCollapseSidebar,
                    onUpdateAppColumns = onUpdateAppColumns
                )
                SettingsCategory.APPS -> AppsSettingsPanel(
                    settings = settings,
                    hiddenApps = hiddenApps,
                    onUpdateAppSortMode = onUpdateAppSortMode,
                    onUnhideApp = onUnhideApp
                )
                SettingsCategory.WATCHLIST -> WatchlistSettingsPanel(
                    settings = settings,
                    onUpdateTmdbLanguage = onUpdateTmdbLanguage,
                    onUpdateProviderRegion = onUpdateProviderRegion,
                    onExportWatchlist = onExportWatchlist,
                    onImportWatchlist = onImportWatchlist,
                    onResetWatchlist = onResetWatchlist
                )
                SettingsCategory.SYSTEM -> SystemSettingsPanel()
                SettingsCategory.ABOUT -> AboutSettingsPanel()
            }
        }
    }
}

@Composable
fun SettingsCategoryItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TvFocusableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        focusedScale = 1.03f
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary
            )
        }
    }
}

@Composable
fun GeneralSettingsPanel(
    settings: LauncherSettings,
    onUpdateStartPage: (String) -> Unit,
    onUpdateTimeFormat: (String) -> Unit
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_cat_general),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        // Start Page
        Column {
            Text(
                text = stringResource(R.string.pref_start_page),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    "HOME" to "Home",
                    "APPS" to "Apps",
                    "WATCHLIST" to "Watchlist"
                ).forEach { (key, label) ->
                    val isSel = settings.startPage == key
                    TvFocusableSurface(
                        onClick = { onUpdateStartPage(key) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (isSel) MaterialTheme.colorScheme.primary else SurfaceElevatedDark)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else TextPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = SurfaceElevatedDark)

        // Time format
        Column {
            Text(
                text = stringResource(R.string.pref_time_format),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    "SYSTEM" to "System",
                    "24H" to "24 Stunden",
                    "12H" to "12 Stunden (AM/PM)"
                ).forEach { (key, label) ->
                    val isSel = settings.timeFormat == key
                    TvFocusableSurface(
                        onClick = { onUpdateTimeFormat(key) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (isSel) MaterialTheme.colorScheme.primary else SurfaceElevatedDark)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else TextPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppearanceSettingsPanel(
    settings: LauncherSettings,
    onUpdateAccentColor: (String) -> Unit,
    onUpdateBackgroundType: (String) -> Unit,
    onUpdateAutoCollapseSidebar: (Boolean) -> Unit,
    onUpdateAppColumns: (Int) -> Unit
) {
    val scroll = rememberScrollState()
    val presetColors = listOf(
        "#6C8CFF" to AccentBlue,
        "#22D3EE" to AccentCyan,
        "#10B981" to AccentEmerald,
        "#A855F7" to AccentPurple,
        "#F59E0B" to AccentAmber,
        "#F43F5E" to AccentRose
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_cat_appearance),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        // Accent Color
        Column {
            Text(
                text = stringResource(R.string.pref_accent_color),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                presetColors.forEach { (hex, color) ->
                    val isSel = settings.accentColorHex.equals(hex, ignoreCase = true)
                    TvFocusableSurface(
                        onClick = { onUpdateAccentColor(hex) },
                        shape = CircleShape,
                        focusedScale = 1.15f
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSel) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = SurfaceElevatedDark)

        // Background Style
        Column {
            Text(
                text = stringResource(R.string.pref_background),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    "SOLID" to "Einfarbig",
                    "GRADIENT" to "Verlauf",
                    "DYNAMIC" to "Dynamisch (Poster)"
                ).forEach { (type, label) ->
                    val isSel = settings.backgroundType == type
                    TvFocusableSurface(
                        onClick = { onUpdateBackgroundType(type) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (isSel) MaterialTheme.colorScheme.primary else SurfaceElevatedDark)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else TextPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = SurfaceElevatedDark)

        // Auto collapse sidebar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.pref_collapse_sidebar),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Sidebar klappt automatisch auf Symbolgröße zusammen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            TvFocusableSurface(
                onClick = { onUpdateAutoCollapseSidebar(!settings.autoCollapseSidebar) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    Switch(
                        checked = settings.autoCollapseSidebar,
                        onCheckedChange = { onUpdateAutoCollapseSidebar(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        HorizontalDivider(color = SurfaceElevatedDark)

        // App columns
        Column {
            Text(
                text = stringResource(R.string.pref_app_columns),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(4, 5, 6).forEach { cols ->
                    val isSel = settings.appColumns == cols
                    TvFocusableSurface(
                        onClick = { onUpdateAppColumns(cols) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (isSel) MaterialTheme.colorScheme.primary else SurfaceElevatedDark)
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "$cols Spalten",
                                color = if (isSel) Color.Black else TextPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppsSettingsPanel(
    settings: LauncherSettings,
    hiddenApps: List<InstalledApp>,
    onUpdateAppSortMode: (String) -> Unit,
    onUnhideApp: (String) -> Unit
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_cat_apps),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        // Default Sort
        Column {
            Text(
                text = "Standard-Sortierung",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    "AZ" to stringResource(R.string.sort_az),
                    "LAST_USED" to stringResource(R.string.sort_last_used),
                    "CUSTOM" to stringResource(R.string.sort_custom)
                ).forEach { (mode, label) ->
                    val isSel = settings.appSortMode == mode
                    TvFocusableSurface(
                        onClick = { onUpdateAppSortMode(mode) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (isSel) MaterialTheme.colorScheme.primary else SurfaceElevatedDark)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else TextPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = SurfaceElevatedDark)

        // Hidden Apps Management
        Column {
            Text(
                text = stringResource(R.string.pref_manage_hidden_apps),
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (hiddenApps.isEmpty()) {
                Text(
                    text = stringResource(R.string.pref_no_hidden_apps),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            } else {
                hiddenApps.forEach { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        TvFocusableSurface(
                            onClick = { onUnhideApp(app.packageName) },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(SurfaceElevatedDark)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.apps_unhide),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistSettingsPanel(
    settings: LauncherSettings,
    onUpdateTmdbLanguage: (String) -> Unit,
    onUpdateProviderRegion: (String) -> Unit,
    onExportWatchlist: () -> Unit,
    onImportWatchlist: () -> Unit,
    onResetWatchlist: () -> Unit
) {
    val scroll = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_cat_watchlist),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        // TMDB Language
        Column {
            Text(
                text = stringResource(R.string.pref_tmdb_language),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    "de-DE" to "Deutsch (de-DE)",
                    "en-US" to "English (en-US)"
                ).forEach { (lang, label) ->
                    val isSel = settings.tmdbLanguage == lang
                    TvFocusableSurface(
                        onClick = { onUpdateTmdbLanguage(lang) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (isSel) MaterialTheme.colorScheme.primary else SurfaceElevatedDark)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else TextPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = SurfaceElevatedDark)

        // Provider Region
        Column {
            Text(
                text = stringResource(R.string.pref_provider_region),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    "DE" to "Deutschland",
                    "AT" to "Österreich",
                    "CH" to "Schweiz",
                    "US" to "USA"
                ).forEach { (region, label) ->
                    val isSel = settings.providerRegion == region
                    TvFocusableSurface(
                        onClick = { onUpdateProviderRegion(region) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (isSel) MaterialTheme.colorScheme.primary else SurfaceElevatedDark)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else TextPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = SurfaceElevatedDark)

        // Export / Import JSON & Reset
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TvFocusableSurface(
                    onClick = onExportWatchlist,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(SurfaceElevatedDark)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = TextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.pref_export_watchlist), color = TextPrimary)
                    }
                }

                TvFocusableSurface(
                    onClick = onImportWatchlist,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(SurfaceElevatedDark)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = null, tint = TextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.pref_import_watchlist), color = TextPrimary)
                    }
                }
            }

            TvFocusableSurface(
                onClick = { showResetDialog = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(DangerRed.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = DangerRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.pref_reset_watchlist), color = DangerRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.pref_reset_watchlist), color = TextPrimary) },
            text = { Text(stringResource(R.string.pref_reset_watchlist_confirm), color = TextSecondary) },
            confirmButton = {
                TvFocusableSurface(
                    onClick = {
                        onResetWatchlist()
                        showResetDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.background(DangerRed).padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text(stringResource(R.string.confirm), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TvFocusableSurface(
                    onClick = { showResetDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.background(SurfaceElevatedDark).padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text(stringResource(R.string.cancel), color = TextPrimary)
                    }
                }
            },
            containerColor = SurfaceElevatedDark
        )
    }
}

@Composable
fun SystemSettingsPanel() {
    val context = LocalContext.current
    val scroll = rememberScrollState()

    fun openIntent(action: String) {
        try {
            val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Intent not supported on this TV / emulator, ignore
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_cat_system),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        SystemQuickLinkItem(
            title = stringResource(R.string.sys_wifi),
            icon = Icons.Filled.Wifi,
            onClick = { openIntent(Settings.ACTION_WIFI_SETTINGS) }
        )
        SystemQuickLinkItem(
            title = stringResource(R.string.sys_bluetooth),
            icon = Icons.Filled.Bluetooth,
            onClick = { openIntent(Settings.ACTION_BLUETOOTH_SETTINGS) }
        )
        SystemQuickLinkItem(
            title = stringResource(R.string.sys_display),
            icon = Icons.Filled.Tv,
            onClick = { openIntent(Settings.ACTION_DISPLAY_SETTINGS) }
        )
        SystemQuickLinkItem(
            title = stringResource(R.string.sys_sound),
            icon = Icons.Filled.VolumeUp,
            onClick = { openIntent(Settings.ACTION_SOUND_SETTINGS) }
        )
        SystemQuickLinkItem(
            title = stringResource(R.string.sys_all_settings),
            icon = Icons.Filled.Settings,
            onClick = { openIntent(Settings.ACTION_SETTINGS) }
        )
        SystemQuickLinkItem(
            title = stringResource(R.string.sys_home_settings),
            icon = Icons.Filled.Home,
            onClick = { openIntent(Settings.ACTION_HOME_SETTINGS) }
        )
    }
}

@Composable
fun SystemQuickLinkItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    TvFocusableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceElevatedDark)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        }
    }
}

@Composable
fun AboutSettingsPanel() {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_cat_about),
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        Column {
            Text(
                text = "SideLauncher",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Version 1.0 (Build 1) · Jetpack Compose for TV",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        HorizontalDivider(color = SurfaceElevatedDark)

        // TMDB Attribution (Mandatory by TMDB policy)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "TMDB Attribution",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = stringResource(R.string.tmdb_attribution_text),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = stringResource(R.string.justwatch_attribution_text),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        HorizontalDivider(color = SurfaceElevatedDark)

        // Open Source note
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Open-Source-Lizenzen",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Android Jetpack, Material Design 3, Kotlin Coroutines, Retrofit, OkHttp, Coil, Moshi, Room Database.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
    }
}
